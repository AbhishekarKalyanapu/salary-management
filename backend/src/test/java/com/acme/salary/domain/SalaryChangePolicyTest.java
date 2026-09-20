package com.acme.salary.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.domain.SalaryChangePolicy.ProposedChange;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class SalaryChangePolicyTest {

    private static final BigDecimal CURRENT = new BigDecimal("100000");
    private static final LocalDate HIRED = LocalDate.of(2020, 1, 15);

    private final SalaryChangePolicy policy = new SalaryChangePolicy();

    private List<ValidationError> validate(String newSalary, LocalDate effective, String reason) {
        return policy.validate(CURRENT, HIRED, new ProposedChange(new BigDecimal(newSalary), effective, reason));
    }

    @Test
    void acceptsARaise() {
        assertThat(validate("110000", LocalDate.of(2026, 1, 1), "Annual review")).isEmpty();
    }

    @Test
    void acceptsAnEffectiveDateEqualToTheHireDate() {
        assertThat(validate("110000", HIRED, null)).isEmpty();
    }

    @Test
    void rejectsAChangeToTheSameAmountEvenIfScaleDiffers() {
        assertThat(validate("100000.00", LocalDate.of(2026, 1, 1), null))
                .extracting(ValidationError::field).containsExactly("newSalary");
    }

    @Test
    void rejectsNonPositiveNewSalary() {
        assertThat(validate("0", LocalDate.of(2026, 1, 1), null))
                .extracting(ValidationError::field).containsExactly("newSalary");
    }

    @Test
    void rejectsEffectiveDateBeforeHireDate() {
        assertThat(validate("110000", HIRED.minusDays(1), null))
                .extracting(ValidationError::field).containsExactly("effectiveDate");
    }

    @Test
    void requiresAnEffectiveDate() {
        assertThat(validate("110000", null, null))
                .extracting(ValidationError::field).containsExactly("effectiveDate");
    }

    @Test
    void limitsReasonLength() {
        assertThat(validate("110000", LocalDate.of(2026, 1, 1), "x".repeat(255))).isEmpty();
        assertThat(validate("110000", LocalDate.of(2026, 1, 1), "x".repeat(256)))
                .extracting(ValidationError::field).containsExactly("reason");
    }

    @Test
    void computesSignedPercentChange() {
        assertThat(SalaryChangePolicy.percentChange(CURRENT, new BigDecimal("110000"))).isEqualByComparingTo("10.00");
        assertThat(SalaryChangePolicy.percentChange(CURRENT, new BigDecimal("90000"))).isEqualByComparingTo("-10.00");
    }
}
