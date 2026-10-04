package com.irfan.airlinebooking.service;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.model.BusinessSeat;
import com.irfan.airlinebooking.model.EconomySeat;
import com.irfan.airlinebooking.model.Seat;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the seat map for a flight and answers availability questions.
 * Creates {@link EconomySeat}/{@link BusinessSeat} objects, exercising
 * polymorphism via the {@link Seat} base type.
 */
public class SeatService {

    /** Six rows of 4 seats for economy, three rows of 4 for business. */
    public static final int ECONOMY_ROWS = 6;
    public static final int BUSINESS_ROWS = 3;

    /** Creates the seats for a class. */
    public List<Seat> seatMap(SeatClass seatClass) {
        List<Seat> seats = new ArrayList<>();
        if (seatClass == SeatClass.BUSINESS) {
            for (int row = 1; row <= BUSINESS_ROWS; row++) {
                for (char col : new char[]{'A', 'B', 'C', 'D'}) {
                    seats.add(new BusinessSeat(row + String.valueOf(col)));
                }
            }
        } else {
            for (int row = 1; row <= ECONOMY_ROWS; row++) {
                for (char col : new char[]{'A', 'B', 'C', 'D'}) {
                    seats.add(new EconomySeat(row + String.valueOf(col)));
                }
            }
        }
        return seats;
    }

    public boolean isValidSeat(SeatClass seatClass, String seatNumber) {
        if (seatNumber == null) {
            return false;
        }
        return seatMap(seatClass).stream()
                .anyMatch(s -> s.getSeatNumber().equalsIgnoreCase(seatNumber));
    }
}
