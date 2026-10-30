package com.jyotimoykashyap;

import com.jyotimoykashyap.model.PaymentType;
import com.jyotimoykashyap.model.Slot;
import com.jyotimoykashyap.model.VendingSession;
import com.jyotimoykashyap.state.IdleState;
import com.jyotimoykashyap.state.VendingMachineState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context class representing the physical Vending Machine.
 * Delegates lifecycle operations to its current VendingMachineState.
 */
public class VendingMachine {
    private VendingMachineState state;
    private VendingSession session;
    private final Map<String, Slot> slots;

    public VendingMachine() {
        this.state = new IdleState();
        this.slots = new ConcurrentHashMap<>();
    }

    public void addSlot(Slot slot) {
        slots.put(slot.getSlotCode(), slot);
    }

    public Map<String, Slot> getSlots() {
        return Collections.unmodifiableMap(slots);
    }

    public Slot getSlot(String slotCode) {
        return slots.get(slotCode);
    }

    public Slot findAvailableSlotsForItem(String itemId) {
        return slots.values().stream()
                .filter(slot -> slot.getItem() != null
                        && slot.getItem().getId().equals(itemId)
                        && slot.isAvailable())
                .findFirst()
                .orElse(null);
    }

    // --- State Delegation Methods ---

    public void selectItems(List<String> itemIds) {
        state.selectItems(this, itemIds);
    }

    public void proceedToPay(PaymentType paymentType) {
        state.proceedToPay(this, paymentType);
    }

    public void choosePaymentType(PaymentType paymentType) {
        state.choosePaymentType(this, paymentType);
    }

    public void handlePaymentSuccess(String transactionId) {
        state.handlePaymentSuccess(this, transactionId);
    }

    public void handlePaymentFailure(String reason) {
        state.handlePaymentFailure(this, reason);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void onTimeout() {
        state.onTimeout(this);
    }

    public void onDispenseSuccess() {
        state.onDispenseSuccess(this);
    }

    public void onDispenseFailed() {
        state.onDispenseFailed(this);
    }

    public void enterMaintenance() {
        state.enterMaintenance(this);
    }

    public void exitMaintenance() {
        state.exitMaintenance(this);
    }

    // --- Context State & Session Accessors ---

    public VendingMachineState getState() {
        return state;
    }

    public void setState(VendingMachineState state) {
        this.state = state;
    }

    public VendingSession getCurrentSession() {
        return session;
    }

    public void setCurrentSession(VendingSession session) {
        this.session = session;
    }

    public void clearSession() {
        this.session = null;
    }
}
