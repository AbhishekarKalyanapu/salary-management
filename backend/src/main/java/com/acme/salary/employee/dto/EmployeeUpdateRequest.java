package com.acme.salary.employee.dto;

import java.time.LocalDate;

/**
 * What the client sends to edit an employee's non-pay details. Salary is deliberately absent:
 * per DESIGN.md, the only path that changes {@code employee.salary} is the dedicated
 * salary-change endpoint, so that every change is recorded in {@code salary_history}.
 */
public record EmployeeUpdateRequest(
        String firstName,
        String lastName,
        String email,
        String countryCode,
        Integer departmentId,
        Integer jobTitleId,
        Integer level,
        LocalDate hireDate) {
}
