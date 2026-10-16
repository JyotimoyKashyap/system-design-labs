package com.jyotimoykashyap.bookmyshowdesign.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jyotimoykashyap.bookmyshowdesign.exception.SeatLockExpiredException;
import com.jyotimoykashyap.bookmyshowdesign.exception.SeatUnavailableException;
import com.jyotimoykashyap.bookmyshowdesign.locker.SeatLockProvider;
import com.jyotimoykashyap.bookmyshowdesign.model.Booking;
import com.jyotimoykashyap.bookmyshowdesign.model.ShowSeat;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.BookingStatus;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentMethod;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentStatus;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.SeatStatus;
import com.jyotimoykashyap.bookmyshowdesign.repository.BookingRepository;
import com.jyotimoykashyap.bookmyshowdesign.repository.ShowRepository;
import com.jyotimoykashyap.bookmyshowdesign.repository.ShowSeatRepository;
import com.jyotimoykashyap.bookmyshowdesign.repository.UserRepository;
import com.jyotimoykashyap.bookmyshowdesign.service.BookingService;
import com.jyotimoykashyap.bookmyshowdesign.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.val;


@Service
@RequiredArgsConstructor 
@Transactional 
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final SeatLockProvider seatLockProvider;
    private final PaymentService paymentService;

    @Override
    public Booking holdSeats(int showId, List<Integer> seatIds, int userId) {
        // validate showId and userId first
        val show = showRepository.findById(showId).orElseThrow(() -> new IllegalArgumentException("Please provide valid showid"));
        val user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Please provide valid userid"));

        //fetch the seats from ids
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdIn(showId, seatIds);

        // check if all seats are available
        if (showSeats.size() != seatIds.size())
            throw new SeatUnavailableException("One or more invalid seats");

        // check if any seats are already booked
        boolean isBooked = showSeats.stream().anyMatch(seat -> seat.getStatus() == SeatStatus.BOOKED);
        if (isBooked)
            throw new SeatUnavailableException("One or more seats are already booked");

        boolean isLocked = seatLockProvider.lockSeats(showId, seatIds, userId);
        if (!isLocked) {
            throw new SeatUnavailableException("One or more seats are currently locked by another user");
        }

        val total = showSeats.stream().map(seat -> seat.getPrice())
            .reduce(BigDecimal.ZERO, (subtotal, price) -> subtotal.add(price));
        
        Booking booking = Booking.builder()
                            .bookingReference("BMS-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                            .user(user)
                            .show(show)
                            .status(BookingStatus.PENDING)
                            .totalAmount(total)
                            .createdAt(Instant.now())
                            .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                            .build();

        booking = bookingRepository.save(booking);
        
        for (ShowSeat s : showSeats) {
            s.lock(booking);
        }

        showSeatRepository.saveAll(showSeats);
        booking.setSeats(showSeats);

        booking = bookingRepository.save(booking);
        return booking;
    }

    @Override
    public Booking confirmBooking(int bookingId, int userId, PaymentMethod paymentMethod) {
        var booking = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking ID is not valid"));
        // make sure user requesting the booking is the same user as that of the booking
        if (!booking.getUser().getId().equals(userId))
            throw new IllegalArgumentException("Invalid User");
        if (booking.isExpired()) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new IllegalArgumentException("Booking already expired");
        }
        // make sure booking is in PENDING state
        if (booking.getStatus() != BookingStatus.PENDING)
            throw new IllegalArgumentException("Booking is already " + booking.getStatus().toString());

        // verify lock ownership
        List<Integer> seatIds = booking.getSeats().stream().map(showSeat -> showSeat.getSeat().getId()).toList();
        boolean isValid = seatLockProvider.validateLock(booking.getShow().getId(), seatIds, userId);

        if (!isValid) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new SeatLockExpiredException("Hold expired, seats released");
        }

        val payment = paymentService.processPayment(booking, paymentMethod);

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Payment failed with status: " + payment.getStatus());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPayment(payment);

        // get showSeats 
        List<ShowSeat> showSeats = booking.getSeats().stream().toList();

        for (ShowSeat seat : showSeats) {
            seat.book();
        }

        showSeatRepository.saveAll(booking.getSeats());
        booking = bookingRepository.save(booking);

        // cleanup the seat locking
        seatLockProvider.unlockSeats(booking.getShow().getId(), seatIds, userId);
        return booking;
    }

    @Override
    public Booking cancelBooking(int bookingId, int userId) {
        var booking = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking doesn't exists"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled");
        }

        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User doens't exists"));

        // validate booking belongs to the user 
        if (booking.getUser().getId() != userId)
            throw new IllegalArgumentException("Booking doesn't belong to the user");

        // refund first 
        if (booking.getPayment() != null)
            paymentService.refundPayment(bookingId);

        // release the seasts
        booking.getSeats().forEach(seat -> seat.release());
        showSeatRepository.saveAll(booking.getSeats());
        booking.setStatus(BookingStatus.CANCELLED);

        List<Integer> seatIds = booking.getSeats().stream().map(seat -> seat.getSeat().getId()).toList();

        seatLockProvider.unlockSeats(booking.getShow().getId(), seatIds, userId);
        booking = bookingRepository.save(booking);
        return booking;
    }

    @Override
    public Booking getBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingReference));
    }

}
