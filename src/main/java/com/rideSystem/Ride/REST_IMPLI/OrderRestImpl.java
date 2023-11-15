package com.rideSystem.Ride.REST_IMPLI;

import com.rideSystem.Ride.POJO.Order;
import com.rideSystem.Ride.REST.OrderRest;
import com.rideSystem.Ride.Service.OrderService;
import com.rideSystem.Ride.Service.RideService;
import com.rideSystem.Ride.Service_IMPL.OrderServiceImpl;
import com.rideSystem.Ride.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
public class OrderRestImpl implements OrderRest {
    @Autowired
    OrderService orderService;
    @Override
    public Response createOrder(@RequestBody(required = true) Map<String,String> requestMap){
        try{
            return orderService.createOrder(requestMap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed: create order rest impl", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response getOrder(Integer orderId, Map<String, String> requestMap) {
        try{
            return orderService.getOrder(orderId, requestMap);
        }catch(Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed: get order rest impl", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Order payment(@RequestParam("order_id") Integer orderId) {
        try {
            return orderService.payment(orderId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public Response paymentResult(@PathVariable("oId") Integer orderId, Map<String,Boolean> success){
        try{
            return orderService.paymentResult(orderId, success);
        }catch(Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed (OrderRestImpl) : payment result", HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
