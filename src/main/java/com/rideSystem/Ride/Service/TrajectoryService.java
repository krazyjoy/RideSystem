package com.rideSystem.Ride.Service;

import com.rideSystem.Ride.utils.Response;

import java.util.Map;

public interface TrajectoryService {
    Response createTrajectory(Integer rideId, Map<String,String> requestMap);
}
