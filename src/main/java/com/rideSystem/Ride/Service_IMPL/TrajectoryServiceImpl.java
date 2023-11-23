package com.rideSystem.Ride.Service_IMPL;

import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.DAO.TrajectoryDao;
import com.rideSystem.Ride.DAO.UserDao;
import com.rideSystem.Ride.POJO.Ride;
import com.rideSystem.Ride.POJO.RideStatus;
import com.rideSystem.Ride.POJO.Trajectory;
import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.REST.TrajectoryRest;
import com.rideSystem.Ride.Scheduled.ScheduleService;
import com.rideSystem.Ride.Service.TrajectoryService;
import com.rideSystem.Ride.mqtt.CustomCallBack;
import com.rideSystem.Ride.utils.ObjectToHashMapConverter;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.SameLen;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import com.rideSystem.Ride.utils.Response;
import org.springframework.web.socket.server.HandshakeHandler;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@NoArgsConstructor
@Slf4j
@Service
public class TrajectoryServiceImpl  implements TrajectoryService{
    @Autowired
    RideDao rideDao;
    @Autowired
    UserDao userDao;
    @Autowired
    TrajectoryDao trajectoryDao;
    public TrajectoryServiceImpl(UserDao userDao, RideDao rideDao, TrajectoryDao trajectoryDao){
        this.userDao = userDao;
        this.rideDao = rideDao;
        this.trajectoryDao = trajectoryDao;
    }
    @Override
    public Response createTrajectory(Integer rideId, Map<String,String> requestMap){
        try{
            log.info("inside create Trajectory");
            if(rideDao.existsById(rideId)){
                if(validateTrajectoryCreationMap(requestMap)){
                    log.info("passed validate trajectory creation {}", requestMap);
                    Trajectory trajectory = new Trajectory();
                    trajectory.setCorrespondedRideId(rideId);
                    Float curr_lat = Float.parseFloat(requestMap.get("gpsLatitude"));
                    Float curr_long = Float.parseFloat(requestMap.get("gpsLongitude"));
                    trajectory.setGPSLatitude(curr_lat);
                    trajectory.setGPSLongitude(curr_long);
                    trajectory.setTimeSequence(LocalDateTime.now());
                    trajectoryDao.save(trajectory);
                    log.info("schedule trajectory");
                    requestMap.put("accepted_rideId", rideId.toString());
                    scheduleTrajectory(trajectory, requestMap);

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

    @Override
    public Response acceptedRideDriverView(Integer driverId){
        try{
            log.info("inside accepted ride driver view: {}",driverId);
            User driver = userDao.getUserById(driverId);
            String topic = driver.getState();
            ScheduleService scheduleService = new ScheduleService(userDao, rideDao);
            HashMap<Integer, Set<Integer>> acceptedRideMap = scheduleService.getAcceptedDriverRideMap();
            log.info("acceptedRideMap: {}", acceptedRideMap);

            Response response = Response.successResponse();
            HashMap<String,Object> acceptedRideDriverViewResponse = new HashMap<>();

            Set<Integer> acceptedRideIdSet = null;

            if(acceptedRideMap.containsKey(driverId)) {
                acceptedRideIdSet = acceptedRideMap.get(driverId);

            }
            HashMap<String, Set<Integer>> createdRidesMap = scheduleService.getCreatedRidesMap();
            HashMap<String, Set<Integer>> acceptedRidesMap = scheduleService.getAcceptedRidesMap();
            if (!createdRidesMap.containsKey(topic)) {
                createdRidesMap.put(topic, new HashSet<>());
            }
            if(!acceptedRidesMap.containsKey(topic)){
                acceptedRidesMap.put(topic, new HashSet<>());
            }
            acceptedRideDriverViewResponse.put("created", createdRidesMap);
            acceptedRideDriverViewResponse.put("accepted", acceptedRidesMap);

            acceptedRideDriverViewResponse.put("accepted_rideId", acceptedRideIdSet);

            response.setData(acceptedRideDriverViewResponse);
            return response;

        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("Failed at acceptedRideDriverVIew", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response getTrajectory(Integer rideId, Map<String,String> requestMap){
        try{
            log.info("inside getTrajectory: {}", rideId);
            ScheduleService scheduleService = new ScheduleService(userDao, rideDao);

            HashMap<String, HashMap<Integer, HashMap<String, Set<Float>>>> gpsTrajectory =  scheduleService.getGpsLatLngMap();
            log.info("getTrajectory: get gpsHashMAP {} ", gpsTrajectory);
            String topic = requestMap.get("topic");
            HashMap<String, Object> trajectoryMap = new HashMap<>();
            if(gpsTrajectory.containsKey(topic)){
                if(gpsTrajectory.get(topic).containsKey(rideId)){
                    trajectoryMap = new HashMap<>(gpsTrajectory.get(topic).get(rideId));
                }
            }

            log.info("get trajectory: {}", trajectoryMap);

            Response getTrajectoryResponse = Response.successResponse();
            getTrajectoryResponse.setData(trajectoryMap);
            return getTrajectoryResponse;

        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("Failed: get trajectory", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public void scheduleTrajectory(Trajectory trajectory, Map<String,String> requestMap){

        try{
            Integer userId = Integer.parseInt(requestMap.get("userId"));
            String topic = requestMap.get("topic");

            String driver_mqtt_id = "d" + userId + "_mqtt_trajectory";
            log.info("driver_trajectory_id: {}", driver_mqtt_id);
            ScheduleService scheduleService = new ScheduleService(userDao, rideDao);

            MqttServiceImpl mqttServiceImpl = new MqttServiceImpl(scheduleService);
            MqttClient mqttClient = mqttServiceImpl.mqtt_connection_and_subscription("driver", driver_mqtt_id, topic);

            CustomCallBack customCallBack = new CustomCallBack(mqttClient, scheduleService);


            HashMap<String, HashMap<String, HashMap<String, String>>> driverTrajectoryMap = createDriverTrajectoryMap(trajectory, topic);
            customCallBack.publishMessage(topic, driverTrajectoryMap, 2, false);
            log.info("schedule requestMap: {}", requestMap);
            CompletableFuture<Void> publishCompletion = scheduleService.startScheduledDriverPublishTrajectoryTask(topic, userId, requestMap);


        }catch (Exception e){
            e.printStackTrace();
        }



    }

    public HashMap<String, HashMap<String,HashMap<String,String>>> createDriverTrajectoryMap(Trajectory t, String topic){
        String driverLat = t.getGPSLatitude().toString();
        String driverLng = t.getGPSLongitude().toString();
        String rideId = t.getCorrespondedRideId().toString();
        HashMap<String, HashMap<String, HashMap<String, String>>> driverTrajectoryMap = new HashMap<>();
        HashMap<String, HashMap<String, String>> trajectoryMap = new HashMap<>();
        HashMap<String, String> LatLng = new HashMap<>();
        LatLng.put("driverLat", driverLat);
        LatLng.put("driverLong", driverLng);
        LatLng.put("rideId", rideId);
        trajectoryMap.put("trajectory", LatLng);
        driverTrajectoryMap.put(topic, trajectoryMap);
        return driverTrajectoryMap;
    }
    public boolean validateTrajectoryCreationMap(Map<String,String> requestMap){
        if(requestMap.containsKey("userId") && requestMap.containsKey("topic") &&
                requestMap.containsKey("gpsLatitude") && requestMap.containsKey("gpsLongitude")){
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

    @Override
    public Response deleteAllTrajectories(){
        try{
            trajectoryDao.deleteAllTrajectories();
            return Response.successResponse();
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed at delete all trajectories", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response deleteTrajectoriesByIds(List<Integer> ids){
        try{
            // example url: /api/v1/trajectory/deleteByIds/1,2,3
            trajectoryDao.deleteTrajectoriesByIds(ids);
        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed at delete Trajectoies By ids", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response getPassengerLocation(Integer rideId){
        try{
            Ride ride = rideDao.findById(rideId).orElseThrow();
            Map<String, Object> location = new HashMap<>();
            location.put("pickupLat", ride.getDepartureLatitude());
            location.put("pickupLong", ride.getDepartureLongitude());
            location.put("arriveLat", ride.getDestinationLatitude());
            location.put("arriveLong", ride.getDestinationLongitude());

            Response passengerLocation = Response.successResponse();
            passengerLocation.setData(location);
            return passengerLocation;
        }catch (Exception e){
            e.printStackTrace();
        }
        return  Response.failedResponse("failed at get passenger location", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
