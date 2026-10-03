package com.jyotimoykashyap.pricingengine.farestrategy;

import com.jyotimoykashyap.models.Fare;
import com.jyotimoykashyap.models.Ticket;

public class BaseFareStrategy implements FareStrategy {
    @Override
    public void calculateFare(Ticket ticket, Fare fare) {
        double hours = ticket.getDurationHours();
        int hourlyRate = ticket.getParkingSpot().getBaseFee();
        int baseAmount = (int) Math.round(hours * hourlyRate);

        fare.setBaseFare(baseAmount);
    }
}
