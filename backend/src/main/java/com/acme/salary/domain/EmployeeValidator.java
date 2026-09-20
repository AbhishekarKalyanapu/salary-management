package com.acme.salary.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Structural validation of employee data. Checks that need the database (does this country
 * exist? is this email already taken?) belong to the service layer, not here.
 */
public final class EmployeeValidator {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 6;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern COUNTRY = Pattern.compile("^[A-Z]{2}$");
    private static final Pattern CURRENCY = Pattern.compile("^[A-Z]{3}$");

    private final Clock clock;

    public EmployeeValidator(Clock clock) {
        this.clock = clock;
    }

    public List<ValidationError> validate(EmployeeInput in) {
        List<ValidationError> errors = new ArrayList<>();

        checkName(errors, "firstName", in.firstName());
        checkName(errors, "lastName", in.lastName());
        checkEmail(errors, in.email());
        checkPattern(errors, "countryCode", in.countryCode(), COUNTRY, "must be a 2-letter uppercase ISO code");
        checkPattern(errors, "currencyCode", in.currencyCode(), CURRENCY, "must be a 3-letter uppercase ISO code");
        checkId(errors, "departmentId", in.departmentId());
        checkId(errors, "jobTitleId", in.jobTitleId());
        checkLevel(errors, in.level());
        checkHireDate(errors, in.hireDate());
        SalaryRules.violation(in.salary()).ifPresent(msg -> errors.add(new ValidationError("salary", msg)));

        return errors;
    }

    private static void checkName(List<ValidationError> errors, String field, String value) {
        if (value == null || value.isBlank()) {
            errors.add(new ValidationError(field, "is required"));
        } else if (value.length() > 100) {
            errors.add(new ValidationError(field, "must be at most 100 characters"));
        }
    }

    private static void checkEmail(List<ValidationError> errors, String email) {
        if (email == null || email.isBlank()) {
            errors.add(new ValidationError("email", "is required"));
        } else if (email.length() > 255) {
            errors.add(new ValidationError("email", "must be at most 255 characters"));
        } else if (!EMAIL.matcher(email).matches()) {
            errors.add(new ValidationError("email", "must be a valid email address"));
        }
    }

    private static void checkPattern(List<ValidationError> errors, String field, String value,
                                     Pattern pattern, String message) {
        if (value == null || value.isBlank()) {
            errors.add(new ValidationError(field, "is required"));
        } else if (!pattern.matcher(value).matches()) {
            errors.add(new ValidationError(field, message));
        }
    }

    private static void checkId(List<ValidationError> errors, String field, Integer id) {
        if (id == null) {
            errors.add(new ValidationError(field, "is required"));
        } else if (id <= 0) {
            errors.add(new ValidationError(field, "must be a positive id"));
        }
    }

    private static void checkLevel(List<ValidationError> errors, Integer level) {
        if (level == null) {
            errors.add(new ValidationError("level", "is required"));
        } else if (level < MIN_LEVEL || level > MAX_LEVEL) {
            errors.add(new ValidationError("level", "must be between " + MIN_LEVEL + " and " + MAX_LEVEL));
        }
    }

    private void checkHireDate(List<ValidationError> errors, LocalDate hireDate) {
        if (hireDate == null) {
            errors.add(new ValidationError("hireDate", "is required"));
        } else if (hireDate.isAfter(LocalDate.now(clock))) {
            errors.add(new ValidationError("hireDate", "must not be in the future"));
        }
    }
}
