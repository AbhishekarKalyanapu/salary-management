package com.acme.salary.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Rules for changing an existing employee's salary. */
public final class SalaryChangePolicy {

    public static final int MAX_REASON_LENGTH = 255;

    public record ProposedChange(BigDecimal newSalary, LocalDate effectiveDate, String reason) {
    }

    public List<ValidationError> validate(BigDecimal currentSalary, LocalDate hireDate, ProposedChange change) {
        List<ValidationError> errors = new ArrayList<>();

        SalaryRules.violation(change.newSalary())
                .ifPresent(msg -> errors.add(new ValidationError("newSalary", msg)));
        if (change.newSalary() != null && change.newSalary().compareTo(currentSalary) == 0) {
            errors.add(new ValidationError("newSalary", "must differ from the current salary"));
        }

        if (change.effectiveDate() == null) {
            errors.add(new ValidationError("effectiveDate", "is required"));
        } else if (change.effectiveDate().isBefore(hireDate)) {
            errors.add(new ValidationError("effectiveDate", "must not be before the hire date"));
        }

        if (change.reason() != null && change.reason().length() > MAX_REASON_LENGTH) {
            errors.add(new ValidationError("reason", "must be at most " + MAX_REASON_LENGTH + " characters"));
        }
        return errors;
    }

    /** Signed percentage change, e.g. 100000 to 110000 is 10.00. */
    public static BigDecimal percentChange(BigDecimal oldSalary, BigDecimal newSalary) {
        return newSalary.subtract(oldSalary).multiply(BigDecimal.valueOf(100))
                .divide(oldSalary, 2, RoundingMode.HALF_UP);
    }
}
