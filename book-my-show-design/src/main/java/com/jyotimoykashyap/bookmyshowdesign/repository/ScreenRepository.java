package com.jyotimoykashyap.bookmyshowdesign.repository;

import com.jyotimoykashyap.bookmyshowdesign.model.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Integer> {
    List<Screen> findByTheatreId(Integer theatreId);
}
