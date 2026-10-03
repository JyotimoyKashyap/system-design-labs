package com.jyotimoykashyap.pricingengine.farestrategy;

import com.jyotimoykashyap.models.Fare;
import com.jyotimoykashyap.models.Ticket;

import java.time.LocalTime;

public class PeakHoursStrategy implements FareStrategy {
    private static final LocalTime MORNING_PEAK_START = LocalTime.of(8, 0);
    private static final LocalTime MORNING_PEAK_END = LocalTime.of(10, 0);
    private static final LocalTime EVENING_PEAK_START = LocalTime.of(17, 0);
    private static final LocalTime EVENING_PEAK_END = LocalTime.of(19, 0);

    @Override
    public void calculateFare(Ticket ticket, Fare fare) {
        LocalTime entry = ticket.getEntryTime().toLocalTime();
        boolean isPeak = (entry.isAfter(MORNING_PEAK_START) && entry.isBefore(MORNING_PEAK_END)) ||
                (entry.isAfter(EVENING_PEAK_START) && entry.isBefore(EVENING_PEAK_END));

        if (!isPeak) return;

        int surgeFee = (int) Math.round(fare.getBaseFare() * 0.50);
        fare.setServiceFee(fare.getServiceFee() + surgeFee);
    }
}
