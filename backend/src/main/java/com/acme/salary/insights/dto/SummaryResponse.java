package com.acme.salary.insights.dto;

import java.math.BigDecimal;

/** Headline numbers for the dashboard: {@code GET /api/insights/summary}. Everything in USD,
 *  since headcount and payroll are meant to be read across all countries at once. */
public record SummaryResponse(int headcount, BigDecimal totalPayrollUsd, BigDecimal medianSalaryUsd) {
}
