package com.rideSystem.Ride.REST;

import com.rideSystem.Ride.utils.Response;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping(path="/order")
public interface OrderRest {
    @PostMapping
    public Response createOrder(@RequestBody(required=true) Map<String,String> requestMap);
    @GetMapping(path="/{oId}")
    public Response getOrder(@PathVariable("oId") Integer orderId, @RequestBody(required = true) Map<String,String> requestMap);

}
