package com.acme.salary.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One row of an employee's salary history, as returned by the API. */
public record SalaryHistoryResponse(
        Long id,
        BigDecimal oldSalary,
        BigDecimal newSalary,
        String currencyCode,
        LocalDate effectiveDate,
        String reason,
        LocalDateTime createdAt,
        /** Null for the initial hire record, since there is no "old" salary to compare against. */
        BigDecimal percentChange) {
}
