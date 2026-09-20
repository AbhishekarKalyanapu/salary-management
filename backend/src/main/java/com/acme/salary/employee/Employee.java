package com.acme.salary.employee;

import com.acme.salary.reference.Country;
import com.acme.salary.reference.Currency;
import com.acme.salary.reference.Department;
import com.acme.salary.reference.JobTitle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Maps to the `employee` table. Thin persistence class: validation and business rules
 * live in {@code com.acme.salary.domain}, not here.
 */
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_title_id", nullable = false)
    private JobTitle jobTitle;

    /** 1 (junior) .. 6 (executive) */
    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    /** Annual, in local currency. The source of truth; see {@code currency}. */
    @Column(name = "salary", nullable = false, precision = 18, scale = 2)
    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * SQL Server ROWVERSION for optimistic concurrency. It is DB-generated on every
     * write, never set by the application, hence insertable/updatable = false.
     */
    @Version
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "row_version", insertable = false, updatable = false)
    private byte[] rowVersion;

    protected Employee() {
        // JPA
    }

    public Employee(String firstName, String lastName, String email, Country country,
                     Department department, JobTitle jobTitle, Integer level,
                     LocalDate hireDate, BigDecimal salary, Currency currency) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.country = country;
        this.department = department;
        this.jobTitle = jobTitle;
        this.level = level;
        this.hireDate = hireDate;
        this.salary = salary;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public JobTitle getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(JobTitle jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    /** Do not call directly to change pay \u2014 go through the salary-change service so history is recorded. */
    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public byte[] getRowVersion() {
        return rowVersion;
    }
}
