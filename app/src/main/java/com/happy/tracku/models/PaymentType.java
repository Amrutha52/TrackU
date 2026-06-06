package com.happy.tracku.models;

public class PaymentType {

    private final String name;
    private final int value;

    public PaymentType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name; // Spinner displays this text
    }
}
