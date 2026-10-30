package com.jyotimoykashyap.model;

import java.util.Objects;

/**
 * A single slot will hold a single item with varying quantities.
 * An item can be in multiple slots as well
 */
public class Slot {
    private final String slotCode;
    private Item item;
    private int quantity;
    private final int capacity;
    private boolean isJammed; // so that in the display we can deactivate that slot selection

    public Slot(String slotCode, Item item, int quantity, int capacity) {
        this.slotCode = Objects.requireNonNull(slotCode, "slotCode cannot be null");
        this.item = item; // item can be null as some slots can remain empty
        this.quantity = quantity;
        this.capacity = capacity;
        this.isJammed = false;

        if (slotCode.isBlank()) {
            throw new IllegalArgumentException("slotCode cannot be blank");
        }

        if (quantity < 0 || capacity < 0) {
            throw new IllegalArgumentException("quantity or capacity cannot be < 0");
        }
    }

    public boolean isAvailable() {
        return !isJammed && item != null && quantity > 0;
    }

    public void decrementStock() {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Slot " + slotCode + " is out of stock");
        }
        this.quantity--;
    }

    public void restock(int qty) {
        if (this.quantity + qty > capacity) {
            throw new IllegalArgumentException("Cannot exceed capacity of " + capacity);
        }
        this.quantity += qty;
    }

    public String getSlotCode() {
        return slotCode;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isJammed() {
        return isJammed;
    }

    public void setJammed(boolean jammed) {
        this.isJammed = jammed;
    }
}
