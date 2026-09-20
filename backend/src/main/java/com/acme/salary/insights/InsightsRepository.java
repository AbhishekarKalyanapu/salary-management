package com.acme.salary.insights;

import com.acme.salary.domain.OutlierDetector;
import com.acme.salary.insights.dto.DimensionStatsResponse;
import com.acme.salary.insights.dto.SummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Raw SQL for the insight queries. Isolated here (per DESIGN.md) because SQL Server's
 * {@code PERCENTILE_CONT} is a window function, not an aggregate: a grouped median needs
 * {@code SELECT DISTINCT ... OVER (PARTITION BY ...)}, a different shape from a plain
 * {@code GROUP BY}, so aggregates and percentiles are fetched with two queries and merged
 * in Java rather than forced into one.
 */
@Repository
public class InsightsRepository {

    private final JdbcTemplate jdbc;

    public InsightsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SummaryResponse summary() {
        Map<String, Object> totals = jdbc.queryForMap("""
                SELECT COUNT(*) AS headcount,
                       SUM(e.salary * c.rate_to_usd) AS total_payroll_usd
                FROM employee e
                JOIN currency c ON c.code = e.currency_code
                """);
        BigDecimal median = jdbc.queryForObject("""
                SELECT DISTINCT
                    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY e.salary * c.rate_to_usd) OVER () AS median_usd
                FROM employee e
                JOIN currency c ON c.code = e.currency_code
                """, BigDecimal.class);
        return new SummaryResponse(
                ((Number) totals.get("headcount")).intValue(),
                (BigDecimal) totals.get("total_payroll_usd"),
                median);
    }

    /** Country groups stay in local currency: one country is already a single currency, so
     *  converting would only lose precision for no benefit. */
    public List<DimensionStatsResponse> byCountry() {
        List<Map<String, Object>> aggregates = jdbc.queryForList("""
                SELECT e.country_code AS dimension_key, co.name AS label, cur.code AS currency_code,
                       COUNT(*) AS cnt, MIN(e.salary) AS min_val, AVG(e.salary) AS mean_val, MAX(e.salary) AS max_val
                FROM employee e
                JOIN country co ON co.code = e.country_code
                JOIN currency cur ON cur.code = co.currency_code
                GROUP BY e.country_code, co.name, cur.code
                """);
        List<Map<String, Object>> percentiles = jdbc.queryForList("""
                SELECT DISTINCT e.country_code AS dimension_key,
                    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY e.salary) OVER (PARTITION BY e.country_code) AS median_val,
                    PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY e.salary) OVER (PARTITION BY e.country_code) AS p90_val
                FROM employee e
                """);
        return merge(aggregates, percentiles);
    }

    /** Department groups span multiple countries/currencies, so figures are converted to USD
     *  to be comparable \u2014 see DimensionStatsResponse javadoc. */
    public List<DimensionStatsResponse> byDepartment() {
        List<Map<String, Object>> aggregates = jdbc.queryForList("""
                SELECT CAST(e.department_id AS VARCHAR(20)) AS dimension_key, d.name AS label, 'USD' AS currency_code,
                       COUNT(*) AS cnt,
                       MIN(e.salary * c.rate_to_usd) AS min_val,
                       AVG(e.salary * c.rate_to_usd) AS mean_val,
                       MAX(e.salary * c.rate_to_usd) AS max_val
                FROM employee e
                JOIN currency c ON c.code = e.currency_code
                JOIN department d ON d.id = e.department_id
                GROUP BY e.department_id, d.name
                """);
        List<Map<String, Object>> percentiles = jdbc.queryForList("""
                SELECT DISTINCT CAST(e.department_id AS VARCHAR(20)) AS dimension_key,
                    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY e.salary * c.rate_to_usd) OVER (PARTITION BY e.department_id) AS median_val,
                    PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY e.salary * c.rate_to_usd) OVER (PARTITION BY e.department_id) AS p90_val
                FROM employee e
                JOIN currency c ON c.code = e.currency_code
                """);
        return merge(aggregates, percentiles);
    }

    /** Same reasoning as {@link #byDepartment()}: job titles span countries, so USD. */
    public List<DimensionStatsResponse> byJobTitle() {
        List<Map<String, Object>> aggregates = jdbc.queryForList("""
                SELECT CAST(e.job_title_id AS VARCHAR(20)) AS dimension_key, jt.title AS label, 'USD' AS currency_code,
                       COUNT(*) AS cnt,
                       MIN(e.salary * c.rate_to_usd) AS min_val,
                       AVG(e.salary * c.rate_to_usd) AS mean_val,
                       MAX(e.salary * c.rate_to_usd) AS max_val
                FROM employee e
                JOIN currency c ON c.code = e.currency_code
                JOIN job_title jt ON jt.id = e.job_title_id
                GROUP BY e.job_title_id, jt.title
                """);
        List<Map<String, Object>> percentiles = jdbc.queryForList("""
                SELECT DISTINCT CAST(e.job_title_id AS VARCHAR(20)) AS dimension_key,
                    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY e.salary * c.rate_to_usd) OVER (PARTITION BY e.job_title_id) AS median_val,
                    PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY e.salary * c.rate_to_usd) OVER (PARTITION BY e.job_title_id) AS p90_val
                FROM employee e
                JOIN currency c ON c.code = e.currency_code
                """);
        return merge(aggregates, percentiles);
    }

    /** All salaries in USD, for the distribution histogram (bucketed in Java, see InsightsService). */
    public List<BigDecimal> allSalariesUsd() {
        return jdbc.query("""
                SELECT e.salary * c.rate_to_usd AS salary_usd
                FROM employee e
                JOIN currency c ON c.code = e.currency_code
                """, (rs, rowNum) -> rs.getBigDecimal("salary_usd"));
    }

    /** Flat rows for {@link OutlierDetector}, which does the peer-group comparison in Java. */
    public List<OutlierDetector.SalaryRecord> allSalaryRecords() {
        return jdbc.query("""
                SELECT e.id AS employee_id, e.country_code, e.job_title_id, e.salary
                FROM employee e
                """, (rs, rowNum) -> new OutlierDetector.SalaryRecord(
                rs.getLong("employee_id"),
                rs.getString("country_code"),
                rs.getLong("job_title_id"),
                rs.getBigDecimal("salary")));
    }

    /** Joins aggregate and percentile rows (same dimension_key) into one DTO per group. */
    private List<DimensionStatsResponse> merge(List<Map<String, Object>> aggregates,
                                                List<Map<String, Object>> percentiles) {
        Map<String, Map<String, Object>> percentileByKey = new LinkedHashMap<>();
        for (Map<String, Object> row : percentiles) {
            percentileByKey.put((String) row.get("dimension_key"), row);
        }
        List<DimensionStatsResponse> result = new ArrayList<>();
        for (Map<String, Object> agg : aggregates) {
            String key = (String) agg.get("dimension_key");
            Map<String, Object> pct = percentileByKey.get(key);
            result.add(new DimensionStatsResponse(
                    key,
                    (String) agg.get("label"),
                    (String) agg.get("currency_code"),
                    ((Number) agg.get("cnt")).intValue(),
                    toBigDecimal(agg.get("min_val")),
                    pct == null ? null : toBigDecimal(pct.get("median_val")),
                    toBigDecimal(agg.get("mean_val")),
                    pct == null ? null : toBigDecimal(pct.get("p90_val")),
                    toBigDecimal(agg.get("max_val"))));
        }
        return result;
    }

    /**
     * The SQL Server JDBC driver doesn't always hand back a {@link BigDecimal} for a
     * {@code DECIMAL} column \u2014 {@code AVG()} in particular can come back as a {@link Double}
     * depending on the exact query shape. A blind cast breaks here, so this converts any
     * {@link Number} safely instead of assuming one specific runtime type.
     */
    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue()).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        throw new IllegalStateException("Unexpected numeric type: " + value.getClass());
    }
}
