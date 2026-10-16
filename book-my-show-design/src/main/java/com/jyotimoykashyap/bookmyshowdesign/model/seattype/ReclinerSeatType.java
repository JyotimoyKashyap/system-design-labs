package com.jyotimoykashyap.bookmyshowdesign.model.seattype;

import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity 
@DiscriminatorValue ("RECLINER")
public class ReclinerSeatType extends SeatType {
    
    @Override 
    public String getName() {
        return "RECLINER";
    }

    @Override
    public double getPriceMultiplier() {
        return 2.0;
    }

    @Override
    public List<String> getPerks() {
        return List.of("160-degree Recline", "Free Popcorn", "Lounge Access");
    }
}
