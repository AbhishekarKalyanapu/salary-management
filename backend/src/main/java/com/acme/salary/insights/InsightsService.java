package com.acme.salary.insights;

import com.acme.salary.domain.OutlierDetector;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.insights.dto.DimensionStatsResponse;
import com.acme.salary.insights.dto.DistributionBucketResponse;
import com.acme.salary.insights.dto.OutlierResponse;
import com.acme.salary.insights.dto.SummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates the insight queries: {@code summary}, {@code by/{dimension}} and
 * {@code distribution} are answered straight from {@link InsightsRepository}'s SQL;
 * {@code outliers} pulls flat salary rows and runs them through the existing, unit-tested
 * {@link OutlierDetector} rather than reimplementing peer-group comparison in SQL.
 */
@Service
public class InsightsService {

    private static final int DISTRIBUTION_BUCKETS = 10;

    private final InsightsRepository insightsRepository;
    private final EmployeeRepository employeeRepository;
    private final OutlierDetector outlierDetector;

    public InsightsService(InsightsRepository insightsRepository,
                            EmployeeRepository employeeRepository,
                            OutlierDetector outlierDetector) {
        this.insightsRepository = insightsRepository;
        this.employeeRepository = employeeRepository;
        this.outlierDetector = outlierDetector;
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary() {
        return insightsRepository.summary();
    }

    @Transactional(readOnly = true)
    public List<DimensionStatsResponse> byDimension(String dimension) {
        return switch (dimension.toLowerCase()) {
            case "country" -> insightsRepository.byCountry();
            case "department" -> insightsRepository.byDepartment();
            case "jobtitle" -> insightsRepository.byJobTitle();
            default -> throw new IllegalArgumentException(
                    "Unknown dimension '" + dimension + "': expected country, department, or jobTitle");
        };
    }

    /** Equal-width buckets in USD spanning the full min\u2013max range, so the histogram shape
     *  makes sense even though salaries span very different currencies and scales. */
    @Transactional(readOnly = true)
    public List<DistributionBucketResponse> distribution() {
        List<BigDecimal> salaries = insightsRepository.allSalariesUsd();
        if (salaries.isEmpty()) {
            return List.of();
        }
        BigDecimal min = salaries.stream().min(BigDecimal::compareTo).orElseThrow();
        BigDecimal max = salaries.stream().max(BigDecimal::compareTo).orElseThrow();
        BigDecimal range = max.subtract(min);
        if (range.signum() == 0) {
            return List.of(new DistributionBucketResponse(min, max, salaries.size()));
        }
        BigDecimal bucketWidth = range.divide(BigDecimal.valueOf(DISTRIBUTION_BUCKETS), 2, RoundingMode.HALF_UP);

        int[] counts = new int[DISTRIBUTION_BUCKETS];
        for (BigDecimal salary : salaries) {
            int index = salary.subtract(min).divide(bucketWidth, 0, RoundingMode.FLOOR).intValue();
            counts[Math.min(index, DISTRIBUTION_BUCKETS - 1)]++;
        }

        List<DistributionBucketResponse> buckets = new ArrayList<>();
        for (int i = 0; i < DISTRIBUTION_BUCKETS; i++) {
            BigDecimal rangeStart = min.add(bucketWidth.multiply(BigDecimal.valueOf(i)));
            BigDecimal rangeEnd = i == DISTRIBUTION_BUCKETS - 1 ? max : rangeStart.add(bucketWidth);
            buckets.add(new DistributionBucketResponse(rangeStart, rangeEnd, counts[i]));
        }
        return buckets;
    }

    @Transactional(readOnly = true)
    public List<OutlierResponse> outliers(BigDecimal thresholdPct, int minGroupSize) {
        List<OutlierDetector.SalaryRecord> records = insightsRepository.allSalaryRecords();
        List<OutlierDetector.Outlier> outliers = outlierDetector.detect(records, thresholdPct, minGroupSize);

        Map<Long, Employee> employeesById = employeeRepository
                .findAllById(outliers.stream().map(OutlierDetector.Outlier::employeeId).toList())
                .stream()
                .collect(Collectors.toMap(Employee::getId, Function.identity()));

        return outliers.stream()
                .map(o -> {
                    Employee e = employeesById.get(o.employeeId());
                    return new OutlierResponse(
                            o.employeeId(), e.getFirstName(), e.getLastName(),
                            e.getCountry().getCode(), e.getJobTitle().getTitle(), e.getCurrency().getCode(),
                            o.salary(), o.groupMedian(), o.deviationPct());
                })
                .toList();
    }
}
