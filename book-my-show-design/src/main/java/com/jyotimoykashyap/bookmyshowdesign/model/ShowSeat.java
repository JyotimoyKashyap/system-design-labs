package com.jyotimoykashyap.bookmyshowdesign.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jyotimoykashyap.bookmyshowdesign.model.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "show_seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"show_id", "seat_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    @JsonIgnoreProperties("showSeats")
    private Show show;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    @JsonIgnoreProperties("seats")
    private Booking booking;

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    public void lock(Booking booking) {
        this.status = SeatStatus.LOCKED;
        this.booking = booking;
    }

    public void book() {
        this.status = SeatStatus.BOOKED;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.booking = null;
    }
}
