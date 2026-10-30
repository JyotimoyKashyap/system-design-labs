package com.jyotimoykashyap;

import com.jyotimoykashyap.model.Item;
import com.jyotimoykashyap.model.ItemCategory;
import com.jyotimoykashyap.model.PaymentType;
import com.jyotimoykashyap.model.Slot;
import com.jyotimoykashyap.state.DispensingState;
import com.jyotimoykashyap.state.IdleState;
import com.jyotimoykashyap.state.ItemSelectedState;
import com.jyotimoykashyap.state.MaintenanceState;
import com.jyotimoykashyap.state.PaymentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VendingMachineTest {
    private VendingMachine machine;
    private Item water;
    private Item soda;

    @BeforeEach
    void setUp() {
        machine = new VendingMachine();
        water = new Item("ITEM-WATER", "Mineral Water", new BigDecimal("1.50"), ItemCategory.BEVERAGE);
        soda = new Item("ITEM-SODA", "Sparkling Soda", new BigDecimal("2.50"), ItemCategory.BEVERAGE);

        machine.addSlot(new Slot("A1", water, 5, 10));
        machine.addSlot(new Slot("A2", soda, 5, 10));
    }

    @Test
    @DisplayName("Happy Path: Select item -> Pay via UPI -> Dispense -> Return to Idle")
    void testHappyPathPurchase() {
        assertTrue(machine.getState() instanceof IdleState);

        // 1. Select item
        machine.selectItems(List.of("ITEM-WATER"));
        assertTrue(machine.getState() instanceof ItemSelectedState);
        assertNotNull(machine.getCurrentSession());
        assertEquals(new BigDecimal("1.50"), machine.getCurrentSession().getTotalAmount());

        // 2. Proceed to pay
        machine.proceedToPay(PaymentType.UPI);
        assertTrue(machine.getState() instanceof PaymentState);
        PaymentState paymentState = (PaymentState) machine.getState();
        assertEquals(PaymentType.UPI, paymentState.getPaymentType());

        // 3. Payment success
        machine.handlePaymentSuccess("TXN-12345");
        assertTrue(machine.getState() instanceof DispensingState);
        assertEquals("TXN-12345", ((DispensingState) machine.getState()).getTransactionId());

        // 4. Dispense success
        machine.onDispenseSuccess();
        assertTrue(machine.getState() instanceof IdleState);
        assertNull(machine.getCurrentSession());
    }

    @Test
    @DisplayName("Payment Failure & Recovery: UPI fails -> switch to CARD -> Payment succeeds")
    void testPaymentFailureAndRetry() {
        machine.selectItems(List.of("ITEM-SODA"));
        machine.proceedToPay(PaymentType.UPI);

        // Payment fails (e.g. gateway timeout)
        machine.handlePaymentFailure("UPI_TIMEOUT");
        assertTrue(machine.getState() instanceof PaymentState);
        PaymentState paymentState = (PaymentState) machine.getState();
        assertEquals("UPI_TIMEOUT", paymentState.getLastFailureReason());

        // Switch to CARD
        machine.choosePaymentType(PaymentType.CARD);
        assertEquals(PaymentType.CARD, paymentState.getPaymentType());
        assertNull(paymentState.getLastFailureReason());

        // Payment succeeds with CARD
        machine.handlePaymentSuccess("CARD-TXN-67890");
        assertTrue(machine.getState() instanceof DispensingState);

        machine.onDispenseSuccess();
        assertTrue(machine.getState() instanceof IdleState);
    }

    @Test
    @DisplayName("Cancellation in PaymentState returns to ItemSelectedState; Timeout returns to Idle")
    void testCancellationAndTimeoutInPaymentState() {
        machine.selectItems(List.of("ITEM-WATER"));
        machine.proceedToPay(PaymentType.CARD);
        assertTrue(machine.getState() instanceof PaymentState);

        // Cancel returns back to ItemSelectedState
        machine.cancel();
        assertTrue(machine.getState() instanceof ItemSelectedState);
        assertNotNull(machine.getCurrentSession());

        // Proceed to pay again, then simulate user walking away (timeout)
        machine.proceedToPay(PaymentType.UPI);
        machine.onTimeout();
        assertTrue(machine.getState() instanceof IdleState);
        assertNull(machine.getCurrentSession());
    }

    @Test
    @DisplayName("Maintenance mode locks operations until exited")
    void testMaintenanceMode() {
        machine.enterMaintenance();
        assertTrue(machine.getState() instanceof MaintenanceState);

        // Operations should be rejected in maintenance
        assertThrows(IllegalStateException.class, () -> machine.selectItems(List.of("ITEM-WATER")));

        machine.exitMaintenance();
        assertTrue(machine.getState() instanceof IdleState);
    }

    @Test
    @DisplayName("Illegal transition guards throw IllegalStateException")
    void testIllegalTransitions() {
        // Cannot handle payment success in IdleState
        assertThrows(IllegalStateException.class, () -> machine.handlePaymentSuccess("TXN-ILLEGAL"));

        // Cannot dispense in IdleState
        assertThrows(IllegalStateException.class, () -> machine.onDispenseSuccess());
    }
}
