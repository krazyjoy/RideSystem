package com.rideSystem.Ride.REST;

import com.rideSystem.Ride.utils.Response;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping(path="/trajectory")
public interface TrajectoryRest {
    @PostMapping(path="/create/{rId}")
    public Response createTrajectory(@PathVariable("rId") Integer rideId, @RequestBody(required = true) Map<String,String> requestMap);
}
