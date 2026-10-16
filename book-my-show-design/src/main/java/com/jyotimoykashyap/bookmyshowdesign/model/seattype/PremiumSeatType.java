package com.jyotimoykashyap.bookmyshowdesign.model.seattype;

import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


@Entity 
@DiscriminatorValue ("PREMIUM")
public class PremiumSeatType extends SeatType {

    @Override 
    public String getName() { return "PREMIUM"; }

    @Override
    public double getPriceMultiplier() { return 1.5; }

    @Override
    public List<String> getPerks() { return List.of("Extra Legroom", "USB-Charging"); }
}
