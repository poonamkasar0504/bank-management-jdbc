package model;

import java.math.BigDecimal;

public class Account {
    private int accNo;
    private String holderName;
    private BigDecimal balance;

    public Account(int accNo, String holderName, BigDecimal balance) {
        this.accNo = accNo;
        this.holderName = holderName;
        this.balance = balance;
    }

    public int getAccNo() { return accNo; }
    public String getHolderName() { return holderName; }
    public BigDecimal getBalance() { return balance; }

    @Override
    public String toString() {
        return "Account No: " + accNo + " | Name: " + holderName + " | Balance: ₹" + balance;
    }
}