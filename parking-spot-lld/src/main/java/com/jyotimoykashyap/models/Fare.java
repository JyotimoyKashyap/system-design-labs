package com.jyotimoykashyap.models;

public class Fare {
    private int baseFare;
    private int additionalFare;
    private int serviceFee;
    private int taxes;

    public Fare() {
        this.baseFare = 0;
        this.additionalFare = 0;
        this.serviceFee = 0;
        this.taxes = 0;
    }

    public int getTotalFare() {
        return baseFare + additionalFare + serviceFee + taxes;
    }

    public int getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(int baseFare) {
        this.baseFare = baseFare;
    }

    public int getAdditionalFare() {
        return additionalFare;
    }

    public void setAdditionalFare(int additionalFare) {
        this.additionalFare = additionalFare;
    }

    public int getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(int serviceFee) {
        this.serviceFee = serviceFee;
    }

    public int getTaxes() {
        return taxes;
    }

    public void setTaxes(int taxes) {
        this.taxes = taxes;
    }
}
