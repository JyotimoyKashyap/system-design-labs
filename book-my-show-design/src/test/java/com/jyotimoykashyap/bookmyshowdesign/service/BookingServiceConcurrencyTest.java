package com.jyotimoykashyap.bookmyshowdesign.service;

import com.jyotimoykashyap.bookmyshowdesign.exception.SeatUnavailableException;
import com.jyotimoykashyap.bookmyshowdesign.locker.SeatLockProvider;
import com.jyotimoykashyap.bookmyshowdesign.model.*;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.BookingStatus;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentMethod;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.PaymentStatus;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.SeatStatus;
import com.jyotimoykashyap.bookmyshowdesign.model.seattype.SeatType;
import com.jyotimoykashyap.bookmyshowdesign.model.seattype.StandardSeatType;
import com.jyotimoykashyap.bookmyshowdesign.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingServiceConcurrencyTest {

    @Autowired private BookingService bookingService;
    @Autowired private SeatLockProvider seatLockProvider;

    @Autowired private CityRepository cityRepository;
    @Autowired private TheatreRepository theatreRepository;
    @Autowired private ScreenRepository screenRepository;
    @Autowired private SeatTypeRepository seatTypeRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatRepository showSeatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;

    private Show show;
    private List<Seat> seats = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Clean up database in FK dependency order
        paymentRepository.deleteAll();
        showSeatRepository.deleteAll();
        bookingRepository.deleteAll();
        showRepository.deleteAll();
        seatRepository.deleteAll();
        screenRepository.deleteAll();
        theatreRepository.deleteAll();
        cityRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();
        seatTypeRepository.deleteAll();

        // 1. City & Theatre
        City city = cityRepository.save(City.builder().name("Bengaluru").state("Karnataka").build());
        Theatre theatre = theatreRepository.save(Theatre.builder().name("PVR Forum").city(city).address("Koramangala").build());

        // 2. Screen
        Screen screen = screenRepository.save(Screen.builder().name("Audi 1").theatre(theatre).totalSeats(10).build());

        // 3. SeatType & Physical Seats
        SeatType standardType = seatTypeRepository.save(new StandardSeatType());
        seats = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            seats.add(seatRepository.save(Seat.builder()
                    .seatRow(1)
                    .seatNumber(i)
                    .screen(screen)
                    .seatType(standardType)
                    .build()));
        }

        // 4. Movie & Show
        Movie movie = movieRepository.save(Movie.builder()
                .title("Interstellar")
                .language("English")
                .genre("Sci-Fi")
                .durationMinutes(169)
                .rating(8.7)
                .build());

        show = showRepository.save(Show.builder()
                .movie(movie)
                .screen(screen)
                .date(LocalDate.now())
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(21, 0))
                .build());

        // 5. ShowSeats
        for (Seat s : seats) {
            showSeatRepository.save(ShowSeat.builder()
                    .show(show)
                    .seat(s)
                    .price(BigDecimal.valueOf(250.00))
                    .status(SeatStatus.AVAILABLE)
                    .build());
        }

        // 6. 25 Users for concurrent requests
        users = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            users.add(userRepository.save(User.builder()
                    .name("User " + i)
                    .email("user" + i + "@example.com")
                    .build()));
        }
    }

    @Test
    @DisplayName("20 concurrent threads race for the exact same 2 seats -> Exactly 1 wins, 19 fail fast")
    void testConcurrentHoldSeats_sameSeats_onlyOneWins() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Integer> targetSeatIds = List.of(seats.get(0).getId(), seats.get(1).getId());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Booking> successfulBookings = Collections.synchronizedList(new ArrayList<>());
        List<Exception> capturedExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startGun.await(); // wait for simultaneous trigger
                    Booking booking = bookingService.holdSeats(show.getId(), targetSeatIds, users.get(userIndex).getId());
                    successCount.incrementAndGet();
                    successfulBookings.add(booking);
                } catch (SeatUnavailableException e) {
                    failureCount.incrementAndGet();
                    capturedExceptions.add(e);
                } catch (Exception e) {
                    capturedExceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Wait until all threads are prepped, then fire start gun
        assertTrue(readyLatch.await(5, TimeUnit.SECONDS), "Threads took too long to get ready");
        startGun.countDown();

        // Wait for all threads to finish
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "Concurrent execution timed out");
        executor.shutdown();

        // Assertions: Zero double-booking
        assertEquals(1, successCount.get(), "Exactly one user must successfully hold the seats");
        assertEquals(19, failureCount.get(), "All remaining 19 users must fail with SeatUnavailableException");
        assertEquals(1, successfulBookings.size());

        Booking winningBooking = successfulBookings.get(0);
        assertNotNull(winningBooking.getId());
        assertEquals(BookingStatus.PENDING, winningBooking.getStatus());
        assertEquals(2, winningBooking.getSeats().size());

        // Verify seats in DB reflect LOCKED status
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdIn(show.getId(), targetSeatIds);
        for (ShowSeat ss : showSeats) {
            assertEquals(SeatStatus.LOCKED, ss.getStatus());
            assertEquals(winningBooking.getId(), ss.getBooking().getId());
        }
    }

    @Test
    @DisplayName("5 concurrent threads request disjoint seats -> All 5 succeed simultaneously")
    void testConcurrentHoldSeats_disjointSeats_allSucceed() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            // Thread 0 takes [0,1], Thread 1 takes [2,3], Thread 2 takes [4,5], etc.
            List<Integer> seatPair = List.of(seats.get(index * 2).getId(), seats.get(index * 2 + 1).getId());
            executor.submit(() -> {
                try {
                    startGun.await();
                    bookingService.holdSeats(show.getId(), seatPair, users.get(index).getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(5, successCount.get(), "All 5 disjoint reservations should succeed");
    }

    @Test
    @DisplayName("Deadlock prevention: Opposite seat lock orders (A: [1,2], B: [2,1]) complete without hang")
    void testDeadlockPrevention_oppositeOrder_noDeadlock() throws Exception {
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        List<Integer> orderA = List.of(seats.get(0).getId(), seats.get(1).getId());
        List<Integer> orderB = List.of(seats.get(1).getId(), seats.get(0).getId());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try {
                startGun.await();
                bookingService.holdSeats(show.getId(), orderA, users.get(0).getId());
                successCount.incrementAndGet();
            } catch (SeatUnavailableException e) {
                failCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startGun.await();
                bookingService.holdSeats(show.getId(), orderB, users.get(1).getId());
                successCount.incrementAndGet();
            } catch (SeatUnavailableException e) {
                failCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        });

        startGun.countDown();
        // If there was a circular wait deadlock, this would time out
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "Deadlock detected! Both threads timed out waiting for locks");
        executor.shutdown();

        assertEquals(1, successCount.get());
        assertEquals(1, failCount.get());
    }

    @Test
    @DisplayName("End-to-End: Hold -> Confirm Payment -> Verify seats become BOOKED")
    void testEndToEnd_holdAndConfirm() {
        List<Integer> targetSeatIds = List.of(seats.get(0).getId(), seats.get(1).getId());
        User user = users.get(0);

        // 1. Hold Seats
        Booking hold = bookingService.holdSeats(show.getId(), targetSeatIds, user.getId());
        assertNotNull(hold);
        assertEquals(BookingStatus.PENDING, hold.getStatus());

        // 2. Confirm Booking with Payment
        Booking confirmed = bookingService.confirmBooking(hold.getId(), user.getId(), PaymentMethod.CREDIT_CARD);
        assertNotNull(confirmed);
        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
        assertNotNull(confirmed.getPayment());
        assertEquals(PaymentStatus.SUCCESS, confirmed.getPayment().getStatus());

        // 3. Verify ShowSeats transitioned to BOOKED
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdIn(show.getId(), targetSeatIds);
        for (ShowSeat ss : showSeats) {
            assertEquals(SeatStatus.BOOKED, ss.getStatus());
        }

        // 4. Verify in-memory lock is released
        List<Integer> lockedSeats = seatLockProvider.getLockedSeats(show.getId());
        assertFalse(lockedSeats.contains(seats.get(0).getId()));
        assertFalse(lockedSeats.contains(seats.get(1).getId()));
    }

    @Test
    @DisplayName("End-to-End: Hold -> Cancel Booking -> Seats released back to AVAILABLE")
    void testEndToEnd_holdAndCancel() {
        List<Integer> targetSeatIds = List.of(seats.get(0).getId(), seats.get(1).getId());
        User user = users.get(0);

        Booking hold = bookingService.holdSeats(show.getId(), targetSeatIds, user.getId());
        assertEquals(BookingStatus.PENDING, hold.getStatus());

        Booking cancelled = bookingService.cancelBooking(hold.getId(), user.getId());
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());

        // Verify seats released
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdIn(show.getId(), targetSeatIds);
        for (ShowSeat ss : showSeats) {
            assertEquals(SeatStatus.AVAILABLE, ss.getStatus());
            assertNull(ss.getBooking());
        }

        // Verify in-memory lock cleared
        List<Integer> lockedSeats = seatLockProvider.getLockedSeats(show.getId());
        assertFalse(lockedSeats.contains(seats.get(0).getId()));
        assertFalse(lockedSeats.contains(seats.get(1).getId()));
    }
}

