package com.jyotimoykashyap.bookmyshowdesign.service.impl;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jyotimoykashyap.bookmyshowdesign.locker.SeatLockProvider;
import com.jyotimoykashyap.bookmyshowdesign.model.Movie;
import com.jyotimoykashyap.bookmyshowdesign.model.Show;
import com.jyotimoykashyap.bookmyshowdesign.model.ShowSeat;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.SeatStatus;
import com.jyotimoykashyap.bookmyshowdesign.repository.ShowRepository;
import com.jyotimoykashyap.bookmyshowdesign.repository.ShowSeatRepository;
import com.jyotimoykashyap.bookmyshowdesign.service.ShowCatalogService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ShowCatalogServiceImpl implements ShowCatalogService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockProvider seatLockProvider;

    @Override
    public List<Movie> getMoviesByCity(int cityId) {
        return showRepository.findDistinctMoviesByCityId(cityId);
    }

    @Override
    public List<Show> getShows(int movieId, int cityId, LocalDate date) {
        return showRepository.findShowsByMovieAndCityAndDate(movieId, cityId, date);
    }

    @Override
    @Transactional (readOnly = true)
    public List<ShowSeat> getSeatLayout(int showId) {
        List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);
        Set<Integer> lockedSeatIds = new HashSet<>(seatLockProvider.getLockedSeats(showId));

        for (ShowSeat seat : showSeats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                continue;
            }

            if (lockedSeatIds.contains(seat.getSeat().getId())) {
                seat.setStatus(SeatStatus.LOCKED);
            } else {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
        }

        return showSeats;
    }

}
