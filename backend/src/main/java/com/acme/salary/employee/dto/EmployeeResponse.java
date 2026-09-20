package com.acme.salary.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

/** What the API returns for an employee. Flattens the entity's relationships to plain fields. */
public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String countryCode,
        String countryName,
        Integer departmentId,
        String departmentName,
        Integer jobTitleId,
        String jobTitleTitle,
        Integer level,
        LocalDate hireDate,
        BigDecimal salary,
        String currencyCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String rowVersion) {

    /** Base64-encodes the raw ROWVERSION bytes so they travel safely as a JSON string. */
    public static String encodeVersion(byte[] rowVersion) {
        return rowVersion == null ? null : Base64.getEncoder().encodeToString(rowVersion);
    }
}
