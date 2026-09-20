package com.acme.salary.domain;

/** A single rule violation, tied to the input field it concerns. */
public record ValidationError(String field, String message) {
}
