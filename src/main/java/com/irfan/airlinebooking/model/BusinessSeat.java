package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.SeatClass;

/** Business-class seat. Base fare multiplier is 3.2x. */
public class BusinessSeat extends Seat {

    public BusinessSeat() {
        super();
    }

    public BusinessSeat(String seatNumber) {
        super(seatNumber);
    }

    @Override
    public double getFareMultiplier() {
        return 3.2;
    }

    @Override
    public SeatClass getSeatClass() {
        return SeatClass.BUSINESS;
    }
}
