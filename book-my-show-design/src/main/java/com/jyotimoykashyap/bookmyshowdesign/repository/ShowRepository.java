package com.jyotimoykashyap.bookmyshowdesign.repository;

import com.jyotimoykashyap.bookmyshowdesign.model.Movie;
import com.jyotimoykashyap.bookmyshowdesign.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Integer> {
    List<Show> findByMovieId(Integer movieId);
    List<Show> findByMovieIdAndDate(Integer movieId, LocalDate date);
    List<Show> findByScreenId(Integer screenId);

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId AND s.screen.theatre.city.id = :cityId AND s.date = :date")
    List<Show> findShowsByMovieAndCityAndDate(
            @Param("movieId") Integer movieId,
            @Param("cityId") Integer cityId,
            @Param("date") LocalDate date
    );


    @Query("SELECT DISTINCT s.movie FROM Show s WHERE s.screen.theatre.city.id = :cityId")
    List<Movie> findDistinctMoviesByCityId(@Param("cityId") Integer cityId);
}
