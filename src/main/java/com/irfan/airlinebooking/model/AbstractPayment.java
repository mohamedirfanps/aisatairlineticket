package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Common logic for the simulated payment methods.
 * Uses a seeded {@link Random} so demos are repeatable: a card ending in
 * "0000" always fails, anything else succeeds.
 */
public abstract class AbstractPayment extends Payment {
    private static final Random RANDOM = new Random(42);

    protected AbstractPayment() {
    }

    protected AbstractPayment(BigDecimal amount) {
        super(amount);
    }

    /** Runs the simulated gateway and records the result.
     *
     * @param forced TRUE = force success, FALSE = force failure, null = random
     */
    protected PaymentStatus settle(Boolean forced) {
        boolean success = forced != null ? forced : RANDOM.nextInt(100) < 95;
        setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        setPaidAt(LocalDateTime.now());
        return getStatus();
    }
}
