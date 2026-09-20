package com.acme.salary.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Maps to the `country` table. */
@Entity
@Table(name = "country")
public class Country {

    @Id
    @Column(name = "code", length = 2, nullable = false)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    protected Country() {
        // JPA
    }

    public Country(String code, String name, Currency currency) {
        this.code = code;
        this.name = name;
        this.currency = currency;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Currency getCurrency() {
        return currency;
    }
}
