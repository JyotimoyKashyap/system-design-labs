package com.jyotimoykashyap.bookmyshowdesign.service;

import com.jyotimoykashyap.bookmyshowdesign.model.Booking;
import com.jyotimoykashyap.bookmyshowdesign.model.Payment;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentMethod;

/**
 * PaymentService
 */
public interface PaymentService {

    /**
     * Charges the user for the pending booking and returns the Payment record
     * 
     * @param booking
     * @param paymentMethod
     * @return
     */
    Payment processPayment(Booking booking, PaymentMethod paymentMethod);

    /**
     * Issues a refund when a booking is cancelled
     * 
     * @param bookingId
     * @return
     */
    Payment refundPayment(int bookingId);
}
