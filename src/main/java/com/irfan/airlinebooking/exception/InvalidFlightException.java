package com.irfan.airlinebooking.exception;

/** Thrown when a flight is missing or otherwise cannot be used for a booking. */
public class InvalidFlightException extends RuntimeException {
    public InvalidFlightException(String message) {
        super(message);
    }
}
