package com.acme.salary.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

/**
 * Percentiles using linear interpolation between closest ranks: the same definition as
 * SQL Server's PERCENTILE_CONT, so in-memory and database results agree.
 */
public final class Percentiles {

    private static final BigDecimal HALF = new BigDecimal("0.5");

    private Percentiles() {
    }

    /** @param p fraction between 0 and 1 inclusive (0.5 = median) */
    public static BigDecimal percentile(Collection<BigDecimal> values, BigDecimal p) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
        if (p.signum() < 0 || p.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("p must be between 0 and 1");
        }
        return interpolate(values.stream().sorted().toList(), p);
    }

    public static BigDecimal median(Collection<BigDecimal> values) {
        return percentile(values, HALF);
    }

    /** @param sorted ascending, non-empty */
    static BigDecimal interpolate(List<BigDecimal> sorted, BigDecimal p) {
        BigDecimal position = p.multiply(BigDecimal.valueOf(sorted.size() - 1L));
        int lowerIndex = position.setScale(0, RoundingMode.FLOOR).intValueExact();
        int upperIndex = Math.min(lowerIndex + 1, sorted.size() - 1);
        BigDecimal fraction = position.subtract(BigDecimal.valueOf(lowerIndex));
        BigDecimal lower = sorted.get(lowerIndex);
        BigDecimal upper = sorted.get(upperIndex);
        return lower.add(upper.subtract(lower).multiply(fraction)).setScale(2, RoundingMode.HALF_UP);
    }
}
