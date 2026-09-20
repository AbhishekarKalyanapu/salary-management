package com.acme.salary.salary;

import com.acme.salary.domain.SalaryChangePolicy;
import com.acme.salary.domain.ValidationException;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeNotFoundException;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.salary.dto.SalaryChangeRequest;
import com.acme.salary.salary.dto.SalaryHistoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The single path that may change {@code employee.salary} (see DESIGN.md: "no other code
 * path writes salary"). Every change updates the employee row and inserts a
 * {@code salary_history} row in one transaction, so the two can never drift apart.
 */
@Service
public class SalaryChangeService {

    private final EmployeeRepository employeeRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;
    private final SalaryChangePolicy policy;

    public SalaryChangeService(EmployeeRepository employeeRepository,
                                SalaryHistoryRepository salaryHistoryRepository,
                                SalaryChangePolicy policy) {
        this.employeeRepository = employeeRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
        this.policy = policy;
    }

    @Transactional
    public SalaryHistoryResponse changeSalary(Long employeeId, SalaryChangeRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        var proposed = new SalaryChangePolicy.ProposedChange(
                request.newSalary(), request.effectiveDate(), request.reason());
        var errors = policy.validate(employee.getSalary(), employee.getHireDate(), proposed);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        var oldSalary = employee.getSalary();
        employee.setSalary(request.newSalary());
        employeeRepository.save(employee);

        SalaryHistory history = salaryHistoryRepository.save(new SalaryHistory(
                employee, oldSalary, request.newSalary(), employee.getCurrency(),
                request.effectiveDate(), request.reason()));

        return SalaryHistoryMapper.toResponse(history);
    }

    @Transactional(readOnly = true)
    public List<SalaryHistoryResponse> history(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        return salaryHistoryRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId).stream()
                .map(SalaryHistoryMapper::toResponse)
                .toList();
    }
}
