package com.acme.salary.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmployeeValidatorTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-20T10:00:00Z"), ZoneOffset.UTC);
    private final EmployeeValidator validator = new EmployeeValidator(clock);

    /** Starts as a valid employee; each test breaks exactly one thing. */
    private static final class Builder {
        String firstName = "Asha";
        String lastName = "Rao";
        String email = "asha.rao@acme.example";
        String countryCode = "IN";
        String currencyCode = "INR";
        Integer departmentId = 1;
        Integer jobTitleId = 2;
        Integer level = 3;
        LocalDate hireDate = LocalDate.of(2020, 1, 15);
        BigDecimal salary = new BigDecimal("1500000");

        EmployeeInput build() {
            return new EmployeeInput(firstName, lastName, email, countryCode, currencyCode,
                    departmentId, jobTitleId, level, hireDate, salary);
        }
    }

    private List<ValidationError> validate(Builder b) {
        return validator.validate(b.build());
    }

    @Test
    void acceptsAValidEmployee() {
        assertThat(validate(new Builder())).isEmpty();
    }

    @Test
    void reportsEveryMissingFieldAtOnce() {
        EmployeeInput empty = new EmployeeInput(null, null, null, null, null, null, null, null, null, null);

        assertThat(validator.validate(empty)).extracting(ValidationError::field).containsExactlyInAnyOrder(
                "firstName", "lastName", "email", "countryCode", "currencyCode",
                "departmentId", "jobTitleId", "level", "hireDate", "salary");
    }

    @Test
    void rejectsBlankAndOverlongNames() {
        Builder b = new Builder();
        b.firstName = "   ";
        b.lastName = "x".repeat(101);

        assertThat(validate(b)).extracting(ValidationError::field).containsExactly("firstName", "lastName");
    }

    @Test
    void rejectsMalformedEmail() {
        Builder b = new Builder();
        b.email = "not-an-email";

        assertThat(validate(b)).extracting(ValidationError::field).containsExactly("email");
    }

    @Test
    void rejectsLevelsOutsideTheScale() {
        for (int badLevel : new int[] {0, 7}) {
            Builder b = new Builder();
            b.level = badLevel;
            assertThat(validate(b)).extracting(ValidationError::field).containsExactly("level");
        }
    }

    @Test
    void rejectsMalformedCountryAndCurrencyCodes() {
        Builder b = new Builder();
        b.countryCode = "india";
        b.currencyCode = "IN";

        assertThat(validate(b)).extracting(ValidationError::field).containsExactly("countryCode", "currencyCode");
    }

    @Test
    void rejectsNonPositiveSalary() {
        for (String bad : new String[] {"0", "-1"}) {
            Builder b = new Builder();
            b.salary = new BigDecimal(bad);
            assertThat(validate(b)).extracting(ValidationError::field).containsExactly("salary");
        }
    }

    @Test
    void rejectsSalaryBeyondTheUpperBound() {
        Builder b = new Builder();
        b.salary = new BigDecimal("10000000000.01");

        assertThat(validate(b)).extracting(ValidationError::field).containsExactly("salary");
    }

    @Test
    void rejectsMoreThanTwoDecimalPlacesButIgnoresTrailingZeros() {
        Builder tooPrecise = new Builder();
        tooPrecise.salary = new BigDecimal("100000.001");
        Builder trailingZeros = new Builder();
        trailingZeros.salary = new BigDecimal("100000.100");

        assertThat(validate(tooPrecise)).extracting(ValidationError::field).containsExactly("salary");
        assertThat(validate(trailingZeros)).isEmpty();
    }

    @Test
    void allowsHiringToday() {
        Builder b = new Builder();
        b.hireDate = LocalDate.of(2026, 9, 20);

        assertThat(validate(b)).isEmpty();
    }

    @Test
    void rejectsHireDateInTheFuture() {
        Builder b = new Builder();
        b.hireDate = LocalDate.of(2026, 9, 21);

        assertThat(validate(b)).extracting(ValidationError::field).containsExactly("hireDate");
    }
}
