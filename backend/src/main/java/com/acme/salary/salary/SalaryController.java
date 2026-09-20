package com.acme.salary.salary;

import com.acme.salary.salary.dto.SalaryChangeRequest;
import com.acme.salary.salary.dto.SalaryHistoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** {@code POST /api/employees/{id}/salary} and {@code GET /api/employees/{id}/salary-history}. */
@RestController
@RequestMapping("/api/employees/{id}")
public class SalaryController {

    private final SalaryChangeService salaryChangeService;

    public SalaryController(SalaryChangeService salaryChangeService) {
        this.salaryChangeService = salaryChangeService;
    }

    @PostMapping("/salary")
    public SalaryHistoryResponse changeSalary(@PathVariable("id") Long employeeId,
                                               @RequestBody SalaryChangeRequest request) {
        return salaryChangeService.changeSalary(employeeId, request);
    }

    @GetMapping("/salary-history")
    public List<SalaryHistoryResponse> history(@PathVariable("id") Long employeeId) {
        return salaryChangeService.history(employeeId);
    }
}
