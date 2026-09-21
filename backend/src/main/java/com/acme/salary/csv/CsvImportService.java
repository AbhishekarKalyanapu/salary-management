package com.acme.salary.csv;

import com.acme.salary.csv.dto.ImportResultResponse;
import com.acme.salary.csv.dto.ImportRowError;
import com.acme.salary.domain.ValidationError;
import com.acme.salary.domain.ValidationException;
import com.acme.salary.employee.EmployeeService;
import com.acme.salary.employee.dto.EmployeeCreateRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Imports new employees from CSV (REQUIREMENTS.md: "the migration path off Excel"). Every row
 * is attempted independently \u2014 each call to {@link EmployeeService#create} runs in its own
 * transaction, so a bad row 5 never rolls back the good rows 1\u20134. Deliberately create-only:
 * updating existing employees, including salary, goes through the employee/salary APIs instead,
 * so history and validation stay consistent with every other write path.
 */
@Service
public class CsvImportService {

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "firstName", "lastName", "email", "countryCode", "currencyCode",
            "departmentId", "jobTitleId", "level", "hireDate", "salary");

    private final EmployeeService employeeService;

    public CsvImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public ImportResultResponse importCsv(Reader reader) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader().setSkipHeaderRecord(true).setTrim(true).build();

        List<Long> createdIds = new ArrayList<>();
        List<ImportRowError> errors = new ArrayList<>();
        int totalRows = 0;

        try (CSVParser parser = new CSVParser(reader, format)) {
            if (!parser.getHeaderMap().keySet().containsAll(REQUIRED_HEADERS)) {
                throw new IllegalArgumentException(
                        "CSV is missing required columns. Expected at least: " + REQUIRED_HEADERS);
            }
            for (CSVRecord record : parser) {
                totalRows++;
                int rowNumber = totalRows;
                List<ValidationError> parseErrors = new ArrayList<>();

                Integer departmentId = parseInt(record, "departmentId", parseErrors);
                Integer jobTitleId = parseInt(record, "jobTitleId", parseErrors);
                Integer level = parseInt(record, "level", parseErrors);
                LocalDate hireDate = parseDate(record, "hireDate", parseErrors);
                BigDecimal salary = parseSalary(record, "salary", parseErrors);

                // Even when some fields failed to parse (null below), we still run the full
                // create() so every problem in the row is reported together in one pass,
                // rather than making the user fix issues one round-trip at a time.
                var request = new EmployeeCreateRequest(
                        blankToNull(record.get("firstName")),
                        blankToNull(record.get("lastName")),
                        blankToNull(record.get("email")),
                        blankToNull(record.get("countryCode")),
                        blankToNull(record.get("currencyCode")),
                        departmentId, jobTitleId, level, hireDate, salary);

                try {
                    var created = employeeService.create(request);
                    createdIds.add(created.id());
                } catch (ValidationException ex) {
                    errors.add(new ImportRowError(rowNumber, mergeErrors(parseErrors, ex.errors())));
                }
            }
        }

        return new ImportResultResponse(totalRows, createdIds.size(), errors.size(), createdIds, errors);
    }

    /** A field that failed to parse (e.g. bad salary) is passed as null into validation, which
     *  would also flag it as "is required" \u2014 redundant with the more specific parse message.
     *  Parse errors win for their own field; domain errors fill in everything else. */
    private static List<ValidationError> mergeErrors(List<ValidationError> parseErrors,
                                                       List<ValidationError> domainErrors) {
        Set<String> parseErrorFields = new java.util.HashSet<>();
        for (ValidationError e : parseErrors) {
            parseErrorFields.add(e.field());
        }
        List<ValidationError> merged = new ArrayList<>(parseErrors);
        for (ValidationError e : domainErrors) {
            if (!parseErrorFields.contains(e.field())) {
                merged.add(e);
            }
        }
        return merged;
    }

    private static Integer parseInt(CSVRecord record, String field, List<ValidationError> errors) {
        String raw = record.isMapped(field) ? record.get(field) : null;
        if (raw == null || raw.isBlank()) {
            errors.add(new ValidationError(field, "is required"));
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(field, "must be a whole number"));
            return null;
        }
    }

    private static LocalDate parseDate(CSVRecord record, String field, List<ValidationError> errors) {
        String raw = record.isMapped(field) ? record.get(field) : null;
        if (raw == null || raw.isBlank()) {
            errors.add(new ValidationError(field, "is required"));
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            errors.add(new ValidationError(field, "must be a valid date in YYYY-MM-DD format"));
            return null;
        }
    }

    private static BigDecimal parseSalary(CSVRecord record, String field, List<ValidationError> errors) {
        String raw = record.isMapped(field) ? record.get(field) : null;
        if (raw == null || raw.isBlank()) {
            errors.add(new ValidationError(field, "is required"));
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(field, "must be a valid number"));
            return null;
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
