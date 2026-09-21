package com.acme.salary.csv.dto;

import com.acme.salary.domain.ValidationError;

import java.util.List;

/** {@code rowNumber} is 1-based and counts data rows only (the header is not row 1). */
public record ImportRowError(int rowNumber, List<ValidationError> errors) {
}
