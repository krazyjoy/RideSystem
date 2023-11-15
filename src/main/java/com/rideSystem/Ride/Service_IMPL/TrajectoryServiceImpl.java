package com.rideSystem.Ride.Service_IMPL;

import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.DAO.TrajectoryDao;
import com.rideSystem.Ride.POJO.Ride;
import com.rideSystem.Ride.POJO.RideStatus;
import com.rideSystem.Ride.POJO.Trajectory;
import com.rideSystem.Ride.REST.TrajectoryRest;
import com.rideSystem.Ride.Service.TrajectoryService;
import com.rideSystem.Ride.utils.ObjectToHashMapConverter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import com.rideSystem.Ride.utils.Response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Slf4j
@Service
public class TrajectoryServiceImpl  implements TrajectoryService{
    @Autowired
    RideDao rideDao;
    @Autowired
    TrajectoryDao trajectoryDao;
    public TrajectoryServiceImpl(RideDao rideDao, TrajectoryDao trajectoryDao){
        rideDao = this.rideDao;
        trajectoryDao = this.trajectoryDao;
    }
    @Override
    public Response createTrajectory(Integer rideId, Map<String,String> requestMap){
        try{
            if(rideDao.existsById(rideId)){
                if(validateTrajectoryCreationMap(requestMap)){
                    Trajectory trajectory = new Trajectory();
                    trajectory.setCorrespondedRideId(rideId);
                    Float curr_lat = Float.parseFloat(requestMap.get("gpsLatitude"));
                    Float curr_long = Float.parseFloat(requestMap.get("gpsLongitude"));
                    trajectory.setGPSLatitude(curr_lat);
                    trajectory.setGPSLongitude(curr_long);
                    trajectory.setTimeSequence(LocalDateTime.now());
                    trajectoryDao.save(trajectory);

                    updateRideStatus(rideId, curr_lat, curr_long);
                    Response success_trajectory_response = Response.successResponse();
                    Map<String, Object> trajectoryMap = ObjectToHashMapConverter.convertObjectToMap(trajectory);
                    success_trajectory_response.setData(trajectoryMap);
                    return success_trajectory_response;
                }
                else{
                    return Response.failedResponse("invalid create Trajectory request map", HttpStatus.BAD_REQUEST);
                }
            }
            return Response.failedResponse("non existed rideId in create trajectory request param", HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed: trajectory service impl", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public boolean validateTrajectoryCreationMap(Map<String,String> requestMap){
        if(requestMap.containsKey("gpsLatitude") && requestMap.containsKey("gpsLongitude")){
            Float gpsLatitude = Float.parseFloat(requestMap.get("gpsLatitude"));
            Float gpsLongitude = Float.parseFloat(requestMap.get("gpsLongitude"));
            if(gpsLongitude>= (float)(-180) && gpsLongitude <= (float)(180)
                && gpsLatitude >= (float)(-90) && gpsLatitude <= (float) (90)
            ){

                return true;
            }
        }
        return false;
    }
    public void updateRideStatus(Integer rideId, Float current_lat, Float current_long){
        log.info("Trajectory: update Ride Status");
        Ride ride = rideDao.findById(rideId).orElseThrow();
        Float depart_lat = ride.getDepartureLatitude();
        Float depart_long = ride.getDepartureLongitude();
        Float dest_lat = ride.getDestinationLatitude();
        Float dest_long = ride.getDestinationLongitude();

        Double dist2pickup = RideServiceImpl.haversine_distance(depart_lat, depart_long,
               current_lat, current_long);
        Double dist2arrive = RideServiceImpl.haversine_distance(dest_lat, dest_long, current_lat, current_long);
        log.info("dist2pickup: {}", dist2pickup);
        log.info("dist2arrive: {}", dist2arrive);
        if(dist2pickup < 0.05){
            ride.setRideStatus(RideStatus.PickedUp);
        }
        if(dist2arrive < 0.05){
            ride.setRideStatus(RideStatus.Arrived);
        }

        rideDao.save(ride);
    }

}
