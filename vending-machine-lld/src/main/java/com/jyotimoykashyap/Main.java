package com.jyotimoykashyap;

import com.jyotimoykashyap.model.Item;
import com.jyotimoykashyap.model.ItemCategory;
import com.jyotimoykashyap.model.PaymentType;
import com.jyotimoykashyap.model.Slot;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Initializing Vending Machine ---");
        VendingMachine machine = new VendingMachine();

        // Stock items
        Item coke = new Item("ITEM-1", "Coca Cola", new BigDecimal("2.50"), ItemCategory.BEVERAGE);
        Item chips = new Item("ITEM-2", "Lays Classic", new BigDecimal("1.50"), ItemCategory.SNACK);

        machine.addSlot(new Slot("A1", coke, 5, 10));
        machine.addSlot(new Slot("B1", chips, 3, 10));

        System.out.println("Initial State: " + machine.getState().getStateName());

        // 1. Customer selects items
        System.out.println("\n--- Step 1: Customer selects Coca Cola ---");
        machine.selectItems(List.of("ITEM-1"));
        System.out.println("State: " + machine.getState().getStateName());
        System.out.println("Total Amount: $" + machine.getCurrentSession().getTotalAmount());

        // 2. Customer proceeds to pay
        System.out.println("\n--- Step 2: Customer proceeds to pay via UPI ---");
        machine.proceedToPay(PaymentType.UPI);
        System.out.println("State: " + machine.getState().getStateName());

        // 3. Simulated Payment Failure (e.g. insufficient funds)
        System.out.println("\n--- Step 3: Payment fails on first attempt ---");
        machine.handlePaymentFailure("UPI Payment Timed Out");
        System.out.println("State after failure: " + machine.getState().getStateName());

        // 4. Customer switches to Card and succeeds
        System.out.println("\n--- Step 4: Customer switches to CARD and succeeds ---");
        machine.choosePaymentType(PaymentType.CARD);
        machine.handlePaymentSuccess("TXN-98765");
        System.out.println("State: " + machine.getState().getStateName());

        // 5. Dispense success
        System.out.println("\n--- Step 5: Machine dispenses product ---");
        machine.onDispenseSuccess();
        System.out.println("State after dispensing: " + machine.getState().getStateName());
    }
}
