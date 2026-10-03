package com.jyotimoykashyap.models.parkingspot;

import com.jyotimoykashyap.models.Vehicle;

public interface ParkingSpot {
    int getSpotNumber();
    int getFloorNumber();
    boolean isOccupied();
    Vehicle getVehicle();
    void occupy(Vehicle vehicle);
    void vacate();
    int getBaseFee();
    int getAdditionalFee();
}
