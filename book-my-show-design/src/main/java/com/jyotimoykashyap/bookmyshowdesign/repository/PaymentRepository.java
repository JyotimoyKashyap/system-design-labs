package com.jyotimoykashyap.bookmyshowdesign.repository;

import com.jyotimoykashyap.bookmyshowdesign.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByBookingId(Integer bookingId);
    Optional<Payment> findByTransactionReference(String transactionReference);
}
