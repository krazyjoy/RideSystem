package com.rideSystem.Ride.controller;

import com.rideSystem.Ride.stripe.ChargeRequest;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


public interface CheckoutInterface {
    // @PostMapping
    //@RequestMapping("/checkout")
    public String checkout(Integer order_id, Model model) throws StripeException;
}