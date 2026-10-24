package com.jyotimoykashyap.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Item {
    private final String id;
    private final String name;
    private final BigDecimal amount;
    private final ItemCategory category;

    public Item(String id, String name, BigDecimal amount, ItemCategory category) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.amount = Objects.requireNonNull(amount, "amount cannot be null");
        this.category = Objects.requireNonNull(category, "category cannot be null");

        if (id.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException("id or name cannot be blank");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ItemCategory getCategory() {
        return category;
    }
}
