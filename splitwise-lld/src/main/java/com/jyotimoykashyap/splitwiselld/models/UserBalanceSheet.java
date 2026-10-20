package com.jyotimoykashyap.splitwiselld.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UserBalanceSheet {
    private final User user;
    private final Map<User, BigDecimal> balances;

    public BigDecimal getTotalBack() {
        return balances.values().stream()
                .filter(b -> b.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalOwe() {
        return balances.values().stream()
                .filter(b -> b.compareTo(BigDecimal.ZERO) < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getNetBalance() {
        return getTotalBack().subtract(getTotalOwe());
    }
}
