package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.PaymentStatus;

import java.math.BigDecimal;

/** Simulated card payment. A card ending in "0000" always fails. */
public class CardPayment extends AbstractPayment {
    private String cardNumber;
    private String cardHolder;

    public CardPayment() {
    }

    public CardPayment(BigDecimal amount, String cardNumber, String cardHolder) {
        super(amount);
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
    }

    @Override
    public PaymentStatus processPayment() {
        String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
        boolean ok = digits.length() >= 4 && !digits.endsWith("0000");
        return settle(ok);
    }

    @Override
    public String describeMethod() {
        return "Card";
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }
}
