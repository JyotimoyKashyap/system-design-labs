package com.jyotimoykashyap.bookmyshowdesign.repository;

import com.jyotimoykashyap.bookmyshowdesign.model.ShowSeat;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Integer> {
    List<ShowSeat> findByShowId(Integer showId);
    List<ShowSeat> findByShowIdAndStatus(Integer showId, SeatStatus status);
    List<ShowSeat> findByShowIdAndSeatIdIn(Integer showId, List<Integer> seatIds);
    List<ShowSeat> findByBookingId(Integer bookingId);
}
