package com.acme.salary.seed;

import com.acme.salary.domain.EmployeeInput;
import com.acme.salary.domain.FxConverter;
import com.acme.salary.seed.SeedCatalog.CountrySpec;
import com.acme.salary.seed.SeedCatalog.TitleSpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

/**
 * Generates realistic, fully deterministic employees: the same seed, reference data and
 * date always produce exactly the same output. It performs no I/O, so it is unit-testable.
 *
 * <p>Salary = title entry salary x level step x country pay index x noise, converted to
 * local currency and rounded to a "human" amount. A small share of people are deliberately
 * paid far above or below their peers so the outlier view has something to find.
 */
public final class EmployeeGenerator {

    static final double LEVEL_STEP = 0.18;          // each level above the title's minimum adds 18%
    static final double NOISE_SIGMA = 0.08;         // log-normal spread, roughly +/- 8%
    static final double OUTLIER_RATE = 0.015;
    static final int[] LEVEL_WEIGHTS = {20, 25, 28, 15, 8, 4};   // index = level - 1
    static final LocalDate EARLIEST_HIRE = LocalDate.of(2012, 1, 1);
    static final int MIN_TENURE_DAYS = 30;
    static final int MAX_RAISES = 3;
    static final double MIN_RAISE = 0.02;
    static final double MAX_RAISE = 0.09;
    static final String EMAIL_DOMAIN = "@acme.example";

    public record CountryRef(String code, String currencyCode) {
    }

    public record JobTitleRef(int id, String title, int departmentId) {
    }

    /** {@code oldSalary} is null for the initial record. */
    public record SalaryHistoryEntry(BigDecimal oldSalary, BigDecimal newSalary,
                                     LocalDate effectiveDate, String reason) {
    }

    public record GeneratedEmployee(EmployeeInput employee, List<SalaryHistoryEntry> history) {
    }

    private record Country(CountryRef ref, CountrySpec spec) {
    }

    private record Title(JobTitleRef ref, TitleSpec spec) {
    }

    private final FxConverter fx;
    private final LocalDate today;
    private final long randomSeed;
    private final List<Country> countries;
    private final List<Title> titles;

    /**
     * @param today reference date for hire dates and history; pass a fixed date for reproducible output
     */
    public EmployeeGenerator(FxConverter fx, LocalDate today, long randomSeed,
                             List<CountryRef> countries, List<JobTitleRef> titles) {
        if (countries.isEmpty() || titles.isEmpty()) {
            throw new IllegalArgumentException("countries and job titles must not be empty");
        }
        if (ChronoUnit.DAYS.between(EARLIEST_HIRE, today.minusDays(MIN_TENURE_DAYS)) <= 0) {
            throw new IllegalArgumentException("today is too early to generate hire dates");
        }
        this.fx = fx;
        this.today = today;
        this.randomSeed = randomSeed;
        this.countries = countries.stream()
                .map(c -> new Country(c, SeedCatalog.country(c.code()).orElseThrow(
                        () -> new IllegalStateException("No seed spec for country: " + c.code()))))
                .toList();
        this.titles = titles.stream()
                .map(t -> new Title(t, SeedCatalog.title(t.title()).orElseThrow(
                        () -> new IllegalStateException("No seed spec for job title: " + t.title()))))
                .toList();
    }

    public List<GeneratedEmployee> generate(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        Random rng = new Random(randomSeed);
        Set<String> usedEmails = new HashSet<>();
        List<GeneratedEmployee> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(generateOne(rng, usedEmails));
        }
        return result;
    }

    private GeneratedEmployee generateOne(Random rng, Set<String> usedEmails) {
        Country country = pickWeighted(rng, countries, c -> c.spec().weight());
        Title title = pickWeighted(rng, titles, t -> t.spec().weight());
        int level = pickLevel(rng, title.spec());
        String firstName = SeedCatalog.FIRST_NAMES.get(rng.nextInt(SeedCatalog.FIRST_NAMES.size()));
        String lastName = SeedCatalog.LAST_NAMES.get(rng.nextInt(SeedCatalog.LAST_NAMES.size()));
        LocalDate hireDate = pickHireDate(rng);
        BigDecimal salary = currentSalary(rng, country, title.spec(), level);
        List<SalaryHistoryEntry> history = buildHistory(rng, hireDate, salary);

        EmployeeInput employee = new EmployeeInput(
                firstName, lastName, uniqueEmail(firstName, lastName, usedEmails),
                country.ref().code(), country.ref().currencyCode(),
                title.ref().departmentId(), title.ref().id(), level, hireDate, salary);
        return new GeneratedEmployee(employee, history);
    }

    private static <T> T pickWeighted(Random rng, List<T> items, ToIntFunction<T> weight) {
        int total = items.stream().mapToInt(weight).sum();
        int roll = rng.nextInt(total);
        for (T item : items) {
            roll -= weight.applyAsInt(item);
            if (roll < 0) {
                return item;
            }
        }
        throw new IllegalStateException("unreachable: weights exhausted");
    }

    private static int pickLevel(Random rng, TitleSpec spec) {
        List<Integer> levels = IntStream.rangeClosed(spec.minLevel(), spec.maxLevel()).boxed().toList();
        return pickWeighted(rng, levels, level -> LEVEL_WEIGHTS[level - 1]);
    }

    private LocalDate pickHireDate(Random rng) {
        long span = ChronoUnit.DAYS.between(EARLIEST_HIRE, today.minusDays(MIN_TENURE_DAYS));
        return EARLIEST_HIRE.plusDays(rng.nextInt((int) span));
    }

    private BigDecimal currentSalary(Random rng, Country country, TitleSpec title, int level) {
        double levelFactor = 1 + LEVEL_STEP * (level - title.minLevel());
        // StrictMath: identical results on every JVM and platform, so seeded data never drifts
        double noise = Math.min(1.35, Math.max(0.75, StrictMath.exp(rng.nextGaussian() * NOISE_SIGMA)));
        double usd = title.entryUsd() * levelFactor * country.spec().payIndex() * noise * outlierFactor(rng);

        String currency = country.ref().currencyCode();
        BigDecimal local = fx.convert(BigDecimal.valueOf(usd).setScale(2, RoundingMode.HALF_UP), "USD", currency);
        return roundToStep(local, BigDecimal.valueOf(stepFor(currency)));
    }

    private static double outlierFactor(Random rng) {
        if (rng.nextDouble() >= OUTLIER_RATE) {
            return 1.0;
        }
        return rng.nextBoolean() ? 1.7 + rng.nextDouble() * 0.3 : 0.5 + rng.nextDouble() * 0.1;
    }

    private static int stepFor(String currency) {
        return "INR".equals(currency) ? 1000 : 100;
    }

    private static BigDecimal roundToStep(BigDecimal amount, BigDecimal step) {
        return amount.divide(step, 0, RoundingMode.HALF_UP).multiply(step).max(step).setScale(2, RoundingMode.UNNECESSARY);
    }

    /** Works backwards from the current salary so the history always ends at exactly that amount. */
    private List<SalaryHistoryEntry> buildHistory(Random rng, LocalDate hireDate, BigDecimal currentSalary) {
        long tenureYears = ChronoUnit.YEARS.between(hireDate, today);
        int raises = (int) Math.min(tenureYears, rng.nextInt(MAX_RAISES + 1));

        BigDecimal[] salaries = new BigDecimal[raises + 1];
        salaries[raises] = currentSalary;
        for (int i = raises; i >= 1; i--) {
            double raise = MIN_RAISE + rng.nextDouble() * (MAX_RAISE - MIN_RAISE);
            salaries[i - 1] = salaries[i].divide(BigDecimal.valueOf(1 + raise), 2, RoundingMode.HALF_UP);
        }

        List<SalaryHistoryEntry> history = new ArrayList<>();
        history.add(new SalaryHistoryEntry(null, salaries[0], hireDate, "Initial salary"));
        for (int i = 1; i <= raises; i++) {
            history.add(new SalaryHistoryEntry(salaries[i - 1], salaries[i], hireDate.plusYears(i), "Annual review"));
        }
        return List.copyOf(history);
    }

    private static String uniqueEmail(String firstName, String lastName, Set<String> used) {
        String base = (firstName + "." + lastName).toLowerCase(Locale.ROOT);
        String email = base + EMAIL_DOMAIN;
        int suffix = 2;
        while (!used.add(email)) {
            email = base + suffix++ + EMAIL_DOMAIN;
        }
        return email;
    }
}
