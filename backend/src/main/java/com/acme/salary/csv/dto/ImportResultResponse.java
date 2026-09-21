package com.acme.salary.csv.dto;

import java.util.List;

/** The error report REQUIREMENTS.md asks for: every row is attempted independently, so one
 *  bad row never blocks the rest \u2014 this tells the HR Manager exactly which rows to fix. */
public record ImportResultResponse(
        int totalRows,
        int successCount,
        int errorCount,
        List<Long> createdEmployeeIds,
        List<ImportRowError> errors) {
}
