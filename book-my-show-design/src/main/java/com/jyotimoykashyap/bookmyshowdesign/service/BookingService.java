package com.jyotimoykashyap.bookmyshowdesign.service;

import java.util.List;

import com.jyotimoykashyap.bookmyshowdesign.model.Booking;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentMethod;

/**
 * BookingService
 */
public interface BookingService {
    
    /**
     * Places a 10-min temporary hold on the requested sesats and created a PENDING booking
     * 
     * @param showId
     * @param seatIds
     * @param userId
     * @return {@Booking}
     */
    Booking holdSeats(int showId, List<Integer> seatIds, int userId);

    /**
     * Validates that the hold is still unexpired, processes payment and marks seats permanently as BOOKED
     * 
     * @param bookingId
     * @param userId
     * @param paymentMethod
     * @return {@Booking}
     */
    Booking confirmBooking(int bookingId, int userId, PaymentMethod paymentMethod);

    /**
     * Cancels an existing booking, frees the seats and initates a refund
     * 
     * @param bookingId
     * @param userId
     * @return {@Booking}
     */
    Booking cancelBooking(int bookingId, int userId);

    /**
     * Retrieves a booking by its unique reference (e.g. "BMS-183934")
     * 
     * @param bookingReference
     * @return {@Booking}
     */
    Booking getBookingByReference(String bookingReference);
}
