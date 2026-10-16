package com.jyotimoykashyap.bookmyshowdesign.repository;

import com.jyotimoykashyap.bookmyshowdesign.model.seattype.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatTypeRepository extends JpaRepository<SeatType, Integer> {
    Optional<SeatType> findByName(String name);
}
