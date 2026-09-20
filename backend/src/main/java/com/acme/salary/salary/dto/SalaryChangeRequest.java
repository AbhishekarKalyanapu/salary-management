package com.acme.salary.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** What the client sends to change an employee's salary. Currency is not included: a salary
 *  change never moves an employee to a different currency, only their pay within it. */
public record SalaryChangeRequest(BigDecimal newSalary, LocalDate effectiveDate, String reason) {
}
