package com.rideSystem.Ride.Service;

import com.rideSystem.Ride.utils.Response;

import java.util.List;
import java.util.Map;

public interface TrajectoryService {
    Response createTrajectory(Integer rideId, Map<String,String> requestMap);

    Response acceptedRideDriverView(Integer driverId);

    Response getTrajectory(Integer rideId, Map<String,String> requestMap);

    Response deleteAllTrajectories();

    Response deleteTrajectoriesByIds(List<Integer> ids);

    Response getPassengerLocation(Integer rideId);
}
