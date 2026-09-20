package com.acme.salary.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * {@link JpaSpecificationExecutor} lets the service compose filters (country, department,
 * job title, level, name/email search) at query time instead of writing one method per
 * combination, while pagination and sorting stay server-side.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
