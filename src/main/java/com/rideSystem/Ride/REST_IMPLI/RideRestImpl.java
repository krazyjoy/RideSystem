package com.rideSystem.Ride.REST_IMPLI;

import com.rideSystem.Ride.REST.RideRest;
import com.rideSystem.Ride.Service.RideService;
import com.rideSystem.Ride.utils.Response;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RideRestImpl implements RideRest {
    @Autowired
    RideService rideService;
    @Override
    public Response driverAcceptRideSession(Integer rideId, Map<String, String> requestMap){
        try{
            return rideService.driverAcceptRideSession(rideId, requestMap);
        }catch(Exception e){
            e.printStackTrace();
        }

        return new Response();
    }

    @Override
    public Response getRideSession(Integer rideId, Map<String, String> requestMap) {
        return rideService.getRideSession(rideId, requestMap);
    }
    @Override
    public Response requestRide(Map<String,String> requestMap){
        try{
            return rideService.requestRide(requestMap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed(rideRestImpl): requestRide", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response test(){
        Response response = new Response();
        response.setStatus(1);
        return response;
    }

    @Override
    public Response cancelRide(Integer rideId, Map<String,String> requestMap){
        try{
            return rideService.cancelRide(rideId,requestMap);
        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed(rideServiceImpl): cancel ride ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response requestOrder(Integer orderId, Integer rideId, Map<String, String> requestMap) {
        try{
            return rideService.requestOrder(orderId, rideId, requestMap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed (rideServiceImpl): request Order", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response listenToRideStatus(Integer rideId) {
        try{
            return rideService.listenToRideStatus(rideId);
        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed: listen to Ride Status ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response subscriptions(String topic){
        try{
            return rideService.subscriptions(topic);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  Response.failedResponse("failed (rideRestImpl): subscriptions", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public Response updateRideStatus(Integer rideId, Map<String, String> requestMap){
        try{
            return rideService.updateRideStatus(rideId, requestMap);
        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed (rideRestImpl): update ride status", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response getRideStatus(Integer rideId){
        try{
            return rideService.getRideStatus(rideId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  Response.failedResponse("failed (rideRestImpl): get ride status", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
