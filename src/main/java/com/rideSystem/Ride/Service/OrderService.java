package com.rideSystem.Ride.Service;

import com.rideSystem.Ride.POJO.Order;
import com.rideSystem.Ride.utils.Response;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Response createOrder(Map<String,String> requestMap);
    Response getOrder(Integer orderId, Map<String,String> requestMap);

    Order payment(Integer orderId);

    Response paymentResult(Integer orderId, Map<String, Boolean> success);

    Response deleteAllOrders();

    Response deleteOrdersByIds(List<Integer> ids);
}
