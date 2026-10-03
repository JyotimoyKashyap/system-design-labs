package com.jyotimoykashyap.pricingengine.farestrategy;

import com.jyotimoykashyap.models.Fare;
import com.jyotimoykashyap.models.Ticket;
import com.jyotimoykashyap.models.parkingspot.EvChargingSpot;

public class EvChargingStrategy implements FareStrategy {
    @Override
    public void calculateFare(Ticket ticket, Fare fare) {
        int hardwareFee = ticket.getParkingSpot().getAdditionalFee();
        fare.setAdditionalFare(fare.getAdditionalFare() + hardwareFee);
    }
}
