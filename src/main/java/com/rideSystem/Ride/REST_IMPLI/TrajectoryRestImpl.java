package com.rideSystem.Ride.REST_IMPLI;

import com.rideSystem.Ride.REST.TrajectoryRest;
import com.rideSystem.Ride.Service.TrajectoryService;
import com.rideSystem.Ride.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
