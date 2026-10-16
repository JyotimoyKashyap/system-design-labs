package com.jyotimoykashyap.bookmyshowdesign.seeder;

import com.jyotimoykashyap.bookmyshowdesign.model.*;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.SeatStatus;
import com.jyotimoykashyap.bookmyshowdesign.model.seattype.PremiumSeatType;
import com.jyotimoykashyap.bookmyshowdesign.model.seattype.ReclinerSeatType;
import com.jyotimoykashyap.bookmyshowdesign.model.seattype.SeatType;
import com.jyotimoykashyap.bookmyshowdesign.model.seattype.StandardSeatType;
import com.jyotimoykashyap.bookmyshowdesign.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryDataSeeder implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (cityRepository.count() > 0) {
            log.info("Database already seeded. Skipping InMemoryDataSeeder.");
            return;
        }

        log.info("--- Bootstrapping BookMyShow In-Memory Database ---");

        // 1. Seat Types
        SeatType standardType = seatTypeRepository.save(new StandardSeatType());
        SeatType premiumType = seatTypeRepository.save(new PremiumSeatType());
        SeatType reclinerType = seatTypeRepository.save(new ReclinerSeatType());

        // 2. Cities
        City bengaluru = cityRepository.save(City.builder().name("Bengaluru").state("Karnataka").build());
        City mumbai = cityRepository.save(City.builder().name("Mumbai").state("Maharashtra").build());
        City delhi = cityRepository.save(City.builder().name("Delhi").state("Delhi").build());

        // 3. Theatres
        Theatre pvrForum = theatreRepository.save(Theatre.builder()
                .name("PVR Forum Koramangala")
                .address("Hosur Road, Koramangala")
                .city(bengaluru)
                .build());

        Theatre inoxPhoenix = theatreRepository.save(Theatre.builder()
                .name("INOX Phoenix Marketcity")
                .address("Whitefield Main Road, Mahadevapura")
                .city(bengaluru)
                .build());

        theatreRepository.save(Theatre.builder()
                .name("PVR Icon Infiniti Mall")
                .address("Link Road, Andheri West")
                .city(mumbai)
                .build());

        // 4. Screens & Seats
        Screen audi1 = screenRepository.save(Screen.builder()
                .name("Audi 1 (Dolby Atmos)")
                .theatre(pvrForum)
                .totalSeats(36)
                .build());

        Screen audiImax = screenRepository.save(Screen.builder()
                .name("IMAX Laser Screen")
                .theatre(inoxPhoenix)
                .totalSeats(36)
                .build());

        List<Seat> audi1Seats = seedScreenSeats(audi1, standardType, premiumType, reclinerType);
        List<Seat> imaxSeats = seedScreenSeats(audiImax, standardType, premiumType, reclinerType);

        // 5. Movies
        Movie interstellar = movieRepository.save(Movie.builder()
                .title("Interstellar")
                .language("English")
                .genre("Sci-Fi / Adventure")
                .durationMinutes(169)
                .rating(8.7)
                .build());

        Movie inception = movieRepository.save(Movie.builder()
                .title("Inception")
                .language("English")
                .genre("Sci-Fi / Action")
                .durationMinutes(148)
                .rating(8.8)
                .build());

        Movie dune2 = movieRepository.save(Movie.builder()
                .title("Dune: Part Two")
                .language("English")
                .genre("Sci-Fi / Epic")
                .durationMinutes(166)
                .rating(8.9)
                .build());

        // 6. Shows & ShowSeats
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        createShowWithSeats(interstellar, audi1, today, LocalTime.of(14, 30), LocalTime.of(17, 20), audi1Seats);
        createShowWithSeats(inception, audi1, today, LocalTime.of(18, 0), LocalTime.of(20, 30), audi1Seats);
        createShowWithSeats(dune2, audiImax, today, LocalTime.of(21, 0), LocalTime.of(23, 45), imaxSeats);
        createShowWithSeats(interstellar, audi1, tomorrow, LocalTime.of(18, 0), LocalTime.of(20, 50), audi1Seats);

        // 7. Users
        userRepository.save(User.builder().name("Alice Smith").email("alice@example.com").phoneNumber("+91-9876543210").build());
        userRepository.save(User.builder().name("Bob Jones").email("bob@example.com").phoneNumber("+91-9876543211").build());
        userRepository.save(User.builder().name("Charlie Brown").email("charlie@example.com").phoneNumber("+91-9876543212").build());

        log.info("--- BookMyShow Seeding Complete: 3 Cities, 3 Theatres, 2 Screens, 72 Seats, 3 Movies, 4 Shows, 3 Users ---");
    }

    private List<Seat> seedScreenSeats(Screen screen, SeatType standard, SeatType premium, SeatType recliner) {
        List<Seat> seats = new ArrayList<>();
        // Row 0 ('A'): 6 Recliner seats
        for (int i = 1; i <= 6; i++) {
            seats.add(seatRepository.save(Seat.builder()
                    .screen(screen)
                    .seatRow(0)
                    .seatNumber(i)
                    .seatType(recliner)
                    .build()));
        }
        // Rows 1-2 ('B', 'C'): 10 Premium seats each
        for (int r = 1; r <= 2; r++) {
            for (int i = 1; i <= 10; i++) {
                seats.add(seatRepository.save(Seat.builder()
                        .screen(screen)
                        .seatRow(r)
                        .seatNumber(i)
                        .seatType(premium)
                        .build()));
            }
        }
        // Row 3 ('D'): 10 Standard seats
        for (int i = 1; i <= 10; i++) {
            seats.add(seatRepository.save(Seat.builder()
                    .screen(screen)
                    .seatRow(3)
                    .seatNumber(i)
                    .seatType(standard)
                    .build()));
        }
        return seats;
    }

    private void createShowWithSeats(Movie movie, Screen screen, LocalDate date, LocalTime start, LocalTime end, List<Seat> seats) {
        Show show = showRepository.save(Show.builder()
                .movie(movie)
                .screen(screen)
                .date(date)
                .startTime(start)
                .endTime(end)
                .build());

        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BigDecimal price = BigDecimal.valueOf(200.00).multiply(BigDecimal.valueOf(seat.getSeatType().getPriceMultiplier()));
            showSeats.add(ShowSeat.builder()
                    .show(show)
                    .seat(seat)
                    .price(price)
                    .status(SeatStatus.AVAILABLE)
                    .build());
        }
        showSeatRepository.saveAll(showSeats);
    }
}

