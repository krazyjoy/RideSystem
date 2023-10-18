package com.rideSystem.Ride.Service;

import com.rideSystem.Ride.utils.Response;

import java.util.Map;

public interface OrderService {
    Response createOrder(Map<String,String> requestMap);
    Response getOrder(Integer orderId, Map<String,String> requestMap);

}
