package com.acme.salary.seed;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Parameters that shape the synthetic data: how many people work in each country and job,
 * and what they roughly earn. Only demo data depends on this; the reference rows themselves
 * (countries, departments, job titles) come from the V2 Flyway migration, and a test keeps
 * the two in sync.
 */
public final class SeedCatalog {

    /**
     * @param payIndex pay level relative to the US (1.00 = same as US, in USD terms)
     * @param weight   relative share of employees located in this country
     */
    public record CountrySpec(String code, double payIndex, int weight) {
    }

    /**
     * @param entryUsd salary in USD (US pay level) at {@code minLevel}; each level above adds a fixed step
     * @param weight   relative share of employees holding this job title
     */
    public record TitleSpec(String title, String department, int entryUsd, int minLevel, int maxLevel, int weight) {
    }

    public static final List<CountrySpec> COUNTRIES = List.of(
            new CountrySpec("US", 1.00, 30),
            new CountrySpec("IN", 0.28, 20),
            new CountrySpec("GB", 0.72, 10),
            new CountrySpec("DE", 0.75, 10),
            new CountrySpec("BR", 0.35, 9),
            new CountrySpec("CA", 0.80, 8),
            new CountrySpec("AU", 0.78, 7),
            new CountrySpec("SG", 0.70, 6));

    public static final List<TitleSpec> TITLES = List.of(
            new TitleSpec("Software Engineer", "Engineering", 85000, 1, 5, 24),
            new TitleSpec("QA Engineer", "Engineering", 65000, 1, 5, 8),
            new TitleSpec("DevOps Engineer", "Engineering", 90000, 1, 5, 7),
            new TitleSpec("Engineering Manager", "Engineering", 150000, 4, 6, 4),
            new TitleSpec("Security Engineer", "Engineering", 95000, 1, 5, 4),
            new TitleSpec("Product Manager", "Product", 95000, 2, 5, 6),
            new TitleSpec("Technical Program Manager", "Product", 100000, 2, 5, 3),
            new TitleSpec("Business Analyst", "Product", 62000, 1, 4, 4),
            new TitleSpec("Product Operations Specialist", "Product", 58000, 1, 3, 2),
            new TitleSpec("Director of Product", "Product", 175000, 5, 6, 1),
            new TitleSpec("UX Designer", "Design", 70000, 1, 5, 4),
            new TitleSpec("UI Designer", "Design", 66000, 1, 5, 3),
            new TitleSpec("UX Researcher", "Design", 70000, 1, 5, 2),
            new TitleSpec("Design Manager", "Design", 135000, 4, 6, 1),
            new TitleSpec("Content Designer", "Design", 62000, 1, 4, 2),
            new TitleSpec("Data Analyst", "Data & Analytics", 62000, 1, 4, 5),
            new TitleSpec("Data Scientist", "Data & Analytics", 95000, 1, 5, 4),
            new TitleSpec("Data Engineer", "Data & Analytics", 90000, 1, 5, 5),
            new TitleSpec("BI Developer", "Data & Analytics", 66000, 1, 4, 3),
            new TitleSpec("ML Engineer", "Data & Analytics", 105000, 2, 5, 3),
            new TitleSpec("Account Executive", "Sales", 80000, 1, 5, 7),
            new TitleSpec("Sales Development Representative", "Sales", 45000, 1, 2, 5),
            new TitleSpec("Sales Manager", "Sales", 120000, 4, 6, 2),
            new TitleSpec("Solutions Consultant", "Sales", 85000, 2, 5, 3),
            new TitleSpec("Customer Success Manager", "Sales", 68000, 1, 5, 4),
            new TitleSpec("Marketing Manager", "Marketing", 90000, 3, 5, 2),
            new TitleSpec("Content Marketer", "Marketing", 52000, 1, 4, 3),
            new TitleSpec("SEO Specialist", "Marketing", 50000, 1, 4, 2),
            new TitleSpec("Brand Manager", "Marketing", 85000, 3, 5, 1),
            new TitleSpec("Growth Marketer", "Marketing", 62000, 1, 5, 2),
            new TitleSpec("Accountant", "Finance", 52000, 1, 4, 3),
            new TitleSpec("Financial Analyst", "Finance", 62000, 1, 5, 3),
            new TitleSpec("Controller", "Finance", 125000, 5, 6, 1),
            new TitleSpec("Payroll Specialist", "Finance", 50000, 1, 4, 1),
            new TitleSpec("Procurement Specialist", "Finance", 52000, 1, 4, 1),
            new TitleSpec("Recruiter", "Human Resources", 52000, 1, 4, 3),
            new TitleSpec("HR Business Partner", "Human Resources", 70000, 2, 5, 2),
            new TitleSpec("Compensation Analyst", "Human Resources", 65000, 2, 5, 1),
            new TitleSpec("Talent Acquisition Manager", "Human Resources", 95000, 4, 6, 1),
            new TitleSpec("HR Coordinator", "Human Resources", 40000, 1, 2, 1));

    public static final List<String> FIRST_NAMES = List.of(
            "Aarav",
            "Priya",
            "Ananya",
            "Rahul",
            "Sneha",
            "Vikram",
            "Meera",
            "Arjun",
            "Emma",
            "Liam",
            "Olivia",
            "Noah",
            "Sophia",
            "James",
            "Charlotte",
            "Oliver",
            "Hannah",
            "Lukas",
            "Mia",
            "Felix",
            "Lena",
            "Jonas",
            "Isabella",
            "Mateus",
            "Camila",
            "Lucas",
            "Beatriz",
            "Gabriel",
            "Chloe",
            "Ethan",
            "Priyanka",
            "Wei",
            "Mei",
            "Jun",
            "Hui",
            "Daniel",
            "Sara",
            "Omar",
            "Fatima",
            "Kenji");

    public static final List<String> LAST_NAMES = List.of(
            "Sharma",
            "Rao",
            "Patel",
            "Reddy",
            "Iyer",
            "Nair",
            "Gupta",
            "Singh",
            "Smith",
            "Johnson",
            "Brown",
            "Taylor",
            "Williams",
            "Jones",
            "Davies",
            "Wilson",
            "Mueller",
            "Schmidt",
            "Fischer",
            "Weber",
            "Wagner",
            "Becker",
            "Silva",
            "Santos",
            "Oliveira",
            "Souza",
            "Costa",
            "Pereira",
            "Tan",
            "Lim",
            "Wong",
            "Lee",
            "Chen",
            "Ng",
            "Nguyen",
            "Kim",
            "Martin",
            "Roy",
            "Clarke",
            "Evans");

    private static final Map<String, CountrySpec> COUNTRY_BY_CODE =
            COUNTRIES.stream().collect(Collectors.toUnmodifiableMap(CountrySpec::code, Function.identity()));
    private static final Map<String, TitleSpec> TITLE_BY_NAME =
            TITLES.stream().collect(Collectors.toUnmodifiableMap(TitleSpec::title, Function.identity()));

    private SeedCatalog() {
    }

    public static Optional<CountrySpec> country(String code) {
        return Optional.ofNullable(COUNTRY_BY_CODE.get(code));
    }

    public static Optional<TitleSpec> title(String title) {
        return Optional.ofNullable(TITLE_BY_NAME.get(title));
    }
}
