package com.rideSystem.Ride.REST;

import com.rideSystem.Ride.utils.Response;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping(path="/ride")
public interface RideRest {
    @PutMapping(path="/rid")
    public Response test();
    @PutMapping(path="/{rId}")
    public Response driverAcceptRideSession(@PathVariable("rId") Integer rideId, @RequestBody(required = true) Map<String,String> requestMap);

    @GetMapping(path="/{rId}")
    public Response getRideSession(@PathVariable("rId") Integer rideId, @RequestBody(required = true) Map<String, String> requestMap);

    @PostMapping
    public Response requestRide(@RequestBody(required = true) Map<String,String> requestMap);
    @PutMapping(path="/cancel/{rId}")
    public Response cancelRide(@PathVariable("rId") Integer rideId, @RequestBody(required=true) Map<String,String> requestMap);

    @PutMapping(path="/{oId}/{rId}")
    public Response requestOrder(@PathVariable("oId") Integer orderId, @PathVariable("rId") Integer rideId, @RequestBody(required = true) Map<String,String> requestMap);

}
