package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.util.Airports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

/**
 * A scheduled flight in the airline's network.
 * <p>
 * Flights are <b>date-less templates</b>: they carry only a departure and
 * arrival <i>time of day</i> and operate every day. The chosen travel date is
 * captured on the {@link Booking}. This is why the flight list never shows a
 * date.
 * <p>
 * {@code baseFare} is stored as the economy-equivalent fare; the final price
 * for a class is derived via {@link Seat#calculateFare(double)} /
 * {@link #getFare()}.
 */
public class Flight extends BaseEntity {
    private String flightNumber;
    private String airline;
    private String source;
    private String destination;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private double durationHours;
    private int stops;
    private SeatClass seatClass;
    private BigDecimal baseFare;

    public Flight() {
    }

    public Flight(String flightNumber, String airline, String source, String destination,
                  LocalTime departureTime, LocalTime arrivalTime,
                  double durationHours, int stops, SeatClass seatClass, BigDecimal baseFare) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.durationHours = durationHours;
        this.stops = stops;
        this.seatClass = seatClass;
        this.baseFare = baseFare;
    }

    /** Final fare for this flight's class (base * class multiplier * tax). */
    public BigDecimal getFare() {
        double amount = baseFare.doubleValue()
                * Seat.multiplierFor(seatClass)
                * (1 + Seat.TAX_RATE);
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public String getSourceCode() {
        return safeCode(source);
    }

    public String getDestinationCode() {
        return safeCode(destination);
    }

    private String safeCode(String city) {
        try {
            return Airports.byCity(city).getCode();
        } catch (RuntimeException e) {
            return city;
        }
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(double durationHours) {
        this.durationHours = durationHours;
    }

    public int getStops() {
        return stops;
    }

    public void setStops(int stops) {
        this.stops = stops;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public String getRoute() {
        return source + " -> " + destination;
    }

    @Override
    public String toString() {
        return String.format("%s %s | %s -> %s | %s | %s | %.1fh | %s %.2f",
                airline, flightNumber, source, destination, seatClass,
                departureTime == null ? "?" : departureTime.toString(),
                durationHours, "INR", getFare());
    }
}
