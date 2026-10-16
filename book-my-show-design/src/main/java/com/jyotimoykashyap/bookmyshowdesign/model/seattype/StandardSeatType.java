package com.jyotimoykashyap.bookmyshowdesign.model.seattype;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity 
@DiscriminatorValue ("STANDARD")
public class StandardSeatType extends SeatType {

    @Override 
    public String getName() {
        return "STANDARD";
    }

    @Override
    public double getPriceMultiplier() {
        return 1.0;
    }

    @Override
    public List<String> getPerks() {
        return Collections.emptyList();
    }
}
