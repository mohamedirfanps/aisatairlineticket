package com.irfan.airlinebooking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/** Tests for the Seat hierarchy and fare polymorphism. */
class SeatTest {

    @Test
    void economySeatAppliesTaxButNoMultiplier() {
        EconomySeat seat = new EconomySeat("1A");
        BigDecimal fare = seat.calculateFare(1000);
        assertEquals(1180.00, fare.doubleValue(), 0.001);
    }

    @Test
    void businessSeatIsMoreExpensive() {
        BusinessSeat business = new BusinessSeat("1A");
        EconomySeat economy = new EconomySeat("1A");
        assertNotEquals(economy.calculateFare(1000), business.calculateFare(1000));
        assertEquals(1000 * 3.2 * 1.18, business.calculateFare(1000).doubleValue(), 0.001);
    }

    @Test
    void multiplierForMatchesSubclasses() {
        assertEquals(1.0, Seat.multiplierFor(com.irfan.airlinebooking.enums.SeatClass.ECONOMY));
        assertEquals(3.2, Seat.multiplierFor(com.irfan.airlinebooking.enums.SeatClass.BUSINESS));
    }
}
