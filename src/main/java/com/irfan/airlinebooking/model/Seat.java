package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.SeatClass;

import java.math.BigDecimal;

/**
 * Abstract seat.
 * <p>
 * Demonstrates <b>abstraction</b> ({@link #calculateFare(double)}) and is the
 * parent of {@link EconomySeat} and {@link BusinessSeat} (<b>inheritance</b>).
 * The fare method is overridden by each subclass (<b>runtime polymorphism</b>).
 */
public abstract class Seat extends BaseEntity {
    /** Statutory/airport tax rate applied to every fare. */
    public static final double TAX_RATE = 0.18;

    private String seatNumber;
    private boolean booked;

    protected Seat() {
    }

    protected Seat(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    /** Concrete class decides the multiplier on the flight base fare. */
    public abstract double getFareMultiplier();

    public abstract SeatClass getSeatClass();

    /** Factory-ish helper: the multiplier associated with a class. */
    public static double multiplierFor(SeatClass seatClass) {
        return seatClass == SeatClass.BUSINESS ? 3.2 : 1.0;
    }

    /** Final fare = base fare * class multiplier * (1 + tax). */
    public BigDecimal calculateFare(double baseFare) {
        double amount = baseFare * getFareMultiplier() * (1 + TAX_RATE);
        return BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    @Override
    public String toString() {
        return getSeatClass() + "-" + seatNumber + (booked ? " (booked)" : "");
    }
}
