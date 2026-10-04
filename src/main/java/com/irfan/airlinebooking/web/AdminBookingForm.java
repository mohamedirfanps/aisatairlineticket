package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.enums.BookingStatus;
import com.irfan.airlinebooking.enums.SeatClass;

import java.math.BigDecimal;

/**
 * Form object for admin editing of a booking.
 * <p>
 * Note: there is deliberately <b>no PNR setter used for updates</b> — the PNR
 * is immutable and is only shown read-only.
 */
public class AdminBookingForm {
    private Long id;
    private String pnr;
    private String passengerName;
    private Integer passengerAge;
    private String travelDate;
    private SeatClass seatClass;
    private String seatNumber;
    private boolean extraLuggage;
    private boolean meals;
    private BigDecimal fare;
    private BookingStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getPassengerAge() {
        return passengerAge;
    }

    public void setPassengerAge(Integer passengerAge) {
        this.passengerAge = passengerAge;
    }

    public String getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(String travelDate) {
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
}
