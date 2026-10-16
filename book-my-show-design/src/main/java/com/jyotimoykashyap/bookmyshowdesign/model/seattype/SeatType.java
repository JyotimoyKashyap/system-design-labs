package com.jyotimoykashyap.bookmyshowdesign.model.seattype;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "seat_types")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "name")
@Getter 
@Setter 
@NoArgsConstructor 
public abstract class SeatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public abstract double getPriceMultiplier();
    public abstract List<String> getPerks();
    public abstract String getName();
}
