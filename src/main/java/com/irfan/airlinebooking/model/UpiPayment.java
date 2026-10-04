package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.PaymentStatus;

import java.math.BigDecimal;

/** Simulated UPI payment. */
public class UpiPayment extends AbstractPayment {
    private String upiId;

    public UpiPayment() {
    }

    public UpiPayment(BigDecimal amount, String upiId) {
        super(amount);
        this.upiId = upiId;
    }

    @Override
    public PaymentStatus processPayment() {
        // A UPI id containing "invalid" simulates a rejected request.
        boolean ok = upiId != null && upiId.contains("@") && !upiId.contains("invalid");
        return settle(ok);
    }

    @Override
    public String describeMethod() {
        return "UPI";
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
}
