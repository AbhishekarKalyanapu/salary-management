package com.acme.salary.domain;

import java.math.BigDecimal;
import java.util.Optional;

/** Rules that apply to any salary amount, shared by employee and salary-change validation. */
final class SalaryRules {

    static final BigDecimal MAX_SALARY = new BigDecimal("10000000000");

    private SalaryRules() {
    }

    /** @return the violation message, or empty if the amount is acceptable */
    static Optional<String> violation(BigDecimal salary) {
        if (salary == null) {
            return Optional.of("is required");
        }
        if (salary.signum() <= 0) {
            return Optional.of("must be greater than zero");
        }
        if (salary.compareTo(MAX_SALARY) > 0) {
            return Optional.of("must not exceed " + MAX_SALARY.toPlainString());
        }
        if (salary.stripTrailingZeros().scale() > 2) {
            return Optional.of("must have at most 2 decimal places");
        }
        return Optional.empty();
    }
}
