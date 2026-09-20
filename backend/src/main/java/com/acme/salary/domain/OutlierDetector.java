package com.acme.salary.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Flags employees whose salary deviates from the median of their peers, where peers are
 * people with the same job title in the same country. Salaries are compared in local
 * currency, so FX rates never distort the result.
 */
public final class OutlierDetector {

    public static final int DEFAULT_MIN_GROUP_SIZE = 5;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public record SalaryRecord(long employeeId, String countryCode, long jobTitleId, BigDecimal salary) {
    }

    /** @param deviationPct signed: positive = above the peer median, negative = below */
    public record Outlier(long employeeId, BigDecimal salary, BigDecimal groupMedian, BigDecimal deviationPct) {
    }

    private record GroupKey(String countryCode, long jobTitleId) {
    }

    /**
     * @param thresholdPct   minimum absolute deviation (in percent) to be flagged; inclusive
     * @param minGroupSize   peer groups smaller than this are skipped: a median of two people means little
     * @return outliers ordered by absolute deviation, largest first
     */
    public List<Outlier> detect(Collection<SalaryRecord> records, BigDecimal thresholdPct, int minGroupSize) {
        if (thresholdPct.signum() <= 0) {
            throw new IllegalArgumentException("thresholdPct must be positive");
        }
        if (minGroupSize < 2) {
            throw new IllegalArgumentException("minGroupSize must be at least 2");
        }

        Map<GroupKey, List<SalaryRecord>> groups = records.stream()
                .collect(Collectors.groupingBy(r -> new GroupKey(r.countryCode(), r.jobTitleId())));

        List<Outlier> outliers = new ArrayList<>();
        for (List<SalaryRecord> group : groups.values()) {
            if (group.size() < minGroupSize) {
                continue;
            }
            BigDecimal median = Percentiles.median(group.stream().map(SalaryRecord::salary).toList());
            if (median.signum() == 0) {
                continue;
            }
            for (SalaryRecord record : group) {
                BigDecimal deviation = deviationPct(record.salary(), median);
                if (deviation.abs().compareTo(thresholdPct) >= 0) {
                    outliers.add(new Outlier(record.employeeId(), record.salary(), median, deviation));
                }
            }
        }

        outliers.sort(Comparator.comparing((Outlier o) -> o.deviationPct().abs()).reversed()
                .thenComparingLong(Outlier::employeeId));
        return List.copyOf(outliers);
    }

    static BigDecimal deviationPct(BigDecimal salary, BigDecimal median) {
        return salary.subtract(median).multiply(HUNDRED).divide(median, 2, RoundingMode.HALF_UP);
    }
}
