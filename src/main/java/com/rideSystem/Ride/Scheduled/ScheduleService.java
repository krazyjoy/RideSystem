package com.rideSystem.Ride.Scheduled;

import com.google.gson.Gson;
import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.DAO.UserDao;
import com.rideSystem.Ride.POJO.Ride;
import com.rideSystem.Ride.POJO.RideStatus;
import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.Service_IMPL.MqttServiceImpl;
import com.rideSystem.Ride.Service_IMPL.RideServiceImpl;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;

@Service
@Slf4j
public class ScheduleService {
    /*
        shouldStopDriver:
            0: ride init - find available ride
            1: trajectory - publish driver trajectory
            2: end mqtt - finished a round
       shouldStopPassenger:
            true: end mqtt - driver arrived
            false: ride request init - publish and wait for a driver to accept a ride
    */
    private ScheduledExecutorService scheduler;
    private static final HashMap<String, Set<Integer>> CreatedRidesMap = new HashMap<>();
    private static final HashMap<String, HashMap<String, HashMap<String, String>>> payloadMap = new HashMap<>();

    private static final HashMap<Integer, Set<Integer>> acceptedDriverRideMap = new HashMap<>();

    private static final HashMap<String, Set<Integer>> AcceptedRidesMap = new HashMap<>();

    private static final HashMap<String, HashMap<Integer, HashMap<String, Set<Float>>>> gpsLatLngMap = new HashMap<>();
    @Autowired
    private RideDao rideDao;
    @Autowired
    private UserDao userDao;

    @Autowired
    private MqttServiceImpl mqttServiceImpl;

    private volatile String topic;
    private volatile Integer userId;
    private volatile Map<String, String> requestMap;



    private boolean shouldStopDriverPoll;
    private boolean shouldStopDriverPublish;
    private boolean shouldStopPassenger;
    private boolean shouldStopPassengerTrajectory;

    public void setTopic(String topic){
        this.topic = topic;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setRequestMap(Map<String,String> requestMap){
        this.requestMap = requestMap;
    }
    public ScheduleService(UserDao userDao, RideDao rideDao){
        this.userDao = userDao;
        this.rideDao = rideDao;
    }
    public CompletableFuture<Void> startScheduledDriverPollTask(String topic, Integer userId, Map<String, String> requestMap){
        log.info("inside startScheduledDriverPollTask");
        CompletableFuture<Void> taskCompletion = new CompletableFuture<>();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {

            try{
                log.info("try catch schedule driver handle message");
                scheduleDriverHandleMessage(scheduler, topic, userId, requestMap);
                taskCompletion.complete(null);
            }catch (Exception e){
                taskCompletion.completeExceptionally(e);
            }

        }, 0, 120, TimeUnit.SECONDS);


        return taskCompletion;

    }
    public CompletableFuture<Void> startScheduledDriverPublishTrajectoryTask(String topic, Integer userId, Map<String, String> requestMap) {
        log.info("startScheduledDriverPublishTrajectoryTask");
        CompletableFuture<Void> taskCompletion = new CompletableFuture<>();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try{
                log.info("startScheduledDriverPublishTrajectoryTask, requestMap: {}", requestMap);
                if(requestMap.containsKey("accepted_rideId")){
                    Integer ride_id = Integer.parseInt(requestMap.get("accepted_rideId"));
//                    log.info("startScheduledDriverPublishTrajectoryTask ride_id: {}", ride_id);
                    Ride ride = rideDao.findById(ride_id).orElseThrow();
                    User passenger = ride.getPassengerId();
                    Integer passenger_id = passenger.getUid();
//                    log.info("startScheduledDriverPublishTrajectoryTask passenger_id: {}", passenger_id);
                    scheduleDriverPublishMessage(scheduler, topic, passenger_id, requestMap);
                    taskCompletion.complete(null);
                }
                return;
            }catch (Exception e){
                taskCompletion.completeExceptionally(e);
            }

        }, 0, 120, TimeUnit.SECONDS);

        return taskCompletion;
    }
    public CompletableFuture<Void> startScheduledPassengerRequestRideTask(String topic, Integer userId){
        CompletableFuture<Void> taskCompletion = new CompletableFuture<>();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try{
//                log.info("startScheduledPassengerRequestRideTask");
//                HashMap<String, String> inner2HashMap = new HashMap<>();
//                inner2HashMap.put("testInner", "passenger");
//                HashMap<String, HashMap<String, String>> innerHashMap = new HashMap<>();
//                innerHashMap.put("testIn", inner2HashMap);
//                Gson gson = new Gson();
//                addPayload("testOut", gson.toJson(innerHashMap));
                schedulePassengerRequestRide(topic, userId, requestMap);
                taskCompletion.complete(null);

            }catch (Exception e){
                taskCompletion.completeExceptionally(e);
            }
        }, 0, 60, TimeUnit.SECONDS);
        return taskCompletion;

    }

    private void scheduleDriverHandleMessage(ScheduledExecutorService scheduler, String topic, Integer userId, Map<String,String> requestMap) {

        log.info("Running Driver Handle Message Schedule");
        log.info("topic: {}", topic);
        log.info("userId: {}", userId);
        log.info("requestMap: {}", requestMap);
        if (getShouldStopDriverPoll()) {
            shouldStopDriverPoll = false; // reset to not stop
            stopScheduler(scheduler, userId);
            return; // stop the task
        }

        driverHandleMessage(topic, userId, requestMap);
        test();

    }

    public void test(){
        log.info("test");
    }
    public void scheduleDriverPublishMessage(ScheduledExecutorService scheduler, String topic, Integer passenger_id, Map<String,String> requestMap){
        log.info("inside scheduler publish message");
        if(getShouldStopDriverPublish()){
            shouldStopDriverPublish = false;
            stopScheduler(scheduler, userId);
        }
        passengerHandleTrajectoryMessage(topic, passenger_id, requestMap);
    }

    private void schedulePassengerRequestRide(String topic, Integer userId, Map<String,String> requestMap) {

        log.info("Running Passenger Request Ride Schedule");
        log.info("topic: {}", topic);
        log.info("userId: {}", userId);
        log.info("requestMap: {}", requestMap);
        if (getShouldStopPassenger()) {
            shouldStopPassenger = false;
            stopScheduler(scheduler, userId);
            return; // stop the task
        }
        passengerHandleMessage(topic, userId, requestMap);
        test();
    }



    public static HashMap<String, HashMap<String, HashMap<String, String>>> getPayloadMap() {
        return payloadMap;
    }
    public synchronized  void addPayload(String topic, String payload) {
        JSONObject jsonPayload = new JSONObject(payload);
        log.info("jsonPayload keyset: {}", jsonPayload.keySet());

        for (String outerKey : jsonPayload.keySet()) {
            Object outerValue = jsonPayload.get(outerKey);

            if (outerValue instanceof JSONObject) {
                JSONObject innerJson = (JSONObject) outerValue;

                for (String innerKey : innerJson.keySet()) {
                    Object innerValue = innerJson.get(innerKey);

                    if (innerValue instanceof JSONObject) {
                        // Handle 3rd layer
                        JSONObject inner2Json = (JSONObject) innerValue;

                        for (String inner2Key : inner2Json.keySet()) {
                            String value = inner2Json.getString(inner2Key);
                            payloadMap.computeIfAbsent(outerKey, k -> new HashMap<>())
                                    .computeIfAbsent(innerKey, k -> new HashMap<>())
                                    .put(inner2Key, value);
                            log.info("value: {}", value);
                            log.info("payloadMap: {}", payloadMap);
                        }
                    } else {
                        // Handle 2nd layer
                        if(innerValue instanceof String){
                            String value = (String) innerValue;
                            payloadMap.computeIfAbsent(outerKey, k -> new HashMap<>())
                                    .put(innerKey, new HashMap<>());
                            payloadMap.get(outerKey).get(innerKey).put("value", value);
                            log.info("value: {}", value);
                            log.info("payloadMap: {}", payloadMap);
                        }



                    }
                }
            } else {
                // Handle 1st layer
                String value = jsonPayload.getString(outerKey);
                payloadMap.put(outerKey, new HashMap<>());
                log.info("value: {}", value);
                log.info("payloadMap: {}", payloadMap);
            }
        }
    }


    public boolean driverHandleMessage(String topic , Integer userId, Map<String, String> requestMap){
        System.out.println("driverHandleMessage executed at: " + System.currentTimeMillis());
        log.info("payloadMap: {}", payloadMap);
        if (!payloadMap.containsKey(topic)){
            log.info("terminate bc no topic {}", topic);
            shouldStopDriverPoll = true;
            return shouldStopDriverPoll;
        }
        shouldStopDriverPoll = false;
        HashMap<String, HashMap<String, String>> innerMap = payloadMap.get(topic);
        for(String key: innerMap.keySet()){
            Set<Integer> createdRideSet = new HashSet<>();
            Set<Integer> acceptedRideSet = new HashSet<>();
            if(key.equals("driver_connected")){
                log.info("driver's connected payload");

            }
            if(key.equals("ride")){
                log.info("ride message: received by driver");

                User driver = userDao.getUserById(userId);
                log.info("driver: {}", driver);
                createdRideSet = CreatedRidesMap.getOrDefault(topic, new HashSet<Integer>());
                acceptedRideSet = AcceptedRidesMap.getOrDefault(topic, new HashSet<Integer>());
                log.info("CreatedRideSet: {}",createdRideSet);
                log.info("AcceptedRideSet: {}",acceptedRideSet);
                Integer target_rideId = driverFindingNearestRide(driver, createdRideSet, acceptedRideSet, requestMap);
                log.info("return from driverFindingNearestRide: {}", target_rideId);
                Ride nearest_ride = new Ride();
                RideStatus rideStatus = RideStatus.Created;
                try{


                    nearest_ride = rideDao.findByRideIdWithStatus(target_rideId);
                    log.info("nearest ride is {}", nearest_ride.getRideId());
                    rideStatus = nearest_ride.getRideStatus();
                    if(rideStatus.equals(RideStatus.Created) || rideStatus.equals(RideStatus.Received_Order)){
                        log.info("rideStatus has value");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                try{
                    if(!acceptedDriverRideMap.containsKey(userId)){
                        acceptedDriverRideMap.put(userId, new HashSet<>());
                    }
                    while(!rideStatus.equals(RideStatus.Created) || acceptedDriverRideMap.get(userId).contains(target_rideId)){
                        log.info("before Created RidesMap: {}", CreatedRidesMap);
                        log.info("before Accepted RidesMap: {}", AcceptedRidesMap);
                        CreatedRidesMap.get(topic).remove(target_rideId);
                        AcceptedRidesMap.get(topic).add(target_rideId);
                        log.info("remove Created RidesMap: {}", CreatedRidesMap);
                        log.info("add Accepted RidesMap: {}", AcceptedRidesMap);

                        target_rideId = driverFindingNearestRide(driver, createdRideSet, acceptedRideSet, requestMap);
                        nearest_ride = rideDao.findByRideIdWithStatus(target_rideId);
                        log.info("nearest ride is {}", nearest_ride.getRideId());
                        rideStatus = nearest_ride.getRideStatus();
                        if(rideStatus.equals(RideStatus.Created) || rideStatus.equals(RideStatus.Received_Order)){
                            log.info("rideStatus has value");
                        }

                    }
                    log.info("outside while");
                    nearest_ride.setDriverId(driver);
                    nearest_ride.setDriverReceivedOrderTime(LocalDateTime.now());
                    nearest_ride.setRideStatus(RideStatus.Received_Order);
                    rideDao.save(nearest_ride);
                    shouldStopDriverPoll = true;
                    final Integer nearest_ride_id = nearest_ride.getRideId();
                    log.info("final ride_id is {}", nearest_ride_id);
                    if(!CreatedRidesMap.containsKey(topic)){
                        CreatedRidesMap.put(topic, new HashSet<>());
                    }
                    CreatedRidesMap.get(topic).add(nearest_ride_id);

                    if(!AcceptedRidesMap.containsKey(topic)){
                        AcceptedRidesMap.put(topic, new HashSet<>());
                    }
                    AcceptedRidesMap.get(topic).add(nearest_ride_id);
                    if(!acceptedDriverRideMap.containsKey(userId)){
                        acceptedDriverRideMap.put(userId, new HashSet<>());
                    }
                    acceptedDriverRideMap.get(userId).add(nearest_ride_id);
                    log.info("created rides map is {}", CreatedRidesMap);
                    log.info("accepted rides map is {}", AcceptedRidesMap);
                    log.info("accepted Driver Ride Map is: {}", acceptedDriverRideMap);
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }
        log.info("return shouldStopDriverPoll : {}", shouldStopDriverPoll);
        return shouldStopDriverPoll;
    }

    public boolean passengerHandleMessage(String topic, Integer userId, Map<String, String> requestMap){
        System.out.println("passengerHandleMessage executed at: " + System.currentTimeMillis());
        log.info("payloadMap: {}", payloadMap);
        // make sure in processing same channel message (Pennsylvania)
        if (!payloadMap.containsKey(topic)){
            log.info("terminate bc no topic {}", topic);
            boolean shouldStopPassenger = true;
            return shouldStopPassenger;
        }
        boolean shouldStop = false;

        HashMap<String, HashMap<String,String>> innerMap = payloadMap.get(topic);
        for(String key: innerMap.keySet()) {
            Set<Integer> rideSet = new HashSet<>();
            if (key.equals("driver_connected")) {
                log.info("driver's connected payload");
                // rideList = CreatedRidesMap.getOrDefault(topic, new ArrayList<>());
            }
            if (key.equals("ride")) {
                log.info("new request ride");
                log.info("ride message: received by passenger");
                HashMap<String, String> new_ride = innerMap.get("ride");
                Integer new_rideId = Integer.parseInt(new_ride.get("rid"));
                rideSet = CreatedRidesMap.getOrDefault(topic, new HashSet<>()); // same topic rides
                rideSet.add(new_rideId);
                CreatedRidesMap.put(topic, rideSet);
                log.info("Passenger add ride to CreatedRidesMap: {}", CreatedRidesMap);
            }
            if(key.equals("trajectory")){
                log.info("driver's trajectory");
                HashMap<String, String> driver_trajectory = innerMap.get("trajectory");
                if(driver_trajectory.containsKey("driverLat") && driver_trajectory.containsKey("driverLong")){
                    Float driverLat = Float.parseFloat(driver_trajectory.get("driverLat"));
                    Float driverLong = Float.parseFloat(driver_trajectory.get("driverLong"));
                    log.info("driverLat: {}", driverLat);
                    log.info("driverLong: {}", driverLong);
                }
            }
        }
        return shouldStop;
    }

    public boolean passengerHandleTrajectoryMessage(String topic, Integer userId, Map<String,String> requestMap){
        System.out.println("passengerHandleTrajectoryMessage executed at: " + System.currentTimeMillis());
        log.info("payloadMap: {}", payloadMap);
        // make sure in processing same channel message (Pennsylvania)
        if (!payloadMap.containsKey(topic)){
            log.info("terminate bc no topic {}", topic);
            boolean shouldStopPassengerTrajectory = true;
            return shouldStopPassengerTrajectory;
        }
        boolean shouldStop = false;

        HashMap<String, HashMap<String,String>> innerMap = payloadMap.get(topic);
        for(String key: innerMap.keySet()) {
            Set<Integer> rideSet = new HashSet<>();
            if(key.equals("trajectory")){
                log.info("driver's trajectory");

                HashMap<String, String> driver_trajectory = innerMap.get("trajectory");
                log.info("--> ",driver_trajectory);
                if(driver_trajectory.containsKey("driverLat") && driver_trajectory.containsKey("driverLong") && driver_trajectory.containsKey("rideId")){
                    log.info("process driver trajectory");
                    Integer rideId = Integer.parseInt(driver_trajectory.get("rideId"));
                    Float driverLat = Float.parseFloat(driver_trajectory.get("driverLat"));
                    Float driverLong = Float.parseFloat(driver_trajectory.get("driverLong"));
                    log.info("corresponded ride id: {}", rideId);
                    log.info("driverLat: {}", driverLat);
                    log.info("driverLong: {}", driverLong);
                    createTrajectoryMap(topic, rideId, driverLat, driverLong);
                }
            }
        }
        return shouldStop;
    }

    public void createTrajectoryMap(String topic, Integer rideId, Float driverLat, Float driverLng){
        // Ensure the outermost map contains the topic
        gpsLatLngMap.putIfAbsent(topic, new HashMap<>());

        // Get the inner map for the given topic
        HashMap<Integer, HashMap<String, Set<Float>>> gpsInnerMap = gpsLatLngMap.get(topic);

        // Ensure the inner map contains the rideId
        gpsInnerMap.putIfAbsent(rideId, new HashMap<>());

        // Get the map for the given rideId
        HashMap<String, Set<Float>> rideData = gpsInnerMap.get(rideId);
        // Ensure the rideData map contains "driverLat" and "driverLng" keys
        rideData.putIfAbsent("driverLat", new HashSet<>());
        rideData.putIfAbsent("driverLng", new HashSet<>());

        // Add latitude and longitude to their respective lists
        rideData.get("driverLat").add(driverLat);
        rideData.get("driverLng").add(driverLng);
        log.info("gpsMap updated: {}", gpsLatLngMap);
    }
    public boolean getShouldStopDriverPoll(){
        return shouldStopDriverPoll;
    }

    public boolean getShouldStopDriverPublish(){
        return shouldStopDriverPublish;
    }
    public boolean getShouldStopPassenger(){
        return shouldStopPassenger;
    }
    public boolean getShouldStopPassengerTrajectory(){
        return shouldStopPassengerTrajectory;
    }

    public HashMap<Integer, Set<Integer>> getAcceptedDriverRideMap(){
        return acceptedDriverRideMap;
    }

    public HashMap<String, Set<Integer>> getCreatedRidesMap(){
        return CreatedRidesMap;
    }
    public HashMap<String, Set<Integer>> getAcceptedRidesMap(){
        return AcceptedRidesMap;
    }
    public HashMap<String, HashMap<Integer, HashMap<String, Set<Float>>>> getGpsLatLngMap(){ return gpsLatLngMap;}
    public void stopScheduler(ScheduledExecutorService scheduler, Integer userId){
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            System.out.println(userId + " stopping scheduler...");
            List<Runnable> pendingTasks = scheduler.shutdownNow();
            System.out.println(userId + " Tasks pending: " + pendingTasks.size());
        }, 20, TimeUnit.SECONDS);
    }

    public Integer driverFindingNearestRide(User driver, Set<Integer> createdRideSet, Set<Integer> acceptedRideSet, Map<String, String> requestMap){
        log.info("inside driver finding nearest ride");
        Double shortestDistance = Double.MAX_VALUE;
        Integer latestRideId = -1;
        Integer earliestSessionRideId = Integer.MAX_VALUE;
        Double latestDistance = Double.MIN_VALUE;
        HashSet<Integer> differenceSet = new HashSet<>(createdRideSet);
        differenceSet.removeAll(acceptedRideSet);
        log.info("difference set: {}", differenceSet);
        Iterator<Integer> iterator = differenceSet.iterator();
        while(iterator.hasNext()){
            Integer rideId = iterator.next();
            earliestSessionRideId = Math.min(rideId, earliestSessionRideId);
            latestRideId = Math.max(rideId, latestRideId);
        }
        log.info("earliestSessionRideId: {}", earliestSessionRideId);
        log.info("latestRideId: {}", latestRideId);

        Integer targetRide = -1;
        for (Integer pending_ride=earliestSessionRideId; pending_ride<=latestRideId; pending_ride++){
            log.info("pending ride: {}", pending_ride);
            Ride ride = rideDao.findById(pending_ride).orElseThrow();

            Float pickUpLat = ride.getDepartureLatitude();
            Float pickUpLong = ride.getDepartureLongitude();
            Float destLat = ride.getDestinationLatitude();
            Float destLong = ride.getDestinationLongitude();
            Double hd = driverGetHarversineDistance(driver, requestMap, pickUpLat, pickUpLong);
            if(shortestDistance > hd){
                shortestDistance = hd;
                targetRide = pending_ride;
            }
//            if(pending_ride.equals(latestRideId)){
//                latestDistance = hd;
//            }

        }

//        if(shortestDistance.equals(latestDistance)){
//            log.info("prioritize latest");
//            targetRide = latestRideId;
//        }
        log.info("shortest distance: {}", shortestDistance);
        log.info("choose to accept: {}", targetRide);
        return targetRide;


    }
    public double driverGetHarversineDistance(User driver, Map<String, String> requestMap, Float pickUpLat, Float pickUpLong){
        log.info("inside driver get harversine distance");
        if(requestMap.containsKey("driverLatitude") && requestMap.containsKey("driverLongitude")){

            Float driver_latitude = Float.parseFloat(requestMap.get("driverLatitude"));
            Float driver_longitude = Float.parseFloat(requestMap.get("driverLongitude"));
            log.info("driver latitude: {}", driver_latitude);
            log.info("driver longitude: {}", driver_longitude);
            Double hd = RideServiceImpl.haversine_distance(driver_latitude, driver_longitude,pickUpLat, pickUpLong);
            log.info("hd is: {}", hd);
            return hd;
        }
        return Double.MAX_VALUE;
    }

}
