package com.acme.salary.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PayStatsTest {

    private static List<BigDecimal> salaries(String... numbers) {
        return java.util.Arrays.stream(numbers).map(BigDecimal::new).toList();
    }

    @Test
    void summarisesAGroupOfSalaries() {
        PayStats stats = PayStats.of(salaries("50000", "60000", "70000", "80000", "90000",
                "100000", "110000", "120000", "130000", "140000"));

        assertThat(stats.count()).isEqualTo(10);
        assertThat(stats.min()).isEqualByComparingTo("50000.00");
        assertThat(stats.max()).isEqualByComparingTo("140000.00");
        assertThat(stats.mean()).isEqualByComparingTo("95000.00");
        assertThat(stats.median()).isEqualByComparingTo("95000.00");
        // position 0.9 * 9 = 8.1 -> 130000 + 0.1 * 10000
        assertThat(stats.p90()).isEqualByComparingTo("131000.00");
    }

    @Test
    void meanIsRoundedToTwoDecimals() {
        assertThat(PayStats.of(salaries("100", "100", "101")).mean()).isEqualByComparingTo("100.33");
    }

    @Test
    void singleSalaryFillsEveryStatistic() {
        PayStats stats = PayStats.of(salaries("75000"));

        assertThat(stats.count()).isEqualTo(1);
        assertThat(stats.min()).isEqualByComparingTo("75000.00");
        assertThat(stats.median()).isEqualByComparingTo("75000.00");
        assertThat(stats.mean()).isEqualByComparingTo("75000.00");
        assertThat(stats.p90()).isEqualByComparingTo("75000.00");
        assertThat(stats.max()).isEqualByComparingTo("75000.00");
    }

    @Test
    void rejectsEmptyInput() {
        assertThatThrownBy(() -> PayStats.of(List.of())).isInstanceOf(IllegalArgumentException.class);
    }
}
