package com.acme.salary.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PercentilesTest {

    private static List<BigDecimal> values(String... numbers) {
        List<BigDecimal> list = new ArrayList<>();
        for (String n : numbers) {
            list.add(new BigDecimal(n));
        }
        return list;
    }

    @Test
    void medianOfOddCountIsTheMiddleValue() {
        assertThat(Percentiles.median(values("1", "2", "3"))).isEqualByComparingTo("2.00");
    }

    @Test
    void medianOfEvenCountInterpolatesBetweenTheTwoMiddleValues() {
        assertThat(Percentiles.median(values("10", "20", "30", "40"))).isEqualByComparingTo("25.00");
    }

    @Test
    void inputOrderDoesNotMatterAndInputIsNotMutated() {
        List<BigDecimal> input = values("30", "10", "20");

        assertThat(Percentiles.median(input)).isEqualByComparingTo("20.00");
        assertThat(input).extracting(BigDecimal::intValue).containsExactly(30, 10, 20);
    }

    @Test
    void percentileLandingExactlyOnARankReturnsThatValue() {
        // 11 values: position = 0.9 * 10 = 9 -> the 10th value
        List<BigDecimal> oneToEleven = values("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");

        assertThat(Percentiles.percentile(oneToEleven, new BigDecimal("0.9"))).isEqualByComparingTo("10.00");
    }

    @Test
    void percentileBetweenRanksInterpolatesLinearly() {
        // position = 0.9 * 4 = 3.6 -> 400 + 0.6 * (500 - 400)
        List<BigDecimal> five = values("100", "200", "300", "400", "500");

        assertThat(Percentiles.percentile(five, new BigDecimal("0.9"))).isEqualByComparingTo("460.00");
    }

    @Test
    void singleValueIsEveryPercentile() {
        assertThat(Percentiles.percentile(values("42"), new BigDecimal("0.9"))).isEqualByComparingTo("42.00");
    }

    @Test
    void rejectsEmptyInput() {
        assertThatThrownBy(() -> Percentiles.median(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsPercentileOutsideZeroToOne() {
        assertThatThrownBy(() -> Percentiles.percentile(values("1"), new BigDecimal("1.5")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Percentiles.percentile(values("1"), new BigDecimal("-0.1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
