package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.model.Flight;

import java.math.BigDecimal;

/** A search result row: a flight plus its computed add-on and total amounts. */
public class FlightRow {
    private final Flight flight;
    private final BigDecimal addOnAmount;
    private final BigDecimal total;

    public FlightRow(Flight flight, BigDecimal addOnAmount, BigDecimal total) {
        this.flight = flight;
        this.addOnAmount = addOnAmount;
        this.total = total;
    }

    public Flight getFlight() {
        return flight;
    }

    public BigDecimal getAddOnAmount() {
        return addOnAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
