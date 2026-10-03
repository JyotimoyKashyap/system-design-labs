package com.jyotimoykashyap.pricingengine.farestrategy;

import com.jyotimoykashyap.models.Fare;
import com.jyotimoykashyap.models.Ticket;

@FunctionalInterface
public interface FareStrategy {
    void calculateFare(Ticket ticket, Fare fare);
}
