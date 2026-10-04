package com.irfan.airlinebooking.exception;

/** Thrown when credentials are missing, unknown, or incorrect. */
public class InvalidLoginException extends RuntimeException {
    public InvalidLoginException(String message) {
        super(message);
    }
}
