package com.acme.salary.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FxConverterTest {

    private final FxConverter fx = new FxConverter(Map.of(
            "USD", new BigDecimal("1"),
            "EUR", new BigDecimal("1.10"),
            "INR", new BigDecimal("0.012")));

    @Test
    void convertsLocalCurrencyToUsd() {
        assertThat(fx.toUsd(new BigDecimal("1000"), "EUR")).isEqualByComparingTo("1100.00");
        assertThat(fx.toUsd(new BigDecimal("1000000"), "INR")).isEqualByComparingTo("12000.00");
    }

    @Test
    void usdToUsdIsUnchanged() {
        assertThat(fx.toUsd(new BigDecimal("123.45"), "USD")).isEqualByComparingTo("123.45");
    }

    @Test
    void roundsHalfUpToTwoDecimals() {
        // 100.05 * 1.10 = 110.055 exactly
        assertThat(fx.toUsd(new BigDecimal("100.05"), "EUR")).isEqualByComparingTo("110.06");
    }

    @Test
    void convertsBetweenTwoCurrenciesViaUsdRoundingOnlyOnce() {
        // 1000 EUR = 1100 USD = 91666.666... INR
        assertThat(fx.convert(new BigDecimal("1000"), "EUR", "INR")).isEqualByComparingTo("91666.67");
    }

    @Test
    void rejectsUnknownCurrency() {
        assertThatThrownBy(() -> fx.toUsd(BigDecimal.ONE, "XYZ"))
                .isInstanceOf(UnknownCurrencyException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void rejectsNonPositiveRates() {
        assertThatThrownBy(() -> new FxConverter(Map.of("EUR", BigDecimal.ZERO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EUR");
    }
}
