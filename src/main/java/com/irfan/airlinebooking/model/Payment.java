package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Abstract payment.
 * <p>
 * {@link #processPayment()} is implemented by every subclass, giving
 * <b>runtime polymorphism</b>. Also exposes the {@link #describeMethod()}
 * hook so the UI can label the payment type.
 */
public abstract class Payment extends BaseEntity {
    private Long bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    protected Payment() {
    }

    protected Payment(BigDecimal amount) {
        this.amount = amount;
    }

    /** Each payment method implements its own (simulated) processing. */
    public abstract PaymentStatus processPayment();

    /** Human readable name, e.g. "UPI". */
    public abstract String describeMethod();

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
