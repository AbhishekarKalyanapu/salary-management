package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.salary.domain.EmployeeInput;
import com.acme.salary.domain.EmployeeValidator;
import com.acme.salary.domain.FxConverter;
import com.acme.salary.domain.OutlierDetector;
import com.acme.salary.domain.OutlierDetector.SalaryRecord;
import com.acme.salary.domain.Percentiles;
import com.acme.salary.domain.ValidationError;
import com.acme.salary.seed.EmployeeGenerator.CountryRef;
import com.acme.salary.seed.EmployeeGenerator.GeneratedEmployee;
import com.acme.salary.seed.EmployeeGenerator.JobTitleRef;
import com.acme.salary.seed.EmployeeGenerator.SalaryHistoryEntry;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmployeeGeneratorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 1);
    private static final int SOFTWARE_ENGINEER = 1;
    private static final int DIRECTOR_OF_PRODUCT = 2;
    private static final int RECRUITER = 3;

    private final FxConverter fx = new FxConverter(Map.of(
            "USD", new BigDecimal("1"),
            "EUR", new BigDecimal("1.09"),
            "INR", new BigDecimal("0.012")));

    private final List<CountryRef> countries = List.of(
            new CountryRef("US", "USD"), new CountryRef("DE", "EUR"), new CountryRef("IN", "INR"));

    private final List<JobTitleRef> titles = List.of(
            new JobTitleRef(SOFTWARE_ENGINEER, "Software Engineer", 1),
            new JobTitleRef(DIRECTOR_OF_PRODUCT, "Director of Product", 2),
            new JobTitleRef(RECRUITER, "Recruiter", 3));

    private EmployeeGenerator generator(long seed) {
        return new EmployeeGenerator(fx, TODAY, seed, countries, titles);
    }

    private List<GeneratedEmployee> generate(int count) {
        return generator(42).generate(count);
    }

    @Test
    void producesTheRequestedNumberOfEmployees() {
        assertThat(generate(500)).hasSize(500);
    }

    @Test
    void sameSeedProducesIdenticalData() {
        assertThat(generator(7).generate(300)).isEqualTo(generator(7).generate(300));
    }

    @Test
    void differentSeedsProduceDifferentData() {
        assertThat(generator(1).generate(300)).isNotEqualTo(generator(2).generate(300));
    }

    @Test
    void everyGeneratedEmployeePassesTheEmployeeValidator() {
        EmployeeValidator validator = new EmployeeValidator(
                Clock.fixed(Instant.parse("2026-09-01T00:00:00Z"), ZoneOffset.UTC));
        List<ValidationError> errors = new ArrayList<>();
        for (GeneratedEmployee g : generate(2000)) {
            errors.addAll(validator.validate(g.employee()));
        }

        assertThat(errors).isEmpty();
    }

    @Test
    void emailsAreUnique() {
        List<String> emails = generate(3000).stream().map(g -> g.employee().email()).toList();

        assertThat(emails.stream().distinct().count()).isEqualTo(emails.size());
    }

    @Test
    void levelsStayWithinTheRangeForTheirJobTitle() {
        for (GeneratedEmployee g : generate(2000)) {
            EmployeeInput e = g.employee();
            if (e.jobTitleId() == DIRECTOR_OF_PRODUCT) {
                assertThat(e.level() >= 5 && e.level() <= 6).isTrue();
            }
            if (e.jobTitleId() == RECRUITER) {
                assertThat(e.level() >= 1 && e.level() <= 4).isTrue();
            }
        }
    }

    @Test
    void hireDatesFallBetweenTheFirstHireAndThirtyDaysAgo() {
        for (GeneratedEmployee g : generate(1000)) {
            LocalDate hired = g.employee().hireDate();
            assertThat(!hired.isBefore(EmployeeGenerator.EARLIEST_HIRE)).isTrue();
            assertThat(!hired.isAfter(TODAY.minusDays(30))).isTrue();
        }
    }

    @Test
    void salariesAreRoundedToAHumanAmountInTheirCurrency() {
        for (GeneratedEmployee g : generate(1000)) {
            BigDecimal step = "INR".equals(g.employee().currencyCode()) ? BigDecimal.valueOf(1000) : BigDecimal.valueOf(100);
            assertThat(g.employee().salary().remainder(step)).isEqualByComparingTo("0");
        }
    }

    @Test
    void payReflectsCountryAndSeniority() {
        List<GeneratedEmployee> all = generate(6000);

        BigDecimal usEngineers = medianUsd(all, "US", SOFTWARE_ENGINEER);
        BigDecimal indiaEngineers = medianUsd(all, "IN", SOFTWARE_ENGINEER);
        BigDecimal usDirectors = medianUsd(all, "US", DIRECTOR_OF_PRODUCT);
        BigDecimal usRecruiters = medianUsd(all, "US", RECRUITER);

        assertThat(usEngineers.compareTo(indiaEngineers) > 0).isTrue();
        assertThat(usDirectors.compareTo(usRecruiters) > 0).isTrue();
    }

    @Test
    void historyStartsAtHireAndChainsUpToTheCurrentSalary() {
        for (GeneratedEmployee g : generate(1000)) {
            List<SalaryHistoryEntry> history = g.history();
            SalaryHistoryEntry first = history.get(0);
            SalaryHistoryEntry last = history.get(history.size() - 1);

            assertThat(first.oldSalary() == null).isTrue();
            assertThat(first.effectiveDate()).isEqualTo(g.employee().hireDate());
            assertThat(last.newSalary()).isEqualByComparingTo(g.employee().salary().toPlainString());
            assertThat(history.size() >= 1 && history.size() <= 1 + EmployeeGenerator.MAX_RAISES).isTrue();

            for (int i = 1; i < history.size(); i++) {
                SalaryHistoryEntry previous = history.get(i - 1);
                SalaryHistoryEntry current = history.get(i);
                assertThat(current.oldSalary()).isEqualByComparingTo(previous.newSalary().toPlainString());
                assertThat(current.newSalary().compareTo(current.oldSalary()) > 0).isTrue();
                assertThat(current.effectiveDate().isAfter(previous.effectiveDate())).isTrue();
                assertThat(!current.effectiveDate().isAfter(TODAY)).isTrue();
            }
        }
    }

    @Test
    void includesAFewDeliberateOutliersButNotMany() {
        List<GeneratedEmployee> all = generate(6000);
        List<SalaryRecord> records = new ArrayList<>();
        long id = 1;
        for (GeneratedEmployee g : all) {
            EmployeeInput e = g.employee();
            records.add(new SalaryRecord(id++, e.countryCode(), e.jobTitleId(), e.salary()));
        }

        List<OutlierDetector.Outlier> outliers = new OutlierDetector()
                .detect(records, new BigDecimal("45"), OutlierDetector.DEFAULT_MIN_GROUP_SIZE);

        assertThat(outliers.isEmpty()).isFalse();
        assertThat(outliers.size() < all.size() / 10).isTrue();
    }

    @Test
    void rejectsJobTitlesAndCountriesWithoutASeedSpec() {
        assertThatThrownBy(() -> new EmployeeGenerator(fx, TODAY, 1, countries,
                List.of(new JobTitleRef(9, "Astronaut", 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Astronaut");
        assertThatThrownBy(() -> new EmployeeGenerator(fx, TODAY, 1,
                List.of(new CountryRef("ZZ", "USD")), titles))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ZZ");
    }

    @Test
    void rejectsANonPositiveCount() {
        assertThatThrownBy(() -> generator(1).generate(0)).isInstanceOf(IllegalArgumentException.class);
    }

    private BigDecimal medianUsd(List<GeneratedEmployee> all, String country, int jobTitleId) {
        List<BigDecimal> usd = all.stream()
                .map(GeneratedEmployee::employee)
                .filter(e -> e.countryCode().equals(country) && e.jobTitleId() == jobTitleId)
                .map(e -> fx.toUsd(e.salary(), e.currencyCode()))
                .toList();
        return Percentiles.median(usd);
    }
}
