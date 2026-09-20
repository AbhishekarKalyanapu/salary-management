package com.acme.salary.salary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, Long> {

    List<SalaryHistory> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);
}
