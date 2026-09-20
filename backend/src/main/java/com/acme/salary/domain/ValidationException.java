package com.acme.salary.domain;

import java.util.List;
import java.util.stream.Collectors;

/** Thrown by the service layer when domain validation returns one or more errors. */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super(errors.stream()
                .map(e -> e.field() + ": " + e.message())
                .collect(Collectors.joining("; ")));
        this.errors = List.copyOf(errors);
    }

    public List<ValidationError> errors() {
        return errors;
    }
}
