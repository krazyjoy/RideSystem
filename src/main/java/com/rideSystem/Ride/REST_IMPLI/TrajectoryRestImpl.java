package com.rideSystem.Ride.REST_IMPLI;

import com.rideSystem.Ride.REST.TrajectoryRest;
import com.rideSystem.Ride.Service.TrajectoryService;
import com.rideSystem.Ride.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TrajectoryRestImpl implements TrajectoryRest {
    @Autowired
    TrajectoryService trajectoryService;

    @Override
    public Response createTrajectory(Integer rideId, Map<String, String> requestMap){
        try{
            return trajectoryService.createTrajectory(rideId, requestMap);
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed (trajectoryRestImpl", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response acceptedRideDriverView(Integer driverId) {
        try{
            return trajectoryService.acceptedRideDriverView(driverId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("Failed at acceptedRideDriverView", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response getTrajectory(Integer rideId, Map<String, String> requestMap) {
        try{
            return trajectoryService.getTrajectory(rideId, requestMap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("Failed at get trajectory", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public Response deleteAllTrajectories(){
        try{

            return trajectoryService.deleteAllTrajectories();
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed at trajectory rest Impl", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response deleteTrajectoriesByIds(List<Integer> ids){
        try{
            return trajectoryService.deleteTrajectoriesByIds(ids);
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed at trajectory rest Impl", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response getPassengerLocation(Integer rideId){
        try{
            return trajectoryService.getPassengerLocation(rideId);
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed at trajectory rest impl", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
