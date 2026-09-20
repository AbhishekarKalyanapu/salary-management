package com.acme.salary.salary;

import com.acme.salary.domain.SalaryChangePolicy;
import com.acme.salary.salary.dto.SalaryHistoryResponse;

/** Pure mapping, including the derived percent-change figure the UI likes to show. */
final class SalaryHistoryMapper {

    private SalaryHistoryMapper() {
    }

    static SalaryHistoryResponse toResponse(SalaryHistory h) {
        var percentChange = h.getOldSalary() == null
                ? null
                : SalaryChangePolicy.percentChange(h.getOldSalary(), h.getNewSalary());
        return new SalaryHistoryResponse(
                h.getId(),
                h.getOldSalary(),
                h.getNewSalary(),
                h.getCurrency().getCode(),
                h.getEffectiveDate(),
                h.getReason(),
                h.getCreatedAt(),
                percentChange);
    }
}
