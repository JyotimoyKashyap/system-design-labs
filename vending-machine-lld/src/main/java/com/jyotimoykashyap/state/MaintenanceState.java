package com.jyotimoykashyap.state;

import com.jyotimoykashyap.VendingMachine;

public class MaintenanceState implements VendingMachineState {
    @Override
    public void exitMaintenance(VendingMachine context) {
        context.setState(new IdleState());
    }

    @Override
    public String getStateName() {
        return "MAINTENANCE";
    }
}
