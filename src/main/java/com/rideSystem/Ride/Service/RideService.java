package com.rideSystem.Ride.Service;

import com.rideSystem.Ride.utils.Response;
import com.rideSystem.Ride.wrapper.RideWrapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface RideService {
    Response driverAcceptRideSession(Integer rideId, Map<String, String> requestMap);
    Response getRideSession(Integer rideId, Map<String, String> requestMap);
    Response requestRide(Map<String, String> requestMap);

    Response cancelRide(Integer rideId, Map<String,String> requestMap);

    Response requestOrder(Integer orderId, Integer rideId, Map<String,String> requestMap);

    Response subscriptions(String topic);

    Response listenToRideStatus(Integer rideId);

    Response updateRideStatus(Integer rideId, Map<String,String> requestMap);

    Response getRideStatus(Integer rideId);
}
