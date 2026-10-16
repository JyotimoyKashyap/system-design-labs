package com.jyotimoykashyap.bookmyshowdesign.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatsRequest {
    private int showId;
    private List<Integer> seatIds;
    private int userId;
}

