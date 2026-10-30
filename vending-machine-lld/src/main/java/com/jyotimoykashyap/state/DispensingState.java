package com.jyotimoykashyap.state;

import com.jyotimoykashyap.VendingMachine;
import com.jyotimoykashyap.model.SessionStatus;
import com.jyotimoykashyap.model.VendingSession;

/**
 * State representing physical product dispensing and change return.
 */
public class DispensingState implements VendingMachineState {
    private final String transactionId;

    public DispensingState() {
        this(null);
    }

    public DispensingState(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public void onDispenseSuccess(VendingMachine context) {
        VendingSession session = context.getCurrentSession();
        if (session != null) {
            session.setStatus(SessionStatus.COMPLETED);
        }
        context.clearSession();
        context.setState(new IdleState());
    }

    @Override
    public void onDispenseFailed(VendingMachine context) {
        // Physical dispense failed (jam or motor fault) -> trigger refund logic
        VendingSession session = context.getCurrentSession();
        if (session != null) {
            session.setStatus(SessionStatus.CANCELLED);
        }
        context.clearSession();
        context.setState(new IdleState());
    }

    @Override
    public String getStateName() {
        return "DISPENSING";
    }
}
