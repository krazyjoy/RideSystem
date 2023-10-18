package com.rideSystem.Ride.REST_IMPLI;

import com.rideSystem.Ride.REST.OrderRest;
import com.rideSystem.Ride.Service.OrderService;
import com.rideSystem.Ride.Service.RideService;
import com.rideSystem.Ride.Service_IMPL.OrderServiceImpl;
import com.rideSystem.Ride.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    ;


}
