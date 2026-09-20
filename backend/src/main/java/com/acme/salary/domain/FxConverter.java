package com.acme.salary.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

/**
 * Converts amounts using a fixed table of "1 unit of currency = N USD" rates.
 * Pure and deterministic: rates are supplied by the caller (loaded from the currency table).
 */
public final class FxConverter {

    private static final int MONEY_SCALE = 2;

    private final Map<String, BigDecimal> rateToUsd;

    public FxConverter(Map<String, BigDecimal> rateToUsd) {
        Objects.requireNonNull(rateToUsd, "rateToUsd");
        rateToUsd.forEach((code, rate) -> {
            if (rate == null || rate.signum() <= 0) {
                throw new IllegalArgumentException("Rate for " + code + " must be positive");
            }
        });
        this.rateToUsd = Map.copyOf(rateToUsd);
    }

    public BigDecimal toUsd(BigDecimal amount, String currency) {
        return amount.multiply(rateFor(currency)).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** Converts via USD at full precision and rounds once, at the end. */
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        BigDecimal usd = amount.multiply(rateFor(from));
        return usd.divide(rateFor(to), MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal rateFor(String currency) {
        BigDecimal rate = rateToUsd.get(currency);
        if (rate == null) {
            throw new UnknownCurrencyException(currency);
        }
        return rate;
    }
}
