package com.acme.salary.employee;

/** Thrown when an employee id does not exist. Mapped to 404 by {@link com.acme.salary.common.GlobalExceptionHandler}. */
public class EmployeeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmployeeNotFoundException(Long id) {
        super("No employee with id " + id);
    }
}
