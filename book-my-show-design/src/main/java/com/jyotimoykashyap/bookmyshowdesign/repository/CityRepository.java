package com.jyotimoykashyap.bookmyshowdesign.repository;

import com.jyotimoykashyap.bookmyshowdesign.model.City;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface CityRepository extends JpaRepository<City, Integer> {
    Optional<City> findByNameIgnoreCase(String name);
}
