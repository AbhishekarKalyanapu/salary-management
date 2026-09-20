package com.acme.salary.employee;

import com.acme.salary.common.PageResponse;
import com.acme.salary.domain.EmployeeInput;
import com.acme.salary.domain.EmployeeValidator;
import com.acme.salary.domain.ValidationError;
import com.acme.salary.domain.ValidationException;
import com.acme.salary.employee.dto.EmployeeCreateRequest;
import com.acme.salary.employee.dto.EmployeeResponse;
import com.acme.salary.employee.dto.EmployeeUpdateRequest;
import com.acme.salary.reference.Country;
import com.acme.salary.reference.CountryRepository;
import com.acme.salary.reference.Currency;
import com.acme.salary.reference.CurrencyRepository;
import com.acme.salary.reference.Department;
import com.acme.salary.reference.DepartmentRepository;
import com.acme.salary.reference.JobTitle;
import com.acme.salary.reference.JobTitleRepository;
import com.acme.salary.salary.SalaryHistory;
import com.acme.salary.salary.SalaryHistoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates employee CRUD: structural validation ({@link EmployeeValidator}), existence
 * checks against reference data, then persistence. Deliberately does not change {@code salary}
 * on update \u2014 that only happens through the salary-change service (see DESIGN.md).
 */
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CountryRepository countryRepository;
    private final DepartmentRepository departmentRepository;
    private final JobTitleRepository jobTitleRepository;
    private final CurrencyRepository currencyRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;
    private final EmployeeValidator validator;

    public EmployeeService(EmployeeRepository employeeRepository,
                            CountryRepository countryRepository,
                            DepartmentRepository departmentRepository,
                            JobTitleRepository jobTitleRepository,
                            CurrencyRepository currencyRepository,
                            SalaryHistoryRepository salaryHistoryRepository,
                            EmployeeValidator validator) {
        this.employeeRepository = employeeRepository;
        this.countryRepository = countryRepository;
        this.departmentRepository = departmentRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.currencyRepository = currencyRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> list(String q, String countryCode, Integer departmentId,
                                                Integer jobTitleId, Integer level, Pageable pageable) {
        var spec = EmployeeSpecifications.allOf(Arrays.asList(
                EmployeeSpecifications.matches(q),
                EmployeeSpecifications.countryIs(countryCode),
                EmployeeSpecifications.departmentIs(departmentId),
                EmployeeSpecifications.jobTitleIs(jobTitleId),
                EmployeeSpecifications.levelIs(level)));
        var page = employeeRepository.findAll(spec, pageable).map(EmployeeMapper::toResponse);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(Long id) {
        return EmployeeMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest request) {
        List<ValidationError> errors = new ArrayList<>(validator.validate(toInput(request)));

        if (request.email() != null && employeeRepository.existsByEmailIgnoreCase(request.email())) {
            errors.add(new ValidationError("email", "is already in use"));
        }
        Country country = lookup(countryRepository::findById, request.countryCode(), "countryCode", errors);
        Department department = lookup(departmentRepository::findById, request.departmentId(), "departmentId", errors);
        JobTitle jobTitle = lookup(jobTitleRepository::findById, request.jobTitleId(), "jobTitleId", errors);
        Currency currency = lookup(currencyRepository::findById, request.currencyCode(), "currencyCode", errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Employee employee = new Employee(
                request.firstName(), request.lastName(), request.email(),
                country, department, jobTitle, request.level(),
                request.hireDate(), request.salary(), currency);
        employee = employeeRepository.save(employee);

        // Initial hire record: old_salary is NULL by schema convention for the first row.
        salaryHistoryRepository.save(new SalaryHistory(
                employee, null, request.salary(), currency, request.hireDate(), "Initial hire"));

        return EmployeeMapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        Employee employee = findOrThrow(id);
        List<ValidationError> errors = new ArrayList<>(validator.validate(toInput(request, employee)));

        if (request.email() != null && !request.email().equalsIgnoreCase(employee.getEmail())
                && employeeRepository.existsByEmailIgnoreCase(request.email())) {
            errors.add(new ValidationError("email", "is already in use"));
        }
        Country country = lookup(countryRepository::findById, request.countryCode(), "countryCode", errors);
        Department department = lookup(departmentRepository::findById, request.departmentId(), "departmentId", errors);
        JobTitle jobTitle = lookup(jobTitleRepository::findById, request.jobTitleId(), "jobTitleId", errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setCountry(country);
        employee.setDepartment(department);
        employee.setJobTitle(jobTitle);
        employee.setLevel(request.level());
        employee.setHireDate(request.hireDate());
        // Note: salary and currency are untouched here by design; see class javadoc.

        return EmployeeMapper.toResponse(employee);
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = findOrThrow(id);
        employeeRepository.delete(employee);
    }

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private <ID, T> T lookup(java.util.function.Function<ID, Optional<T>> finder, ID id,
                              String field, List<ValidationError> errors) {
        if (id == null) {
            // EmployeeValidator already reports "is required" for this field; avoid a duplicate error.
            return null;
        }
        Optional<T> found = finder.apply(id);
        if (found.isEmpty()) {
            errors.add(new ValidationError(field, "does not refer to an existing record"));
        }
        return found.orElse(null);
    }

    private EmployeeInput toInput(EmployeeCreateRequest r) {
        return new EmployeeInput(r.firstName(), r.lastName(), r.email(), r.countryCode(), r.currencyCode(),
                r.departmentId(), r.jobTitleId(), r.level(), r.hireDate(), r.salary());
    }

    /** Reuses the existing employee's salary/currency, since update never changes pay. */
    private EmployeeInput toInput(EmployeeUpdateRequest r, Employee existing) {
        return new EmployeeInput(r.firstName(), r.lastName(), r.email(), r.countryCode(),
                existing.getCurrency().getCode(), r.departmentId(), r.jobTitleId(), r.level(),
                r.hireDate(), existing.getSalary());
    }
}
