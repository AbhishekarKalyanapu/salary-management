package com.acme.salary.domain;

public class UnknownCurrencyException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public UnknownCurrencyException(String currency) {
        super("No FX rate configured for currency: " + currency);
    }
}
