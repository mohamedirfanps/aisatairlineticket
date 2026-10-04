package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.SeatClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A printable ticket derived from a {@link Booking}. */
public class Ticket extends BaseEntity {
    private Long bookingId;
    private String pnr;
    private String passengerName;
    private String flightNumber;
    private String source;
    private String destination;
    private LocalDate travelDate;
    private SeatClass seatClass;
    private String seatNumber;
    private boolean extraLuggage;
    private boolean meals;
    private BigDecimal addOnAmount;
    private BigDecimal fare;
    private LocalDateTime issuedAt;

    public Ticket() {
    }

    public Ticket(Booking booking) {
        this.bookingId = booking.getId();
        this.pnr = booking.getPnr();
        this.passengerName = booking.getPassengerName();
        this.flightNumber = booking.getFlightNumber();
        this.source = booking.getSource();
        this.destination = booking.getDestination();
        this.travelDate = booking.getTravelDate();
        this.seatClass = booking.getSeatClass();
        this.seatNumber = booking.getSeatNumber();
        this.extraLuggage = booking.isExtraLuggage();
        this.meals = booking.isMeals();
        this.addOnAmount = booking.getAddOnAmount();
        this.fare = booking.getFare();
        this.issuedAt = LocalDateTime.now();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
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

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
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

    public BigDecimal getFare() {
        return fare;
    }

    public void setFare(BigDecimal fare) {
        this.fare = fare;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    /** Human-readable add-on summary. */
    public String getAddOnSummary() {
        StringBuilder sb = new StringBuilder();
        if (extraLuggage) {
            sb.append("Extra luggage");
        }
        if (meals) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Meals");
        }
        return sb.length() == 0 ? "None" : sb.toString();
    }

    /** Renders the ticket as text for the console / file output. */
    public String render() {
        return """
                ============================================
                            E-TICKET
                ============================================
                 PNR           : %s
                 Passenger     : %s
                 Flight        : %s
                 Route         : %s -> %s
                 Travel date   : %s
                 Seat          : %s
                 Add-ons       : %s (%.2f)
                 Fare (INR)    : %.2f
                 Issued at     : %s
                ============================================
                """.formatted(pnr, passengerName, flightNumber, source, destination,
                travelDate, seatNumber, getAddOnSummary(),
                addOnAmount == null ? 0.0 : addOnAmount.doubleValue(),
                fare.doubleValue(), issuedAt);
    }
}
