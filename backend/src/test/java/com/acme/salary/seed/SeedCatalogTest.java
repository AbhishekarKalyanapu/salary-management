package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.seed.SeedCatalog.TitleSpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Keeps the Java seed parameters and the V2 reference-data migration from drifting apart. */
class SeedCatalogTest {

    private static final Pattern TITLE_ROW = Pattern.compile("\\('([^']+)',\\s*'([^']+)'\\)");
    private static final Pattern COUNTRY_ROW = Pattern.compile("\\('([A-Z]{2})',");

    private static String migration() throws IOException {
        try (InputStream in = SeedCatalogTest.class.getResourceAsStream("/db/migration/V2__reference_data.sql")) {
            assertThat(in != null).isTrue();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** The part of the migration from the given statement start up to its terminating semicolon. */
    private static String statement(String sql, String start) {
        int from = sql.indexOf(start);
        assertThat(from >= 0).isTrue();
        return sql.substring(from, sql.indexOf(';', from));
    }

    @Test
    void everyJobTitleInTheMigrationHasASeedSpecInTheSameDepartment() throws IOException {
        Matcher rows = TITLE_ROW.matcher(statement(migration(), "INSERT INTO job_title"));
        List<String> sqlTitles = new ArrayList<>();
        while (rows.find()) {
            sqlTitles.add(rows.group(1));
            TitleSpec spec = SeedCatalog.title(rows.group(1)).orElse(null);
            assertThat(spec != null).isTrue();
            assertThat(spec.department()).isEqualTo(rows.group(2));
        }

        assertThat(sqlTitles).containsExactlyInAnyOrder(
                SeedCatalog.TITLES.stream().map(TitleSpec::title).toArray(String[]::new));
    }

    @Test
    void everyCountryInTheMigrationHasASeedSpec() throws IOException {
        Matcher rows = COUNTRY_ROW.matcher(statement(migration(), "INSERT INTO country"));
        List<String> sqlCountries = new ArrayList<>();
        while (rows.find()) {
            sqlCountries.add(rows.group(1));
        }

        assertThat(sqlCountries).containsExactlyInAnyOrder(
                SeedCatalog.COUNTRIES.stream().map(SeedCatalog.CountrySpec::code).toArray(String[]::new));
    }

    @Test
    void titleParametersAreSane() {
        for (TitleSpec t : SeedCatalog.TITLES) {
            assertThat(t.entryUsd() > 0 && t.weight() > 0).isTrue();
            assertThat(t.minLevel() >= 1 && t.minLevel() <= t.maxLevel() && t.maxLevel() <= 6).isTrue();
        }
    }

    @Test
    void countryParametersAreSane() {
        for (SeedCatalog.CountrySpec c : SeedCatalog.COUNTRIES) {
            assertThat(c.payIndex() > 0 && c.weight() > 0).isTrue();
        }
    }
}
