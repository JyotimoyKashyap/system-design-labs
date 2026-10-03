package com.jyotimoykashyap.repository;

import com.jyotimoykashyap.models.Ticket;
import com.jyotimoykashyap.models.Vehicle;
import com.jyotimoykashyap.models.VehicleSize;
import com.jyotimoykashyap.models.parkingspot.ParkingSpot;

import javax.swing.text.html.Option;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ParkingDataRepository {
    private final Map<VehicleSize, Deque<ParkingSpot>> availableSpotsBySize = new ConcurrentHashMap<>();
    private final Map<UUID, Ticket> activeTicketsById = new ConcurrentHashMap<>();
    private final Map<Vehicle, Ticket> activeTicketByVehicle = new ConcurrentHashMap<>();

    public ParkingDataRepository() {
        for (VehicleSize size : VehicleSize.values()) {
            availableSpotsBySize.put(size, new ConcurrentLinkedDeque<>());
        }
    }

    public void registerSpot(ParkingSpot spot, VehicleSize supportedSize) {
        Deque<ParkingSpot> queue = availableSpotsBySize.get(supportedSize);
        queue.addLast(spot);
    }

    public synchronized Optional<ParkingSpot> findAndClaimSpot(Vehicle vehicle) {
        if (activeTicketByVehicle.containsKey(vehicle)) {
            throw new IllegalStateException("Vehicle " + vehicle.getLicensePlate() + " is already parked inside!");
        }

        Deque<ParkingSpot> queue = availableSpotsBySize.get(vehicle.getVehicleSize());

        if (queue == null || queue.isEmpty()) {
            return Optional.empty();
        }

        ParkingSpot spot = queue.pollFirst();
        if (spot != null) {
            spot.occupy(vehicle);
            return Optional.of(spot);
        }
        return Optional.empty();
    }

    public synchronized void recordTicket(Ticket ticket) {
        activeTicketByVehicle.put(ticket.getVehicle(), ticket);
        activeTicketsById.put(ticket.getId(), ticket);
    }

    public Optional<Ticket> getActiveTicket(UUID ticketId) {
        return Optional.ofNullable(activeTicketsById.get(ticketId));
    }

    public Optional<Ticket> getTicketByVehicle(Vehicle vehicle) {
        return Optional.ofNullable(activeTicketByVehicle.get(vehicle));
    }

    /**
     * Releases an occupied spot back into the available pool upon exit.
     */
    public synchronized void releaseSpot(Ticket ticket) {
        ParkingSpot spot = ticket.getParkingSpot();
        Vehicle vehicle = ticket.getVehicle();

        spot.vacate();
        activeTicketByVehicle.remove(vehicle);
        activeTicketsById.remove(ticket.getId());

        // Return spot to the available queue for its vehicle category
        Deque<ParkingSpot> queue = availableSpotsBySize.get(vehicle.getVehicleSize());
        if (queue != null) {
            queue.addFirst(spot); // Recycled spot returned to front
        }
    }

    public int getAvailableCount(VehicleSize size) {
        Deque<ParkingSpot> queue = availableSpotsBySize.get(size);
        return queue == null ? 0 : queue.size();
    }
}
