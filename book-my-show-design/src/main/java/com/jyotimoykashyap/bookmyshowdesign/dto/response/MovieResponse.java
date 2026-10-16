package com.jyotimoykashyap.bookmyshowdesign.dto.response;

import com.jyotimoykashyap.bookmyshowdesign.model.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private Integer id;
    private String title;
    private String language;
    private String genre;
    private int durationMinutes;
    private double rating;

    public static MovieResponse fromEntity(Movie movie) {
        if (movie == null) return null;
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .language(movie.getLanguage())
                .genre(movie.getGenre())
                .durationMinutes(movie.getDurationMinutes())
                .rating(movie.getRating())
                .build();
    }
}

