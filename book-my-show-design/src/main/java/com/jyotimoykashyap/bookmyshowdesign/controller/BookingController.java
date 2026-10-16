package com.jyotimoykashyap.bookmyshowdesign.controller;

import com.jyotimoykashyap.bookmyshowdesign.dto.request.CancelBookingRequest;
import com.jyotimoykashyap.bookmyshowdesign.dto.request.ConfirmBookingRequest;
import com.jyotimoykashyap.bookmyshowdesign.dto.request.HoldSeatsRequest;
import com.jyotimoykashyap.bookmyshowdesign.dto.response.BookingResponse;
import com.jyotimoykashyap.bookmyshowdesign.model.Booking;
import com.jyotimoykashyap.bookmyshowdesign.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/hold")
    public ResponseEntity<BookingResponse> holdSeats(@RequestBody HoldSeatsRequest request) {
        Booking booking = bookingService.holdSeats(request.getShowId(), request.getSeatIds(), request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.fromEntity(booking));
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable int bookingId,
            @RequestBody ConfirmBookingRequest request) {
        Booking booking = bookingService.confirmBooking(bookingId, request.getUserId(), request.getPaymentMethod());
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable int bookingId,
            @RequestBody CancelBookingRequest request) {
        Booking booking = bookingService.cancelBooking(bookingId, request.getUserId());
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }

    @GetMapping("/{bookingReference}")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String bookingReference) {
        Booking booking = bookingService.getBookingByReference(bookingReference);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }
}

