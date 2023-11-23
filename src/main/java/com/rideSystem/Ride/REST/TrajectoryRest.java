package com.rideSystem.Ride.REST;

import com.rideSystem.Ride.utils.Response;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path="/trajectory")
public interface TrajectoryRest {
    @PostMapping(path="/create/{rId}")
    public Response createTrajectory(@PathVariable("rId") Integer rideId, @RequestBody(required = true) Map<String,String> requestMap);

    @PostMapping(path="/accepted/{driverId}")
    public Response acceptedRideDriverView(@PathVariable("driverId") Integer driverId);

    @PostMapping(path="/passenger/{rideId}")
    public Response getTrajectory(@PathVariable("rideId") Integer rideId, @RequestBody(required=true) Map<String, String> requestMap);

    @DeleteMapping(path="/deleteAll")
    public Response deleteAllTrajectories();

    @DeleteMapping(path="/deleteByIds/{ids}")
    public Response deleteTrajectoriesByIds(@PathVariable List<Integer> ids);

    @GetMapping(path="/passengerLocation/{rid}")
    public Response getPassengerLocation(@PathVariable("rid") Integer rideId);
}
