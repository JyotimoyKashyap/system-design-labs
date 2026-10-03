package com.jyotimoykashyap.models.parkingspot;

import com.jyotimoykashyap.models.Vehicle;
import java.util.Objects;

public class TwoWheelerSpot implements ParkingSpot {

    private final int spotNumber;
    private final int floorNumber;
    private final int baseFee;
    private boolean isOccupied;
    private Vehicle vehicle;

    public TwoWheelerSpot(int spotNumber, int floorNumber, int baseFee) {
        this.spotNumber = spotNumber;
        this.floorNumber = floorNumber;
        this.baseFee = baseFee;
        this.isOccupied = false;
        this.vehicle = null;
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
        return 0;
    }
}
