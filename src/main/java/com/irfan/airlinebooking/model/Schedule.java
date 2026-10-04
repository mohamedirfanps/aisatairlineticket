package com.irfan.airlinebooking.model;

import java.time.LocalDate;

/**
 * A daily schedule entry that groups flights running on a given route/date.
 * Kept intentionally small; flights themselves carry the concrete times.
 */
public class Schedule extends BaseEntity {
    private Long flightId;
    private LocalDate travelDate;
    private int availableSeats;

    public Schedule() {
    }

    public Schedule(Long flightId, LocalDate travelDate, int availableSeats) {
        this.flightId = flightId;
        this.travelDate = travelDate;
        this.availableSeats = availableSeats;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}
