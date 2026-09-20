package com.acme.salary.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * What the client sends to create an employee. Includes the starting salary, since a new
 * hire needs one; subsequent salary changes go through {@code POST /employees/{id}/salary},
 * never through {@link EmployeeUpdateRequest}.
 */
public record EmployeeCreateRequest(
        String firstName,
        String lastName,
        String email,
        String countryCode,
        String currencyCode,
        Integer departmentId,
        Integer jobTitleId,
        Integer level,
        LocalDate hireDate,
        BigDecimal salary) {
}
