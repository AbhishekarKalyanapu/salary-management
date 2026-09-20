package com.acme.salary.insights.dto;

import java.math.BigDecimal;

/**
 * One group's pay statistics for {@code GET /api/insights/by/{dimension}}.
 * For {@code country}, figures are in that country's own local currency (a single country is
 * already one currency, so no conversion is needed or wanted). For {@code department} and
 * {@code jobTitle}, which span multiple countries/currencies, figures are converted to USD
 * so the groups are actually comparable \u2014 see REQUIREMENTS.md's multi-currency section.
 */
public record DimensionStatsResponse(
        String key,
        String label,
        String currencyCode,
        int count,
        BigDecimal min,
        BigDecimal median,
        BigDecimal mean,
        BigDecimal p90,
        BigDecimal max) {
}
