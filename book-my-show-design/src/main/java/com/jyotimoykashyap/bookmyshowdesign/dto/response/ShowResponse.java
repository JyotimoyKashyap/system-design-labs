package com.jyotimoykashyap.bookmyshowdesign.dto.response;

import com.jyotimoykashyap.bookmyshowdesign.model.Show;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {
    private Integer id;
    private Integer movieId;
    private String movieTitle;
    private String theatreName;
    private String theatreAddress;
    private String screenName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public static ShowResponse fromEntity(Show show) {
        if (show == null) return null;
        return ShowResponse.builder()
                .id(show.getId())
                .movieId(show.getMovie() != null ? show.getMovie().getId() : null)
                .movieTitle(show.getMovie() != null ? show.getMovie().getTitle() : null)
                .theatreName(show.getScreen() != null && show.getScreen().getTheatre() != null ? show.getScreen().getTheatre().getName() : null)
                .theatreAddress(show.getScreen() != null && show.getScreen().getTheatre() != null ? show.getScreen().getTheatre().getAddress() : null)
                .screenName(show.getScreen() != null ? show.getScreen().getName() : null)
                .date(show.getDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .build();
    }
}

