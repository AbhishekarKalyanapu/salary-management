package com.acme.salary.employee;

import com.acme.salary.employee.dto.EmployeeResponse;

/** Pure mapping, no business logic \u2014 kept separate so EmployeeService stays focused on behavior. */
final class EmployeeMapper {

    private EmployeeMapper() {
    }

    static EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(
                e.getId(),
                e.getFirstName(),
                e.getLastName(),
                e.getEmail(),
                e.getCountry().getCode(),
                e.getCountry().getName(),
                e.getDepartment().getId(),
                e.getDepartment().getName(),
                e.getJobTitle().getId(),
                e.getJobTitle().getTitle(),
                e.getLevel(),
                e.getHireDate(),
                e.getSalary(),
                e.getCurrency().getCode(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                EmployeeResponse.encodeVersion(e.getRowVersion()));
    }
}
