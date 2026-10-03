package com.jyotimoykashyap.pricingengine;

import com.jyotimoykashyap.models.Fare;
import com.jyotimoykashyap.models.Ticket;
import com.jyotimoykashyap.pricingengine.farestrategy.BaseFareStrategy;
import com.jyotimoykashyap.pricingengine.farestrategy.EvChargingStrategy;
import com.jyotimoykashyap.pricingengine.farestrategy.FareStrategy;
import com.jyotimoykashyap.pricingengine.farestrategy.PeakHoursStrategy;

import java.util.List;

public class FareCalculator {
    private final List<FareStrategy> strategies;

    public FareCalculator(List<FareStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public static  FareCalculator defaultPipeline() {
        return new FareCalculator(List.of(
                new BaseFareStrategy(),
                new PeakHoursStrategy(),
                new EvChargingStrategy()
        ));
    }

    public Fare calculateFare(Ticket ticket) {
        Fare fare = new Fare();
        for (FareStrategy strategy : strategies) {
            strategy.calculateFare(ticket, fare);
        }
        return fare;
    }
}
