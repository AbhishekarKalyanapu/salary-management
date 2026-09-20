# Design Notes

## Stack
| Layer | Choice | Why |
|---|---|---|
| Backend | Java 21, Spring Boot 3 (Web, Validation, Data JPA, JDBC) | Role-specified; mature ecosystem |
| Database | SQL Server, schema managed by Flyway | Role-specified; versioned, repeatable migrations |
| Frontend | Angular (standalone components), Angular Material, Chart.js | Role-specified; Material gives a table with server-side paginator and sorting |
| Tests | JUnit 5, AssertJ, Mockito, Testcontainers (MSSQL) | Fast unit tests for logic; few real-DB tests for queries |

## Backend structure (package by feature)
```
com.acme.salary
  employee/   controller, service, repository, dto, validation
  salary/     salary-change service, history repository
  insights/   controller, service, InsightsRepository (native SQL)
  reference/  countries, departments, job titles, currencies, FX
  csv/        import/export
  common/     error handling (RFC 7807), paging
  seed/       SeedRunner (profile "seed")
```
Principle: business rules live in plain Java classes (`SalaryPolicy`, `FxConverter`, `OutlierDetector`, `EmployeeValidator`) with no Spring or DB dependency, so they are unit-testable in milliseconds.

## Data model decisions
- **Salary stored in local currency**, with `currency_code` on the employee, so history stays correct if a country's currency mapping changes.
- **Salary changes go through one service method** that updates `employee.salary` and inserts a `salary_history` row in a single transaction. No other code path writes salary.
- **`ROWVERSION` for optimistic concurrency**, so two edits do not silently overwrite each other.
- Full schema: `backend/src/main/resources/db/migration/V1__init_schema.sql`.

## API (v1)
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/employees` | page, size, sort, q, country, department, jobTitle, level |
| POST / PUT / DELETE | `/api/employees[/{id}]` | CRUD with validation |
| POST | `/api/employees/{id}/salary` | change salary (amount, effectiveDate, reason) |
| GET | `/api/employees/{id}/salary-history` | history |
| GET | `/api/insights/summary` | headcount, total payroll (USD), median |
| GET | `/api/insights/by/{country\|department\|jobTitle}` | count, min, median, mean, p90, max |
| GET | `/api/insights/distribution` | histogram buckets, filterable |
| GET | `/api/insights/outliers?thresholdPct=` | employees far from same-title-and-country median |
| GET / POST | `/api/employees/export`, `/api/employees/import` | CSV |
| GET | `/api/reference/*` | dropdown data |

## Insights queries
- Aggregates (count/min/max/avg) use `GROUP BY` in SQL, with salary converted to USD via a join to `currency`.
- Median/p90 use `PERCENTILE_CONT(...) WITHIN GROUP (ORDER BY salary) OVER (PARTITION BY ...)`, then `SELECT DISTINCT` (it is a window function in SQL Server).
- Outliers: compare each salary with its (country, job title) median, in local currency so FX never distorts the comparison. Small groups (fewer than 5 people) are excluded, because a median of 2 people is not meaningful.

## Seeding
- **Reference data is a migration, not a seed.** `V2__reference_data.sql` inserts currencies (illustrative fixed FX rates), 8 countries, 8 departments and 40 job titles, because the app cannot work without them.
- **`EmployeeGenerator`** (pure Java, no I/O) produces employees and their salary history. Same seed, reference data and reference date always give identical output, so tests and demos are reproducible. It uses `java.util.Random` plus `StrictMath`, whose results are specified to be identical on every JVM.
- Salary = title entry salary x level step (+18% per level above the title's minimum) x country pay index x log-normal noise, converted to local currency and rounded to a human amount. Levels are constrained per title (a Director is never level 1).
- About 1.5% of people are deliberately paid far above or below peers, so the outlier view has something to find.
- **Salary history** is built backwards from the current salary (0 to 3 annual raises of 2-9%), so it always ends at exactly the employee's salary.
- **`SeedRunner`** (profile `seed`, no web server, one-shot) reads reference rows in a fixed order, generates 10,000 employees (about 23,500 history rows), and inserts them in JDBC batches of 1,000 inside one transaction. It refuses to touch a non-empty table unless `--app.seed.reset=true`.
- `SeedCatalogTest` parses the V2 migration and fails if the Java seed parameters and the SQL reference rows ever drift apart.

## Performance
- Server-side pagination and sorting; the directory never loads 10k rows into the browser.
- Indexes match the filter and grouping columns (see the migration). The `INCLUDE`d salary column makes the aggregation queries covering.
- Target: directory and insight queries under 500 ms at 10k rows; verify with an integration test that asserts the seeded count and a timing sanity check.

## Testing strategy
1. **Unit (no DB, no Spring):** validation rules, FX conversion and rounding, outlier detection, CSV row parsing and validation, salary-change rules.
2. **Service tests (Mockito):** salary change writes history and updates the employee atomically; rejected changes write nothing.
3. **Integration (few and focused):** the median/percentile queries, the pagination and filter query, migrations apply cleanly. They run against a dedicated `salary_test` database on the same SQL Server, so Docker is not required. Testcontainers remains an option for CI.
4. **Angular:** component tests for the filter bar and the dashboard rendering from a stubbed service.

## Known gaps (deliberate, see requirements)
No auth, live FX or demographics analysis. A real deployment needs SSO and an audit trail before holding real salary data.
