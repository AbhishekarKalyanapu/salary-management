package com.acme.salary.seed;

import com.acme.salary.domain.EmployeeInput;
import com.acme.salary.domain.FxConverter;
import com.acme.salary.seed.EmployeeGenerator.CountryRef;
import com.acme.salary.seed.EmployeeGenerator.GeneratedEmployee;
import com.acme.salary.seed.EmployeeGenerator.JobTitleRef;
import com.acme.salary.seed.EmployeeGenerator.SalaryHistoryEntry;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Loads demo employees into the database. Runs only with the "seed" profile, as a
 * one-shot job (no web server), and never touches existing data unless told to reset.
 *
 * <pre>
 * mvn spring-boot:run "-Dspring-boot.run.profiles=seed"
 * mvn spring-boot:run "-Dspring-boot.run.profiles=seed" "-Dspring-boot.run.arguments=--app.seed.reset=true"
 * </pre>
 */
@Component
@Profile("seed")
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);
    private static final int BATCH_SIZE = 1000;

    private static final String INSERT_EMPLOYEE =
            "INSERT INTO employee (first_name, last_name, email, country_code, department_id, "
                    + "job_title_id, level, hire_date, salary, currency_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_HISTORY =
            "INSERT INTO salary_history (employee_id, old_salary, new_salary, currency_code, effective_date, reason) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private record HistoryRow(long employeeId, String currencyCode, SalaryHistoryEntry entry) {
    }

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final int count;
    private final long randomSeed;
    private final LocalDate asOf;
    private final boolean reset;

    public SeedRunner(JdbcTemplate jdbc,
                      PlatformTransactionManager transactionManager,
                      @Value("${app.seed.count:10000}") int count,
                      @Value("${app.seed.random-seed:42}") long randomSeed,
                      @Value("${app.seed.as-of:2026-09-01}") String asOf,
                      @Value("${app.seed.reset:false}") boolean reset) {
        this.jdbc = jdbc;
        this.transaction = new TransactionTemplate(transactionManager);
        this.count = count;
        this.randomSeed = randomSeed;
        this.asOf = LocalDate.parse(asOf);
        this.reset = reset;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long existing = jdbc.queryForObject("SELECT COUNT(*) FROM employee", Long.class);
        if (existing != null && existing > 0 && !reset) {
            log.warn("employee table already has {} rows; nothing seeded. "
                    + "Re-run with --app.seed.reset=true to replace them.", existing);
            return;
        }

        EmployeeGenerator generator = new EmployeeGenerator(
                new FxConverter(loadRates()), asOf, randomSeed, loadCountries(), loadJobTitles());
        List<GeneratedEmployee> generated = generator.generate(count);

        transaction.executeWithoutResult(status -> {
            if (reset) {
                jdbc.update("DELETE FROM salary_history");
                jdbc.update("DELETE FROM employee");
            }
            insertEmployees(generated);
            int historyRows = insertHistory(generated);
            log.info("Seeded {} employees and {} salary-history rows (random seed {}, as of {})",
                    generated.size(), historyRows, randomSeed, asOf);
        });
    }

    // Reference rows are read in a fixed order: the generator's output depends on list order.

    private Map<String, BigDecimal> loadRates() {
        return jdbc.query("SELECT code, rate_to_usd FROM currency ORDER BY code",
                        (rs, i) -> Map.entry(rs.getString("code").trim(), rs.getBigDecimal("rate_to_usd")))
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<CountryRef> loadCountries() {
        return jdbc.query("SELECT code, currency_code FROM country ORDER BY code",
                (rs, i) -> new CountryRef(rs.getString("code").trim(), rs.getString("currency_code").trim()));
    }

    private List<JobTitleRef> loadJobTitles() {
        return jdbc.query("SELECT id, title, department_id FROM job_title ORDER BY id",
                (rs, i) -> new JobTitleRef(rs.getInt("id"), rs.getString("title"), rs.getInt("department_id")));
    }

    private void insertEmployees(List<GeneratedEmployee> generated) {
        jdbc.batchUpdate(INSERT_EMPLOYEE, generated, BATCH_SIZE, (ps, g) -> {
            EmployeeInput e = g.employee();
            ps.setString(1, e.firstName());
            ps.setString(2, e.lastName());
            ps.setString(3, e.email());
            ps.setString(4, e.countryCode());
            ps.setInt(5, e.departmentId());
            ps.setInt(6, e.jobTitleId());
            ps.setInt(7, e.level());
            ps.setDate(8, Date.valueOf(e.hireDate()));
            ps.setBigDecimal(9, e.salary());
            ps.setString(10, e.currencyCode());
        });
    }

    private int insertHistory(List<GeneratedEmployee> generated) {
        // Identity ids are assigned by the database, so look them up by the unique email.
        Map<String, Long> idByEmail = new HashMap<>();
        jdbc.query("SELECT id, email FROM employee",
                (rs, i) -> Map.entry(rs.getString("email"), rs.getLong("id")))
                .forEach(entry -> idByEmail.put(entry.getKey(), entry.getValue()));

        List<HistoryRow> rows = generated.stream()
                .flatMap(g -> g.history().stream().map(entry -> new HistoryRow(
                        idByEmail.get(g.employee().email()), g.employee().currencyCode(), entry)))
                .toList();

        jdbc.batchUpdate(INSERT_HISTORY, rows, BATCH_SIZE, (ps, row) -> {
            SalaryHistoryEntry entry = row.entry();
            ps.setLong(1, row.employeeId());
            ps.setObject(2, entry.oldSalary(), Types.DECIMAL);
            ps.setBigDecimal(3, entry.newSalary());
            ps.setString(4, row.currencyCode());
            ps.setDate(5, Date.valueOf(entry.effectiveDate()));
            ps.setString(6, entry.reason());
        });
        return rows.size();
    }
}
