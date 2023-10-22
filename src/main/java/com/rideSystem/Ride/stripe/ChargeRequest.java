package com.rideSystem.Ride.stripe;

import lombok.Data;

@Data
public class ChargeRequest {
    public enum Currency{
        EUR,
        USD
    }
    private String description;
    private Integer amount;
    private Currency currency;
    private String stripeEmail;
    private String stripeToken;
    private Integer orderId;
    public String getDescription(){
        return description;
    }

    public int getAmount() {
        return amount;
    }

    public String getStripeEmail() {
        return stripeEmail;
    }

    public String getStripeToken() {
        return stripeToken;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
