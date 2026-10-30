package com.jyotimoykashyap.state;

import com.jyotimoykashyap.VendingMachine;
import com.jyotimoykashyap.model.Item;
import com.jyotimoykashyap.model.Slot;
import com.jyotimoykashyap.model.VendingSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IdleState implements VendingMachineState {
    @Override
    public void selectItems(VendingMachine context, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new IllegalArgumentException("Must select at least one item");
        }

        List<Item> selectedItems = new ArrayList<>();
        List<String> assignedSlotCodes = new ArrayList<>();

        // resolve each item to an available unjammed slot
        for (String itemId : itemIds) {
            Slot slot = context.findAvailableSlotsForItem(itemId);
            if (slot == null || !slot.isAvailable()) {
                throw new IllegalStateException("Item " + itemId + " is currently out of stock or jammed");
            }
            selectedItems.add(slot.getItem());
            assignedSlotCodes.add(slot.getSlotCode());
        }

        // create new active session
        String sessionId = "SESS-" + UUID.randomUUID().toString().substring(0, 8);
        VendingSession session = new VendingSession(sessionId, assignedSlotCodes, selectedItems, 60);

        context.setCurrentSession(session);
        context.setState(new ItemSelectedState());
    }

    @Override
    public void enterMaintenance(VendingMachine context) {
        context.setState(new MaintenanceState());
    }

    @Override
    public String getStateName() {
        return "IDLE";
    }
}
