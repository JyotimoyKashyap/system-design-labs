package com.jyotimoykashyap.state;

import com.jyotimoykashyap.VendingMachine;
import com.jyotimoykashyap.model.PaymentType;
import com.jyotimoykashyap.model.SessionStatus;
import com.jyotimoykashyap.model.VendingSession;

/**
 * Handles payment method selection, processing, and success/failure callbacks.
 */
public class PaymentState implements VendingMachineState {
    private PaymentType paymentType;
    private String lastFailureReason;

    public PaymentState() {
        this(null);
    }

    public PaymentState(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    @Override
    public void choosePaymentType(VendingMachine context, PaymentType paymentType) {
        this.paymentType = paymentType;
        this.lastFailureReason = null;
    }

    @Override
    public void handlePaymentSuccess(VendingMachine context, String transactionId) {
        VendingSession session = context.getCurrentSession();
        if (session == null) {
            throw new IllegalStateException("No active session found during payment success");
        }

        session.setStatus(SessionStatus.PAID);
        // Payment verified: transition immediately to physical dispensing
        context.setState(new DispensingState(transactionId));
    }

    @Override
    public void handlePaymentFailure(VendingMachine context, String reason) {
        this.lastFailureReason = reason;
        VendingSession session = context.getCurrentSession();
        if (session != null) {
            session.setStatus(SessionStatus.PAYMENT_PENDING);
        }
        // Machine remains in PaymentState, allowing customer to retry or choose another payment method
    }

    @Override
    public void cancel(VendingMachine context) {
        // User pressed cancel/back on payment screen: return to cart/item selection
        this.lastFailureReason = null;
        context.setState(new ItemSelectedState());
    }

    @Override
    public void onTimeout(VendingMachine context) {
        // Inactivity timeout: abandon session and return to idle
        VendingSession session = context.getCurrentSession();
        if (session != null) {
            session.setStatus(SessionStatus.CANCELLED);
        }
        context.clearSession();
        context.setState(new IdleState());
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    @Override
    public String getStateName() {
        return "PAYMENT";
    }
}
