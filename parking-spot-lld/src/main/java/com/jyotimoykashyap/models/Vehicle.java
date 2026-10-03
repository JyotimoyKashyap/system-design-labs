package com.jyotimoykashyap.models;

import java.util.Objects;

public class Vehicle {
    private final String licensePlate;
    private final VehicleSize vehicleSize;

    public Vehicle(String licensePlate, VehicleSize vehicleSize) {
        this.licensePlate = Objects.requireNonNull(licensePlate, "License plate cannot be null").trim();
        this.vehicleSize = Objects.requireNonNull(vehicleSize, "VehicleSie cannot be null");
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }
}
