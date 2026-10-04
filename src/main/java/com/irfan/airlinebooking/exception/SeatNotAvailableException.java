package com.irfan.airlinebooking.exception;

/** Thrown when a requested seat has already been taken. */
public class SeatNotAvailableException extends RuntimeException {
    public SeatNotAvailableException(String message) {
        super(message);
    }
}
