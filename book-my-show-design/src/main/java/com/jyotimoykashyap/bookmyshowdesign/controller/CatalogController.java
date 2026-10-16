package com.jyotimoykashyap.bookmyshowdesign.controller;

import com.jyotimoykashyap.bookmyshowdesign.dto.response.MovieResponse;
import com.jyotimoykashyap.bookmyshowdesign.dto.response.ShowResponse;
import com.jyotimoykashyap.bookmyshowdesign.dto.response.ShowSeatResponse;
import com.jyotimoykashyap.bookmyshowdesign.model.Movie;
import com.jyotimoykashyap.bookmyshowdesign.model.Show;
import com.jyotimoykashyap.bookmyshowdesign.model.ShowSeat;
import com.jyotimoykashyap.bookmyshowdesign.service.ShowCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogController {

    private final ShowCatalogService showCatalogService;

    @GetMapping("/cities/{cityId}/movies")
    public ResponseEntity<List<MovieResponse>> getMoviesByCity(@PathVariable int cityId) {
        List<Movie> movies = showCatalogService.getMoviesByCity(cityId);
        List<MovieResponse> response = movies.stream().map(MovieResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shows")
    public ResponseEntity<List<ShowResponse>> getShows(
            @RequestParam int movieId,
            @RequestParam int cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<Show> shows = showCatalogService.getShows(movieId, cityId, queryDate);
        List<ShowResponse> response = shows.stream().map(ShowResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<List<ShowSeatResponse>> getSeatLayout(@PathVariable int showId) {
        List<ShowSeat> showSeats = showCatalogService.getSeatLayout(showId);
        List<ShowSeatResponse> response = showSeats.stream().map(ShowSeatResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }
}

