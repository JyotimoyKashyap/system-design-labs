package com.jyotimoykashyap.bookmyshowdesign.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jyotimoykashyap.bookmyshowdesign.model.Booking;
import com.jyotimoykashyap.bookmyshowdesign.model.Payment;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentMethod;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentStatus;
import com.jyotimoykashyap.bookmyshowdesign.repository.PaymentRepository;
import com.jyotimoykashyap.bookmyshowdesign.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
@Transactional 
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    
    @Override
    public Payment processPayment(Booking booking, PaymentMethod paymentMethod) {
        Payment payment = Payment.builder()
                        .amount(booking.getTotalAmount())
                        .method(paymentMethod)
                        .status(PaymentStatus.SUCCESS)
                        .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                        .timestamp(Instant.now())
                        .booking(booking)
                        .build();
        return paymentRepository.save(payment);
    }

    @Override
    public Payment refundPayment(int bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId).orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));
        payment.setStatus(PaymentStatus.REFUNDED);

        return paymentRepository.save(payment);
    }

}
