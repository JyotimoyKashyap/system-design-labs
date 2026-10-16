package com.jyotimoykashyap.bookmyshowdesign.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jyotimoykashyap.bookmyshowdesign.model.seattype.SeatType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity 
@Table (name = "seats")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Seat {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer id;

    private int seatRow;
    private int seatNumber;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "screen_id", nullable = false)
    @JsonIgnoreProperties ("seats")
    private Screen screen;
    
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "seat_type_id", nullable = false)
    private SeatType seatType;

    public String getRowLabel() {
        return String.valueOf((char) ('A' + seatRow));
    }
}
