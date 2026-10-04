package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.PaymentStatus;

import java.math.BigDecimal;

/** Simulated net-banking payment. */
public class NetBankingPayment extends AbstractPayment {
    private String bank;

    public NetBankingPayment() {
    }

    public NetBankingPayment(BigDecimal amount, String bank) {
        super(amount);
        this.bank = bank;
    }

    @Override
    public PaymentStatus processPayment() {
        return settle(bank != null && !bank.isBlank());
    }

    @Override
    public String describeMethod() {
        return "NetBanking";
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }
}
