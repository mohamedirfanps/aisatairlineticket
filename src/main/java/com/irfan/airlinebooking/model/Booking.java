package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.BookingStatus;
import com.irfan.airlinebooking.enums.SeatClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A confirmed reservation.
 * <p>
 * The PNR is generated from a <b>static</b>, thread-safe counter so it is
 * unique across concurrent bookings. The travel date lives here (flights are
 * date-less templates that operate every day).
 */
public class Booking extends BaseEntity {
    /** Static counter used for PNR generation (shared by all bookings). */
    private static final AtomicInteger PNR_COUNTER = new AtomicInteger(1000);

    private String pnr;
    private Long userId;
    private Long flightId;
    private String flightNumber;
    private String source;
    private String destination;
    private LocalDate travelDate;
    private SeatClass seatClass;
    private String seatNumber;
    private String passengerName;
    private int passengerAge;
    private boolean extraLuggage;
    private boolean meals;
    private BigDecimal addOnAmount = BigDecimal.ZERO;
    private BigDecimal fare;
    private BookingStatus status;
    private LocalDateTime bookedAt;

    public Booking() {
    }

    /** Builds a booking and assigns a fresh PNR. */
    public Booking(Long userId, Long flightId, String passengerName, int passengerAge) {
        this.pnr = generatePnr();
        this.userId = userId;
        this.flightId = flightId;
        this.passengerName = passengerName;
        this.passengerAge = passengerAge;
        this.status = BookingStatus.CONFIRMED;
        this.bookedAt = LocalDateTime.now();
    }

    /** Generates a PNR such as "PNR-1001". */
    public static String generatePnr() {
        return "PNR-" + PNR_COUNTER.incrementAndGet();
    }

    /**
     * Advances the counter so newly generated PNRs do not collide with rows
     * already stored in a persistent database. Call once at startup.
     */
    public static void seedPnrCounter(int highestExistingSequence) {
        PNR_COUNTER.updateAndGet(current -> Math.max(current, highestExistingSequence));
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
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

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public boolean isExtraLuggage() {
        return extraLuggage;
    }

    public void setExtraLuggage(boolean extraLuggage) {
        this.extraLuggage = extraLuggage;
    }

    public boolean isMeals() {
        return meals;
    }

    public void setMeals(boolean meals) {
        this.meals = meals;
    }

    public BigDecimal getAddOnAmount() {
        return addOnAmount;
    }

    public void setAddOnAmount(BigDecimal addOnAmount) {
        this.addOnAmount = addOnAmount;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public int getPassengerAge() {
        return passengerAge;
    }

    public void setPassengerAge(int passengerAge) {
        this.passengerAge = passengerAge;
    }

    public BigDecimal getFare() {
        return fare;
    }

    public void setFare(BigDecimal fare) {
        this.fare = fare;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    @Override
    public String toString() {
        return String.format("%s | %s -> %s | %s %s | %s | %s",
                pnr, source, destination, seatClass, seatNumber, fare, status);
    }
}
