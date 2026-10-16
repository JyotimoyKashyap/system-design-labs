package com.jyotimoykashyap.bookmyshowdesign.repository;

import com.jyotimoykashyap.bookmyshowdesign.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByTitleContainingIgnoreCase(String title);
    List<Movie> findByLanguageIgnoreCase(String language);
    List<Movie> findByGenreIgnoreCase(String genre);
}
