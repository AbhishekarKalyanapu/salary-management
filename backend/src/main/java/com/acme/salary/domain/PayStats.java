package com.acme.salary.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

/** Summary statistics for a group of salaries, all in one currency. */
public record PayStats(int count, BigDecimal min, BigDecimal median, BigDecimal mean,
                       BigDecimal p90, BigDecimal max) {

    private static final BigDecimal P90 = new BigDecimal("0.9");
    private static final BigDecimal HALF = new BigDecimal("0.5");

    public static PayStats of(Collection<BigDecimal> salaries) {
        if (salaries.isEmpty()) {
            throw new IllegalArgumentException("salaries must not be empty");
        }
        List<BigDecimal> sorted = salaries.stream().sorted().toList();
        int n = sorted.size();
        BigDecimal sum = sorted.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PayStats(
                n,
                sorted.get(0).setScale(2, RoundingMode.HALF_UP),
                Percentiles.interpolate(sorted, HALF),
                sum.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP),
                Percentiles.interpolate(sorted, P90),
                sorted.get(n - 1).setScale(2, RoundingMode.HALF_UP));
    }
}
