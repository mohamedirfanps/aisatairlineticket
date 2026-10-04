package com.irfan.airlinebooking.service;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.exception.InvalidFlightException;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.repository.FlightNetworkGenerator;
import com.irfan.airlinebooking.repository.FlightRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for flight search and admin flight management.
 * <p>
 * Search uses the <b>Streams API</b> (filter / sorted) which keeps the
 * controller thin and demonstrates Java 8+ features.
 */
public class FlightService {

    private final FlightRepository flights = new FlightRepository();

    /** Flexible search; any blank criterion is ignored. Results sorted by fare. */
    public List<Flight> search(String source, String destination, SeatClass seatClass) {
        return flights.search(source, destination, seatClass).stream()
                .sorted(Comparator.comparing(Flight::getBaseFare))
                .collect(Collectors.toList());
    }

    /** Search sorted by duration (used by the "fastest" option). */
    public List<Flight> searchByDuration(String source, String destination, SeatClass seatClass) {
        return flights.search(source, destination, seatClass).stream()
                .sorted(Comparator.comparingDouble(Flight::getDurationHours))
                .collect(Collectors.toList());
    }

    public List<Flight> findAll() {
        return flights.findAll();
    }

    public Flight findById(Long id) {
        if (id == null) {
            throw new InvalidFlightException("No flight selected");
        }
        return flights.findById(id)
                .orElseThrow(() -> new InvalidFlightException("No flight with id " + id));
    }

    public boolean exists(Long id) {
        return flights.findById(id).isPresent();
    }

    /** Admin: add a new flight. */
    public Flight addFlight(Flight flight) {
        if (flight.getSource().equalsIgnoreCase(flight.getDestination())) {
            throw new InvalidFlightException("Source and destination must differ");
        }
        return flights.save(flight);
    }

    /** Admin: update an existing flight. */
    public Flight updateFlight(Flight flight) {
        if (flight.getId() == null) {
            throw new InvalidFlightException("Cannot update a flight without an id");
        }
        return flights.update(flight);
    }

    /** Admin: delete a flight. */
    public boolean deleteFlight(Long id) {
        return flights.deleteById(id);
    }

    public int count() {
        return flights.count();
    }

    /**
     * Generates the full flight network and persists it. Returns the number of
     * flights created. Idempotent: does nothing if flights already exist.
     */
    public int seedNetwork() {
        if (flights.count() > 0) {
            return 0;
        }
        List<Flight> network = new FlightNetworkGenerator().generate();
        int count = 0;
        for (Flight f : network) {
            flights.save(f);
            count++;
        }
        return count;
    }
}
