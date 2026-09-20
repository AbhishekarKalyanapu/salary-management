package com.acme.salary.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Maps to the `currency` table: 1 unit of this currency = rate_to_usd USD. */
@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @Column(name = "code", length = 3, nullable = false, columnDefinition = "CHAR(3)")
    private String code;

    @Column(name = "rate_to_usd", nullable = false, precision = 18, scale = 6, columnDefinition = "DECIMAL(18,6)")
    private BigDecimal rateToUsd;

    @Column(name = "as_of", nullable = false)
    private LocalDate asOf;

    protected Currency() {
        // JPA
    }

    public Currency(String code, BigDecimal rateToUsd, LocalDate asOf) {
        this.code = code;
        this.rateToUsd = rateToUsd;
        this.asOf = asOf;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getRateToUsd() {
        return rateToUsd;
    }

    public LocalDate getAsOf() {
        return asOf;
    }
}
