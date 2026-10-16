package com.jyotimoykashyap.bookmyshowdesign.dto.response;

import com.jyotimoykashyap.bookmyshowdesign.model.ShowSeat;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatResponse {
    private Integer showSeatId;
    private Integer seatId;
    private String rowLabel;
    private int seatNumber;
    private String seatType;
    private BigDecimal price;
    private SeatStatus status;

    public static ShowSeatResponse fromEntity(ShowSeat ss) {
        if (ss == null) return null;

        return ShowSeatResponse.builder()
                .showSeatId(ss.getId())
                .seatId(ss.getSeat() != null ? ss.getSeat().getId() : null)
                .rowLabel(ss.getSeat() != null ? ss.getSeat().getRowLabel() : null)
                .seatNumber(ss.getSeat() != null ? ss.getSeat().getSeatNumber() : 0)
                .seatType(ss.getSeat() != null && ss.getSeat().getSeatType() != null ? ss.getSeat().getSeatType().getName() : "STANDARD")
                .price(ss.getPrice())
                .status(ss.getStatus())
                .build();
    }
}

