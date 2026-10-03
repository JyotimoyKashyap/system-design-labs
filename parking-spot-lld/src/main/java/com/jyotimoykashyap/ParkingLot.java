package com.jyotimoykashyap;

import com.jyotimoykashyap.models.Fare;
import com.jyotimoykashyap.models.Ticket;
import com.jyotimoykashyap.models.Vehicle;
import com.jyotimoykashyap.models.VehicleSize;
import com.jyotimoykashyap.models.parkingspot.ParkingSpot;
import com.jyotimoykashyap.pricingengine.FareCalculator;
import com.jyotimoykashyap.repository.ParkingDataRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ParkingLot {
    private final String name;
    private final ParkingDataRepository repository;
    private final FareCalculator calculator;

    public ParkingLot(String name, ParkingDataRepository repository, FareCalculator calculator) {
        this.name = Objects.requireNonNull(name);
        this.repository = Objects.requireNonNull(repository);
        this.calculator = Objects.requireNonNull(calculator);
    }

    public synchronized Ticket assignVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null");

        Optional<ParkingSpot> spotOpt = repository.findAndClaimSpot(vehicle);
        if (spotOpt.isEmpty()) {
            throw new IllegalStateException("Parking Full");
        }

        ParkingSpot assignedSpot = spotOpt.get();
        Ticket ticket = Ticket.issue(vehicle, assignedSpot, LocalDateTime.now());
        repository.recordTicket(ticket);
        return ticket;
    }

    /**
     * Exit Gate Workflow: Computes fare, releases spot, and closes ticket via ticket ID.
     */
    public synchronized Ticket vacateVehicle(UUID ticketId) {
        Objects.requireNonNull(ticketId, "Ticket ID cannot be null");

        Ticket activeTicket = repository.getActiveTicket(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket ID: " + ticketId));

        return settleAndReleaseTicket(activeTicket);
    }

    /**
     * Exit Gate Workflow (ANPR Camera Scan): Releases vehicle directly by license plate / vehicle.
     */
    public synchronized Ticket vacateVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null");

        Ticket activeTicket = repository.getTicketByVehicle(vehicle)
                .orElseThrow(() -> new IllegalArgumentException("No active ticket found for vehicle: " + vehicle.getLicensePlate()));

        return settleAndReleaseTicket(activeTicket);
    }

    private Ticket settleAndReleaseTicket(Ticket ticket) {
        LocalDateTime exitTime = LocalDateTime.now();
        ticket.setExitTime(exitTime);

        // Compute dynamic fare across configured strategy pipeline
        Fare finalFare = calculator.calculateFare(ticket);
        ticket.setFare(finalFare);

        // Deallocate spot and update repository
        repository.releaseSpot(ticket);

        return ticket;
    }

    public String getName() {
        return name;
    }

    public int getAvailableSpots(VehicleSize size) {
        return repository.getAvailableCount(size);
    }
}
