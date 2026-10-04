package com.irfan.airlinebooking.exception;

/** Thrown when a requested entity cannot be found. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
