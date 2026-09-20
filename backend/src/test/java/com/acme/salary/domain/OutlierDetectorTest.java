package com.acme.salary.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.salary.domain.OutlierDetector.Outlier;
import com.acme.salary.domain.OutlierDetector.SalaryRecord;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OutlierDetectorTest {

    private static final BigDecimal FIFTY_PERCENT = new BigDecimal("50");

    private final OutlierDetector detector = new OutlierDetector();

    /** Peers in one country and job title; ids are assigned consecutively from firstId. */
    private static List<SalaryRecord> peers(String country, long jobTitleId, long firstId, String... salaries) {
        List<SalaryRecord> records = new ArrayList<>();
        long id = firstId;
        for (String salary : salaries) {
            records.add(new SalaryRecord(id++, country, jobTitleId, new BigDecimal(salary)));
        }
        return records;
    }

    @Test
    void flagsSomeoneFarAbovePeersAndReportsTheDetails() {
        List<SalaryRecord> records = peers("DE", 1, 1, "100", "100", "100", "100", "200");

        List<Outlier> outliers = detector.detect(records, FIFTY_PERCENT, 5);

        assertThat(outliers).hasSize(1);
        Outlier outlier = outliers.get(0);
        assertThat(outlier.employeeId()).isEqualTo(5L);
        assertThat(outlier.groupMedian()).isEqualByComparingTo("100.00");
        assertThat(outlier.deviationPct()).isEqualByComparingTo("100.00");
    }

    @Test
    void flagsSomeoneFarBelowPeersWithNegativeDeviation() {
        List<SalaryRecord> records = peers("DE", 1, 1, "100", "100", "100", "100", "40");

        List<Outlier> outliers = detector.detect(records, FIFTY_PERCENT, 5);

        assertThat(outliers).extracting(Outlier::employeeId).containsExactly(5L);
        assertThat(outliers.get(0).deviationPct()).isEqualByComparingTo("-60.00");
    }

    @Test
    void thresholdIsInclusive() {
        List<SalaryRecord> records = peers("DE", 1, 1, "100", "100", "100", "100", "150");

        assertThat(detector.detect(records, FIFTY_PERCENT, 5)).hasSize(1);
    }

    @Test
    void ignoresPeopleWithinTheThreshold() {
        List<SalaryRecord> records = peers("DE", 1, 1, "95", "100", "100", "100", "105");

        assertThat(detector.detect(records, FIFTY_PERCENT, 5)).isEmpty();
    }

    @Test
    void skipsPeerGroupsSmallerThanTheMinimum() {
        List<SalaryRecord> records = peers("DE", 1, 1, "100", "100", "1000");

        assertThat(detector.detect(records, FIFTY_PERCENT, 5)).isEmpty();
    }

    @Test
    void comparesOnlyWithinTheSameCountryAndJobTitle() {
        List<SalaryRecord> records = new ArrayList<>();
        records.addAll(peers("DE", 1, 1, "95", "100", "100", "100", "105"));
        records.addAll(peers("IN", 1, 11, "9500", "10000", "10000", "10000", "10500"));
        records.addAll(peers("DE", 2, 21, "190", "200", "200", "200", "210"));

        assertThat(detector.detect(records, FIFTY_PERCENT, 5)).isEmpty();
    }

    @Test
    void ordersByLargestAbsoluteDeviationFirst() {
        // median is 100; id 8 is +60%, id 9 is -80%
        List<SalaryRecord> records = peers("DE", 1, 1,
                "100", "100", "100", "100", "100", "100", "100", "160", "20");

        List<Outlier> outliers = detector.detect(records, FIFTY_PERCENT, 5);

        assertThat(outliers).extracting(Outlier::employeeId).containsExactly(9L, 8L);
    }

    @Test
    void rejectsInvalidParameters() {
        assertThatThrownBy(() -> detector.detect(List.of(), BigDecimal.ZERO, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> detector.detect(List.of(), FIFTY_PERCENT, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
