package com.jyotimoykashyap.bookmyshowdesign.repository;

import com.jyotimoykashyap.bookmyshowdesign.model.Booking;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Optional<Booking> findByBookingReference(String bookingReference);
    List<Booking> findByUserId(Integer userId);
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, Instant now);
}
