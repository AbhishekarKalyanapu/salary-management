package com.acme.salary.employee;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/** Composable filters for the employee directory. Each returns null when the filter is absent. */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> countryIs(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("country").get("code"), countryCode);
    }

    public static Specification<Employee> departmentIs(Integer departmentId) {
        if (departmentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<Employee> jobTitleIs(Integer jobTitleId) {
        if (jobTitleId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("jobTitle").get("id"), jobTitleId);
    }

    public static Specification<Employee> levelIs(Integer level) {
        if (level == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("level"), level);
    }

    /** Case-insensitive match against first name, last name, or email. */
    public static Specification<Employee> matches(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), like),
                cb.like(cb.lower(root.get("lastName")), like),
                cb.like(cb.lower(root.get("email")), like));
    }

    /** Combines every non-null specification with AND; null-safe starting point. */
    public static Specification<Employee> allOf(List<Specification<Employee>> specs) {
        Specification<Employee> result = Specification.where(null);
        for (Specification<Employee> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}
