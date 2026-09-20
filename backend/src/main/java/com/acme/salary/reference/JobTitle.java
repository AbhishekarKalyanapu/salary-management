package com.acme.salary.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Maps to the `job_title` table. */
@Entity
@Table(name = "job_title")
public class JobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false, length = 150, unique = true)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    protected JobTitle() {
        // JPA
    }

    public JobTitle(String title, Department department) {
        this.title = title;
        this.department = department;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Department getDepartment() {
        return department;
    }
}
