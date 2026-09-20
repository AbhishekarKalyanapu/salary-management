package com.acme.salary.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Raw, unvalidated employee data. Wrapper types so that missing values can be reported. */
public record EmployeeInput(String firstName, String lastName, String email,
                            String countryCode, String currencyCode,
                            Integer departmentId, Integer jobTitleId, Integer level,
                            LocalDate hireDate, BigDecimal salary) {
}
