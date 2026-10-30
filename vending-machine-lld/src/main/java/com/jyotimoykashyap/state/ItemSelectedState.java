package com.jyotimoykashyap.state;

import com.jyotimoykashyap.VendingMachine;
import com.jyotimoykashyap.model.PaymentType;
import com.jyotimoykashyap.model.SessionStatus;
import com.jyotimoykashyap.model.VendingSession;

/**
 * State representing that the customer has selected items and can review the cart,
 * proceed to payment, or cancel.
 */
public class ItemSelectedState implements VendingMachineState {

    @Override
    public void proceedToPay(VendingMachine context, PaymentType paymentType) {
        if (context.getCurrentSession() == null) {
            throw new IllegalStateException("No active session found to pay for");
        }
        context.getCurrentSession().setStatus(SessionStatus.PAYMENT_PENDING);
        context.setState(new PaymentState(paymentType));
    }

    @Override
    public void cancel(VendingMachine context) {
        VendingSession session = context.getCurrentSession();
        if (session != null) {
            session.setStatus(SessionStatus.CANCELLED);
        }
        context.clearSession();
        context.setState(new IdleState());
    }

    @Override
    public void onTimeout(VendingMachine context) {
        cancel(context);
    }

    @Override
    public String getStateName() {
        return "ITEM_SELECTED";
    }
}
