package com.irfan.airlinebooking.exception;

/** Thrown when a simulated payment does not succeed. */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
