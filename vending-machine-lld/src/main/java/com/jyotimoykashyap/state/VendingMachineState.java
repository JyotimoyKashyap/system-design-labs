package com.jyotimoykashyap.state;

import com.jyotimoykashyap.VendingMachine;
import com.jyotimoykashyap.model.PaymentType;

import java.util.List;

/**
 * State contract for the Vending Machine.
 * Provides default throws so concrete states only implement valid transitions.
 */
public interface VendingMachineState {

    // 1. Selection Phase
    default void selectItems(VendingMachine context, List<String> itemIds) {
        throwException("Cannot select items in state");
    }

    // 2. Payment Phase
    default void proceedToPay(VendingMachine context, PaymentType paymentType) {
        throwException("Cannot proceed to pay in state");
    }

    default void choosePaymentType(VendingMachine context, PaymentType paymentType) {
        throwException("Cannot choose payment type in state");
    }

    default void handlePaymentSuccess(VendingMachine context, String transactionId) {
        throwException("Payment success not expected in state");
    }

    default void handlePaymentFailure(VendingMachine context, String reason) {
        throwException("Payment failure not expected in state");
    }

    // 3. User & Timer Interruptions
    default void cancel(VendingMachine context) {
        throwException("Cannot cancel in state");
    }

    default void onTimeout(VendingMachine context) {
        throwException("Timeout not handled in state");
    }

    // 4. Physical Dispensing Lifecycle
    default void onDispenseSuccess(VendingMachine context) {
        throwException("Dispense success not valid in state");
    }

    default void onDispenseFailed(VendingMachine context) {
        throwException("Dispense failure not valid in state");
    }

    // 5. Maintenance Mode
    default void enterMaintenance(VendingMachine context) {
        throwException("Cannot enter maintenance from state");
    }

    default void exitMaintenance(VendingMachine context) {
        throwException("Cannot exit maintenance from state");
    }

    String getStateName();

    default void throwException(String msg) {
        throw new IllegalStateException(msg + " : " + getStateName());
    }
}
