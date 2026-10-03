package com.jyotimoykashyap.models;

import com.jyotimoykashyap.models.parkingspot.ParkingSpot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Ticket {
    private final UUID id;
    private final Vehicle vehicle;
    private final ParkingSpot parkingSpot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Fare fare;


    public Ticket(Vehicle vehicle, ParkingSpot parkingSpot, LocalDateTime entryTime) {
        this(UUID.randomUUID(), vehicle, parkingSpot, entryTime, null, null);
    }

    public Ticket(UUID id, Vehicle vehicle, ParkingSpot parkingSpot, LocalDateTime entryTime, LocalDateTime exitTime, Fare fare) {
        this.id = Objects.requireNonNull(id, "Ticket ID cannot be null");
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        this.parkingSpot = Objects.requireNonNull(parkingSpot, "Parking spot cannot be null");
        this.entryTime = Objects.requireNonNull(entryTime, "Entry time cannot be null");
        this.exitTime = exitTime;
        this.fare = fare;
    }

    public static Ticket issue(Vehicle vehicle, ParkingSpot parkingSpot, LocalDateTime entryTime) {
        return new Ticket(vehicle, parkingSpot, entryTime);
    }

    public UUID getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public Fare getFare() {
        return fare;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public void setFare(Fare fare) {
        this.fare = fare;
    }

    public double getDurationHours() {
        if (exitTime == null) return 0.0;
        long minutes = Math.max(1, Duration.between(entryTime, exitTime).toMinutes());
        return Math.ceil(minutes / 60.0);
    }
 }
