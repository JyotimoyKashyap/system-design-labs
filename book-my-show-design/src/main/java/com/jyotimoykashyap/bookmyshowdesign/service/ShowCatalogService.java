package com.jyotimoykashyap.bookmyshowdesign.service;

import java.time.LocalDate;
import java.util.List;

import com.jyotimoykashyap.bookmyshowdesign.model.Movie;
import com.jyotimoykashyap.bookmyshowdesign.model.Show;
import com.jyotimoykashyap.bookmyshowdesign.model.ShowSeat;

/**
 * ShowCatalogService
 */
public interface ShowCatalogService {

    List<Movie> getMoviesByCity(int cityId);

    List<Show> getShows(int movieId, int cityId, LocalDate date);

    /**
     * Returns the seating layout for a show reflecting both permanently 
     * BOOKED seats and currently LOCKED seats
     * 
     * @param showId
     * @return
     */
    List<ShowSeat> getSeatLayout(int showId);
}
