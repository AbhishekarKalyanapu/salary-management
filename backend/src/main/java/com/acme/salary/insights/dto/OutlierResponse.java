package com.acme.salary.insights.dto;

import java.math.BigDecimal;

/**
 * An employee flagged by {@link com.acme.salary.domain.OutlierDetector}: their salary is
 * more than {@code thresholdPct} away from the median of peers with the same job title and
 * country. Salary and groupMedian are in local currency, so the comparison is never distorted
 * by FX \u2014 see DESIGN.md.
 */
public record OutlierResponse(
        Long employeeId,
        String firstName,
        String lastName,
        String countryCode,
        String jobTitleTitle,
        String currencyCode,
        BigDecimal salary,
        BigDecimal groupMedian,
        BigDecimal deviationPct) {
}
