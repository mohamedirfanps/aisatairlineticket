package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.SeatClass;

/** Economy-class seat. Base fare multiplier is 1.0. */
public class EconomySeat extends Seat {

    public EconomySeat() {
        super();
    }

    public EconomySeat(String seatNumber) {
        super(seatNumber);
    }

    @Override
    public double getFareMultiplier() {
        return 1.0;
    }

    @Override
    public SeatClass getSeatClass() {
        return SeatClass.ECONOMY;
    }
}
