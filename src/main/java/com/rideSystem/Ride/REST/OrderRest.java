package com.rideSystem.Ride.REST;

import com.rideSystem.Ride.POJO.Order;
import com.rideSystem.Ride.utils.Response;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path="/order")
public interface OrderRest {
    @PostMapping
    public Response createOrder(@RequestBody(required=true) Map<String,String> requestMap);
    @GetMapping(path="/{oId}")
    public Response getOrder(@PathVariable("oId") Integer orderId, @RequestBody(required = true) Map<String,String> requestMap);

    @GetMapping(path="/payment")
    public Order payment(@RequestParam("order_id") Integer orderId);

    @PostMapping(path="/payment/result/{oId}")
    public Response paymentResult(@PathVariable("oId") Integer orderId,@RequestBody(required = true) Map<String, Boolean> success);

    @DeleteMapping(path="/deleteAllOrders")
    public Response deleteAllOrders();

    @DeleteMapping(path="/deleteByIds/{orderIds}")
    public Response deleteOrdersByIds(@PathVariable List<Integer> orderIds);
}
