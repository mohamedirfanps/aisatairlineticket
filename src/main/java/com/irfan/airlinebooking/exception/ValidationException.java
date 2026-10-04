package com.irfan.airlinebooking.exception;

/** Thrown when input validation fails (empty fields, bad dates, ...). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
