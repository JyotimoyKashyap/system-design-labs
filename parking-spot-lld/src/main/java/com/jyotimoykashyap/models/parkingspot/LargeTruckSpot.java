package com.jyotimoykashyap.models.parkingspot;

import com.jyotimoykashyap.models.Vehicle;
import java.util.Objects;

public class LargeTruckSpot implements ParkingSpot {

    private final int spotNumber;
    private final int floorNumber;
    private final int baseFee;
    private final int additionalFee;
    private boolean isOccupied;
    private Vehicle vehicle;

    public LargeTruckSpot(int spotNumber, int floorNumber, int baseFee, int additionalFee) {
        this.spotNumber = spotNumber;
        this.floorNumber = floorNumber;
        this.baseFee = baseFee;
        this.additionalFee = additionalFee;
        this.isOccupied = false;
        this.vehicle = null;
    }

    public LargeTruckSpot(int spotNumber, int floorNumber, int baseFee) {
        this(spotNumber, floorNumber, baseFee, 0);
    }

    @Override
    public int getSpotNumber() {
        return spotNumber;
    }

    @Override
    public int getFloorNumber() {
        return floorNumber;
    }

    @Override
    public synchronized boolean isOccupied() {
        return isOccupied;
    }

    @Override
    public synchronized Vehicle getVehicle() {
        return vehicle;
    }

    @Override
    public synchronized void occupy(Vehicle vehicle) {
        if (this.isOccupied) {
            throw new IllegalStateException("Spot " + spotNumber + " on floor " + floorNumber + " is already occupied!");
        }
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        this.isOccupied = true;
    }

    @Override
    public synchronized void vacate() {
        if (!this.isOccupied) {
            throw new IllegalStateException("Spot " + spotNumber + " on floor " + floorNumber + " is already vacant!");
        }
        this.vehicle = null;
        this.isOccupied = false;
    }

    @Override
    public int getBaseFee() {
        return baseFee;
    }

    @Override
    public int getAdditionalFee() {
        return additionalFee;
    }
}
