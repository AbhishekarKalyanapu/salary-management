package com.acme.salary.salary;

import com.acme.salary.employee.Employee;
import com.acme.salary.reference.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Maps to the `salary_history` table. One row per salary change, including the initial hire. */
@Entity
@Table(name = "salary_history")
public class SalaryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** NULL for the initial record. */
    @Column(name = "old_salary", precision = 18, scale = 2)
    private BigDecimal oldSalary;

    @Column(name = "new_salary", nullable = false, precision = 18, scale = 2)
    private BigDecimal newSalary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SalaryHistory() {
        // JPA
    }

    public SalaryHistory(Employee employee, BigDecimal oldSalary, BigDecimal newSalary,
                          Currency currency, LocalDate effectiveDate, String reason) {
        this.employee = employee;
        this.oldSalary = oldSalary;
        this.newSalary = newSalary;
        this.currency = currency;
        this.effectiveDate = effectiveDate;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public BigDecimal getOldSalary() {
        return oldSalary;
    }

    public BigDecimal getNewSalary() {
        return newSalary;
    }

    public Currency getCurrency() {
        return currency;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
