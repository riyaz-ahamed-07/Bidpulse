package com.bidpulse.model;

public enum TransactionType {
    HOLD,      // reserve funds
    CHARGE,    // take money for winner
    RELEASE,    // release reserved funds
    DEPOSIT
}