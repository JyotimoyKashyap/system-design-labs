package com.jyotimoykashyap.bookmyshowdesign.dto.response;

import com.jyotimoykashyap.bookmyshowdesign.model.Booking;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Integer id;
    private String bookingReference;
    private Integer userId;
    private String userName;
    private Integer showId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private List<String> seatLabels;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private String paymentTransactionReference;

    public static BookingResponse fromEntity(Booking booking) {
        if (booking == null) return null;

        List<String> seatLabels = booking.getSeats() == null ? List.of() :
                booking.getSeats().stream()
                        .map(ss -> ss.getSeat() != null ? ss.getSeat().getRowLabel() + ss.getSeat().getSeatNumber() : "Seat-" + ss.getId())
                        .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .userName(booking.getUser() != null ? booking.getUser().getName() : null)
                .showId(booking.getShow() != null ? booking.getShow().getId() : null)
                .movieTitle(booking.getShow() != null && booking.getShow().getMovie() != null ? booking.getShow().getMovie().getTitle() : null)
                .theatreName(booking.getShow() != null && booking.getShow().getScreen() != null && booking.getShow().getScreen().getTheatre() != null
                        ? booking.getShow().getScreen().getTheatre().getName() : null)
                .screenName(booking.getShow() != null && booking.getShow().getScreen() != null ? booking.getShow().getScreen().getName() : null)
                .seatLabels(seatLabels)
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .expiresAt(booking.getExpiresAt())
                .paymentTransactionReference(booking.getPayment() != null ? booking.getPayment().getTransactionReference() : null)
                .build();
    }
}

