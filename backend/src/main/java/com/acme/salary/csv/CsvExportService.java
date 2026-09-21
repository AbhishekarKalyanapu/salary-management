package com.acme.salary.csv;

import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmployeeSpecifications;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * Exports "the current filtered view" (REQUIREMENTS.md), reusing the same
 * {@link EmployeeSpecifications} the directory search uses, but unpaginated since an export
 * should contain every matching row, not just one page.
 */
@Service
public class CsvExportService {

    static final List<String> HEADERS = List.of(
            "id", "firstName", "lastName", "email", "countryCode", "countryName",
            "departmentId", "departmentName", "jobTitleId", "jobTitleTitle",
            "level", "hireDate", "salary", "currencyCode");

    private final EmployeeRepository employeeRepository;

    public CsvExportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public void export(Writer writer, String q, String countryCode, Integer departmentId,
                        Integer jobTitleId, Integer level) throws IOException {
        var spec = EmployeeSpecifications.allOf(Arrays.asList(
                EmployeeSpecifications.matches(q),
                EmployeeSpecifications.countryIs(countryCode),
                EmployeeSpecifications.departmentIs(departmentId),
                EmployeeSpecifications.jobTitleIs(jobTitleId),
                EmployeeSpecifications.levelIs(level)));
        List<Employee> employees = employeeRepository.findAll(spec, Sort.by("id"));

        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(HEADERS.toArray(new String[0])).build();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Employee e : employees) {
                printer.printRecord(
                        e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(),
                        e.getCountry().getCode(), e.getCountry().getName(),
                        e.getDepartment().getId(), e.getDepartment().getName(),
                        e.getJobTitle().getId(), e.getJobTitle().getTitle(),
                        e.getLevel(), e.getHireDate(), e.getSalary(), e.getCurrency().getCode());
            }
        }
    }
}
