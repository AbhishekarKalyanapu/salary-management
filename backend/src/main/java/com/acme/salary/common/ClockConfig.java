package com.acme.salary.common;

import com.acme.salary.domain.EmployeeValidator;
import com.acme.salary.domain.OutlierDetector;
import com.acme.salary.domain.SalaryChangePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires plain, framework-agnostic domain classes (see DESIGN.md: "business rules live in
 * plain Java classes with no Spring or DB dependency") as Spring beans, without adding any
 * Spring annotation to those classes themselves.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public EmployeeValidator employeeValidator(Clock clock) {
        return new EmployeeValidator(clock);
    }

    @Bean
    public SalaryChangePolicy salaryChangePolicy() {
        return new SalaryChangePolicy();
    }

    @Bean
    public OutlierDetector outlierDetector() {
        return new OutlierDetector();
    }
}
