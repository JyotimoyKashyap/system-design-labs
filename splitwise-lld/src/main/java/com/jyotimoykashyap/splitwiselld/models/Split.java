package com.jyotimoykashyap.splitwiselld.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;


@Embeddable
public record Split(
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "user_id", nullable = false)
        User user,

        @Column(nullable = false, precision = 12, scale = 2)
        BigDecimal amount)
{
    public Split {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(amount, "amount must not be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Split amount cannot be negative");
        }
    }
}
