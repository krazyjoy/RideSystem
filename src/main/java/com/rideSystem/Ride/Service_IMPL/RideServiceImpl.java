package com.rideSystem.Ride.Service_IMPL;
import com.rideSystem.Ride.DAO.TrajectoryDao;
import com.rideSystem.Ride.POJO.*;
import com.rideSystem.Ride.Scheduled.ScheduleService;
import com.rideSystem.Ride.mqtt.*;
import org.eclipse.paho.client.mqttv3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.DAO.UserDao;
import org.json.JSONObject;
import com.rideSystem.Ride.Service.RideService;

import com.rideSystem.Ride.utils.ObjectToHashMapConverter;
import com.rideSystem.Ride.utils.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.lang.Thread;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.rideSystem.Ride.utils.Response.failedResponse;
import static com.rideSystem.Ride.utils.Response.successResponse;

@NoArgsConstructor
@Slf4j
@Service

public class RideServiceImpl implements RideService {
    @Autowired
    UserDao userDao;
    @Autowired
    RideDao rideDao; // no autowire will cause null in rideDao
    @Autowired
    TrajectoryDao trajectoryDao;
    @Autowired
    MqttConfiguration mqttConfiguration;



    List<String> topics = new ArrayList<>();
    SimpMessagingTemplate messagingTemplate;
    Queue<Ride> awaiting_request = new LinkedList<>();
    private Map<Integer, RideStatus> lastStatusMap = new HashMap<>();
    private Set<Integer> NewAcceptedRideIds = new HashSet<>();

//    private MqttServiceImpl mqttServiceImpl = new MqttServiceImpl();


    @Autowired
    public RideServiceImpl(UserDao userDao, RideDao rideDao, TrajectoryDao trajectoryDao){
        this.userDao = userDao;
        this.rideDao = rideDao;
        this.trajectoryDao = trajectoryDao;

    }

    @Override
    public Response driverOnMqtt(Integer driverId, Map<String, String> requestMap){
        String topic = "";
        User driver = userDao.getUserById(driverId);
        if (driver == null){
            return Response.failedResponse("failed at rideServiceImpl: driver on mqtt ", HttpStatus.BAD_REQUEST);
        }
        if (!requestMap.containsKey("topic") || !requestMap.containsKey("driverLatitude") || !requestMap.containsKey("driverLongitude")) {
            return Response.failedResponse("failed at rideServiceImpl: driver on mqtt ", HttpStatus.BAD_REQUEST);
        }
        topic = requestMap.get("topic");

        try{

            String driver_mqtt_id = "d" + driverId + "_mqtt_test";
            log.info("driver_mqtt_id: {}", driver_mqtt_id);
            ScheduleService scheduleService = new ScheduleService(userDao, rideDao);

            MqttServiceImpl mqttServiceImpl  = new MqttServiceImpl(scheduleService);
            MqttClient mqttClient = mqttServiceImpl.mqtt_connection_and_subscription("driver", driver_mqtt_id, topic);

            CustomCallBack customCallBack = new CustomCallBack(mqttClient, scheduleService);

            HashMap<String, HashMap<String, HashMap<String, String>>> driverLocationMap = new HashMap<>();
            HashMap<String, HashMap<String, String>> driverLocationInnerMap = new HashMap<>();
            driverLocationInnerMap.put(driver_mqtt_id, new HashMap<>(requestMap));
            driverLocationMap.put(topic, new HashMap<>(driverLocationInnerMap));
            customCallBack.publishMessage(topic, driverLocationMap, 2, false);


            try{
                Thread.sleep(3000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            /* autowire schedule service */


            // 1. driver polling rides
            CompletableFuture<Void> taskCompletion = scheduleService.startScheduledDriverPollTask(topic, driverId, requestMap);
            final String finalTopic = topic;

            try{
                Thread.sleep(3000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            log.info("executed part1");
            // 2. driver found ride, accepted, now publish own trajectory

            try{
                HashMap<Integer, Set<Integer>> acceptedDriverRideMap = scheduleService.getAcceptedDriverRideMap();
                log.info("acceptedDriveRideMap: {}", acceptedDriverRideMap);
                Set<Integer> acceptedRideIdSet = null;

                if(acceptedDriverRideMap.containsKey(driverId)) {
                    acceptedRideIdSet = acceptedDriverRideMap.get(driverId);

                }
                Response driverOnMqttResponse = Response.successResponse();
                HashMap<String,Object> driverOnMqttData = new HashMap<>();
                driverOnMqttData.put("accepted", acceptedRideIdSet);
                driverOnMqttResponse.setData(driverOnMqttData);
                return driverOnMqttResponse;

            }catch (Exception e){
                e.printStackTrace();
            }




        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed at driverOnMqtt: ", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public Response driverAcceptRideSession(Integer rideId, Map<String,String> requestMap){

        log.info("Inside Driver Accept RideSession rideId {}, requestMap {}", rideId, requestMap);

        Pageable pageable = PageRequest.of(0, 10); // Limit to 1 result
        Page<Ride> latestRidePage = rideDao.findLatestRide(pageable);
        List<Ride> latestRide = latestRidePage.getContent();


        Response sessionResponse = new Response();

        if(validateCreateSessionMap(requestMap)){
            User driver = userDao.findById(Integer.parseInt(requestMap.get("driverId"))).orElseThrow();

            if(driver != null){

                Ride nearest_ride = getNearestRide(Float.parseFloat(requestMap.get("longitude")),
                        Float.parseFloat(requestMap.get("latitude")), rideId);


                int nearest_ride_id = nearest_ride.getRideId();

                boolean accepted = false;
                if(acceptableRide(nearest_ride_id)) {
                    if(nearest_ride_id == rideId){
                        nearest_ride.setDriverId(driver);
                        nearest_ride.setDriverReceivedOrderTime(LocalDateTime.now());
                        nearest_ride.setRideStatus(RideStatus.Received_Order);
                        rideDao.save(nearest_ride);
                        accepted = true;
                        NewAcceptedRideIds.add(rideId);
                    }else{
                        log.info("the nearest last 3 ride is {}, but requested ride is {}", nearest_ride_id, rideId);
                    }
                }else{
                    Ride requested_ride = rideDao.findById(rideId).orElseThrow();
                    log.info("ride: {} status is {}", rideId, requested_ride.getRideStatus());
                }

                // create MQTT channel with rideId, return channelId

                System.out.println(mqttConfiguration.getClientid());
                System.out.println(mqttConfiguration.getHost());

                // find channel with rideId
                Ride ride = rideDao.findById(rideId).orElseThrow();

                String channel = ride.getMQTTTopic();
                mqttConfiguration.setTopic(channel);
                MqttPushClient mqttPushClient;
                mqttPushClient = mqttConfiguration.getMqttPushClient();
                if(mqttPushClient!=null){
                    MqttSubClient mqttSubClient = new MqttSubClient(mqttPushClient);
                    Map<String,Object> rideMessage = new HashMap<>();

                        if(accepted) {
                            mqttSubClient.subscribe(channel, 2);
                            rideMessage.put("status", 0);
                            rideMessage.put("msg", "success");
                            rideMessage.put("data", "channel: " + channel);

                            Map<String,Object> accept_ride_response = new HashMap<>();
                            accept_ride_response.put("accepted", true);
                            log.info("accept_ride_response: {}", accept_ride_response);
                            mqttPushClient.publish(2, false,channel, accept_ride_response);

                            sessionResponse.setStatus(0);
                            sessionResponse.setMsg("success");
                            HashMap<String,Object> acceptSession = new HashMap<>();
                            acceptSession.put("channel", channel);
                            sessionResponse.setData(acceptSession);
                        }else{
                            rideMessage.put("status", 1);
                            rideMessage.put("msg", "already subscribed");

                            sessionResponse.setStatus(1);
                            sessionResponse.setMsg("Ride has been accepted by others");
                        }
                }
            }else{
                sessionResponse.setStatus(1);
                sessionResponse.setMsg("fail: driver is null");
            }

        }else{
            sessionResponse.setStatus(1);
            sessionResponse.setMsg("fail: validate request map");
        }
        return sessionResponse;

    }
    public Ride getNearestRide(float latitude, float longitude, int requested_ride_id){


        List<Integer> created_session_ids = new ArrayList<>();
        log.info("lastStatusMap: {}", lastStatusMap);
        // find all created sessions
        for(int session_id: lastStatusMap.keySet()){
            created_session_ids.add(session_id);
        }
        List<Integer> sorted_created_session_ids = new ArrayList<>();
        int n = created_session_ids.size();
        Integer[] tmp_sessions = created_session_ids.toArray(new Integer[0]);
        Arrays.sort(tmp_sessions,  Collections.reverseOrder());
        for(int session_id: tmp_sessions){
            sorted_created_session_ids.add(session_id);

        }
        log.info("created session ids: {}", created_session_ids);
        log.info("sorted session ids: {}", sorted_created_session_ids);

        Float driver_latitude = latitude;
        Float driver_longitude = longitude;

        double min_hd = Double.MAX_VALUE;
        double[] hds = new double[3];

        int min_hds_index = 0;
        int min_hd_session_id=0;
        int requested_index = sorted_created_session_ids.get(0);
        Map<Integer, Double> dis_map = new HashMap<>();
        if(sorted_created_session_ids.size() < 3){
            n = sorted_created_session_ids.size();
        }else{
            n = 3;
        }

        for(int j=0; j<n; j++) {
            int session_id = sorted_created_session_ids.get(j);
            dis_map.put(session_id, Double.MAX_VALUE);
            Ride ride = rideDao.getById(session_id);
            Float passenger_departure_latitude = ride.getDepartureLatitude();
            Float passenger_departure_longitude = ride.getDepartureLongitude();

            double hd = haversine_distance(driver_latitude, driver_longitude, passenger_departure_latitude, passenger_departure_longitude);
            log.info("hd: {}", hd);
            dis_map.put(session_id, hd);
            if(hd < min_hd){
                min_hd_session_id = session_id;
                min_hd = Math.min(hd, min_hd);

            }
        }
        log.info("dis map: {}", dis_map);
        if(dis_map.get(requested_ride_id) == min_hd){
            log.info("prioritize current");
            min_hd_session_id = requested_ride_id;
        }
        log.info("min_hd_session_id: {}", min_hd_session_id);

        Ride ride = rideDao.findById(min_hd_session_id).orElseThrow();
        return ride;
    }

    @Override
    public Response getRideSession(Integer rideId, Map<String, String> requestMap) {
        log.info("inside getRideSession: rideId is {}, requestMap {}",rideId, requestMap);
        try{
            if(validateGetSessionMap(requestMap)){

                Response sessionResponse = Response.successResponse();

                Ride ride = rideDao.findById(rideId).orElseThrow();
                sessionResponse.setData(rideData(ride));
                return sessionResponse;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        Response sessionResponse = failedResponse("getRideSession",HttpStatus.INTERNAL_SERVER_ERROR);
        return sessionResponse;
    }
    @Override
    public Response updateRideStatus(Integer rideId, Map<String,String> requestMap){
        try{
            Ride ride = rideDao.findById(rideId).orElseThrow();
            if(requestMap.containsKey("ride_status")){
                String ride_status = requestMap.get("ride_status");
                if(ride_status.equalsIgnoreCase("Received_Order")){
                    ride.setRideStatus(RideStatus.Received_Order);
                }
                else if(ride_status.equalsIgnoreCase("Departed")){
                    ride.setRideStatus(RideStatus.Departed);
                }
                else if(ride_status.equalsIgnoreCase("Pickedup")){
                    ride.setRideStatus(RideStatus.PickedUp);
                    ride.setPickedUpTime(LocalDateTime.now());
                }
                else if(ride_status.equalsIgnoreCase("Arrived")){
                    ride.setRideStatus(RideStatus.Arrived);
                    ride.setArrivalTime(LocalDateTime.now());

                }
                else if(ride_status.equalsIgnoreCase("Terminated")){
                    ride.setRideStatus(RideStatus.Terminated);
                    //ride.setCancelTime(LocalDateTime.now());
                }
                else{
                    log.info("non existed ride status in update ride status");
                }
                rideDao.save(ride);
                Map<String,Object> updated_ride_response = ObjectToHashMapConverter.convertObjectToMap(ride);
                Response ride_response = Response.successResponse();
                ride_response.setData(updated_ride_response);
            }
            else{
                log.info("invalid request map in update ride status");
                return failedResponse("failed update ride status", HttpStatus.BAD_REQUEST);

            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed: update Ride Status", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static double haversine_distance(Float driver_lat, Float driver_long, Float passenger_lat, Float passenger_long){
        double R = 3958.8;
        double rlat1 = driver_lat * (Math.PI/180);
        double rlat2 = passenger_lat * (Math.PI/180);
        double difflat = rlat2 - rlat1;
        double difflong = (passenger_long - driver_long)*(Math.PI/180);
        double d =2 * R * Math.asin(Math.sqrt(Math.sin(difflat/2)*Math.sin(difflat/2)+Math.cos(rlat1)*Math.cos(rlat2)*Math.sin(difflong/2)*Math.sin(difflong/2)));
        return d;
    }
    @Override
    public Response requestRide(Map<String, String> requestMap){
        log.info("inside request ride requestMap: {}" ,requestMap);
        try{
            if(validateRequestRideMap(requestMap)){
                Ride ride = getRideFromMap(requestMap);

                Integer passenger_id = Integer.parseInt(requestMap.get("uid"));
                User passenger = userDao.findById(passenger_id).orElseThrow();
                // mqtt send
                ride.setRideStatus(RideStatus.Created);
                // save order created time
                ride.setOrderCreatedTime(LocalDateTime.now());
                // sort by latest ride
                log.info("ride order created time: {}", ride.getOrderCreatedTime());

                // set mqtt topic
                String topic = passenger.getState();
                ride.setMQTTTopic(topic);

                rideDao.save(ride);
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Pageable pageable = PageRequest.of(0, 1); // Limit to 1 result
                Page<Ride> latestRidePage = rideDao.findLatestRide(pageable);
                Ride latestRide = latestRidePage.getContent().get(0);
                log.info("latest rides: {}", latestRide);

                try{

                    String passenger_mqtt_id = "p" + passenger_id + "_mqtt_test";
                    log.info("passenger_mqtt_id: {}", passenger_mqtt_id);
                    ScheduleService scheduleService = new ScheduleService(userDao, rideDao);

                    MqttServiceImpl mqttServiceImpl  = new MqttServiceImpl(scheduleService);
                    MqttClient mqttClient = mqttServiceImpl.mqtt_connection_and_subscription("passenger", passenger_mqtt_id, topic);


                    String rideId = latestRide.getRideId().toString();

                    HashMap<String,String> rideInfo = new HashMap<>();
                    rideInfo.put("rid", rideId);
                    HashMap<String,HashMap<String, String>> passengerRequestInnerMap = new HashMap<>();
                    HashMap<String, HashMap<String, HashMap<String,String>>> passengerRequestMap = new HashMap<>();
                    passengerRequestInnerMap.put("ride", new HashMap<>(rideInfo));
                    passengerRequestMap.put(topic, new HashMap<>(passengerRequestInnerMap));
                    CustomCallBack customCallBack = new CustomCallBack(mqttClient, scheduleService);
                    customCallBack.publishMessage(topic, passengerRequestMap, 2, false);

                    try{
                        Thread.sleep(3000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    /* autowire schedule service */

                    CompletableFuture<Void> taskCompletion = scheduleService.startScheduledPassengerRequestRideTask(topic, passenger_id);
                    try{
                        Thread.sleep(3000);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // response


                }catch (MqttException e){
//                    if (e.getReasonCode() == MqttException.REASON_CODE_CLIENT_DISCONNECTING) {
//                        // Handle disconnection without exiting the try-catch block
//                        log.info("MQTT client is disconnecting. Handling the disconnection...");
//                    } else {
//                        // Handle other MQTT exceptions
//                        e.printStackTrace();
//                    }
                    e.printStackTrace();
                }
                latestRidePage = rideDao.findLatestRide(pageable);
                latestRide = latestRidePage.getContent().get(0);
                Map<String,Object> request_ride_response = new HashMap<>();
                request_ride_response.put("userId", passenger_id);
                request_ride_response.put("rideId", latestRide.getRideId());
                request_ride_response.put("topic", topic);
                request_ride_response.put("pickupLat",latestRide.getDepartureLatitude());
                request_ride_response.put("pickupLong", latestRide.getDepartureLongitude());
                request_ride_response.put("destLat", latestRide.getDestinationLatitude());
                request_ride_response.put("destLong", latestRide.getDestinationLongitude());
                Response response = Response.successResponse();
                response.setData(request_ride_response);
                log.info("response: {}", response);
                return response;

            }else{
                log.info("validate: failed in cancel ride");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("requestRide", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public Response cancelRide(Integer rideId, Map<String,String>requestMap){
        log.info("cancel ride: rideId is {}; requestMap {}", rideId, requestMap);
        try{
            if(validateCancelRideMap(rideId, requestMap)){
                User user = userDao.findById(Integer.parseInt(requestMap.get("uid"))).orElseThrow();
                Ride ride = rideDao.findById(rideId).orElseThrow();

                String mqtt_topic = ride.getMQTTTopic();

                log.info("Cancel RIde: mqtt topic is {}", mqtt_topic);

                MqttPushClient mqttPushClient;
                mqttPushClient = mqttConfiguration.getMqttPushClient();
                MqttSubClient mqttSubClient = new MqttSubClient(mqttPushClient);
                mqttSubClient.unsubscribe(mqtt_topic);
                log.info("unsubscribed emqx topic: {} ", mqtt_topic);

                rideDao.deleteById(rideId);

                Response response = Response.successResponse();
                Map<String,Object> cancel_ride_response = ObjectToHashMapConverter.convertObjectToMap(ride);
                response.setData(cancel_ride_response);

                return response;

            }else{
                Response response = Response.successResponse();
                response.setMsg("ride has been canceled");
                return response;
            }


        }catch(Exception e){

        }
        return Response.failedResponse("cancelRide", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Response requestOrder(Integer orderId, Integer rideId, Map<String,String> requestMap){
        try {

        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("requestOrder", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response subscriptions(String topic){
        String baseUrl = "http://10.157.20.99:18083"; // Replace with your EMQX host and port
        String topicFilter = topic; // Replace with the topic you're interested in
        String username = "admin";
        String password = "public";
        log.info("topic: {}", topic);
        // Encode username and password
        String authString = username + ":" + password;
        String authHeaderValue = "Basic " + java.util.Base64.getEncoder().encodeToString(authString.getBytes());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl + "/api/v4/subscriptions");
            httpGet.setHeader("Authorization", authHeaderValue);

            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println(responseBody);
                // Parse responseBody to get subscription information
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

// Assuming the response body is an object with a "subscriptions" array
                JsonNode subscriptions = jsonNode.get("data");
                Response subscription_response = Response.successResponse();
                Map<String, Object> subscription_data = new HashMap<>();
                subscription_data.put("client_id", new ArrayList<String>());
                for (JsonNode subscription : subscriptions) {
                    log.info("looping through subscriptions {}", subscription);
                    String subscribedTopic = subscription.get("topic").asText();
                    log.info("subscribed topic: {}", subscribedTopic);
                    if(subscribedTopic.equals(topic)){
                        log.info("subscribe topic == topic");
                        String clientId = subscription.get("clientid").asText();
                        ((ArrayList<String>) subscription_data.get("client_id")).add(clientId);
                        subscription_response.setData(subscription_data);

                    }
                }
                return subscription_response;
            } else {
                System.err.println("HTTP request failed: " + response.getStatusLine());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed to reach Subscription data", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private boolean acceptableRide(Integer rideId){
        Ride ride = rideDao.findById(rideId).orElseThrow();
        if (ride.getRideStatus().equals(RideStatus.Created)){
            return true;
        }
        else{
            return false;
        }
    }


    private boolean emqxSubscriberFinder(String topic){
        String baseUrl = "http://10.157.20.99:18083"; // Replace with your EMQX host and port
        String topicFilter = topic; // Replace with the topic you're interested in
        String username = "admin";
        String password = "public";

        // Encode username and password
        String authString = username + ":" + password;
        String authHeaderValue = "Basic " + java.util.Base64.getEncoder().encodeToString(authString.getBytes());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl + "/api/v4/subscriptions");
            httpGet.setHeader("Authorization", authHeaderValue);

            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println(responseBody);
                // Parse responseBody to get subscription information
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

// Assuming the response body is an object with a "subscriptions" array
                JsonNode subscriptions = jsonNode.get("data");

                for (JsonNode subscription : subscriptions) {
                    String subscribedTopic = subscription.get("topic").asText();
                    String clientId = subscription.get("clientid").asText();

                    if (subscribedTopic.equals(topic)) {


                        System.out.println("Client ID " + clientId + " is subscribed to topic " + topic);
                        return true;
                    }
                    return false;
                }
            } else {
                System.err.println("HTTP request failed: " + response.getStatusLine());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public Response listenToRideStatus(Integer rideId){
        try{
            Ride ride = rideDao.findById(rideId).orElseThrow();
            RideStatus rideStatus = ride.getRideStatus();
            String rideStatusInString = rideStatus.toString();
            Response response = Response.successResponse();
            HashMap<String, Object> rideStatusResponse = new HashMap<>();
            rideStatusResponse.put("ride_status", rideStatusInString);
            response.setData(rideStatusResponse);
            return response;

        }catch(Exception e){
            e.printStackTrace();
        }
        return Response.failedResponse("failed: listen to Ride Status", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public Response getRideStatus(Integer rideId){
        try{
            Ride ride = rideDao.findById(rideId).orElseThrow();
            String ride_status = ride.getRideStatus().toString();
            Response success_response = Response.successResponse();
            HashMap<String, Object> ride_status_map = new HashMap<>();
            ride_status_map.put("ride_status", ride_status);
            success_response.setData(ride_status_map);
            return success_response;

        }catch (Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed: get ride status", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /* polling ride status change */
    /*
    @Scheduled(fixedDelay=80000) // Poll every 80 seconds
    public void pollRideStatus(){
        log.info("inside poll ride status");
        boolean accepted = false;
        if(lastStatusMap.isEmpty()){
            log.info("lastStatusMap: {}",lastStatusMap);
            // List<Ride> createdRides = rideDao.findByRideStatus(RideStatus.Created);
            List<Ride> createdRides = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 1); // Limit to 1 result
            Page<Ride> latestRidePage = rideDao.findLatestRide(pageable);
            Ride latestRide = latestRidePage.getContent().get(0);
            log.info("create rides: {}", createdRides);
            int rideId = latestRide.getRideId();
            createdRides.add(latestRide);
            lastStatusMap.put(rideId, latestRide.getRideStatus());
        }else{
            Pageable pageable = PageRequest.of(0,3);
            Page<Ride> latestRidePage = rideDao.findLatestRide(pageable);
            List<Ride> latestRides = latestRidePage.getContent();
            for(int i=0; i<latestRides.size(); i++){
                Ride ride = latestRides.get(i);

                lastStatusMap.put(ride.getRideId(), ride.getRideStatus());
            }
            log.info("NOT NULL lastStatusMap: {}", lastStatusMap);
            if(NewAcceptedRideIds.size() > 5){
                int[] sorted_ids = new int[5];
                int i=0;
                for(int id: NewAcceptedRideIds){
                    sorted_ids[i] = id;
                    i+=1;
                }
                Arrays.sort(sorted_ids);
                while(NewAcceptedRideIds.size() > 5){
                    NewAcceptedRideIds.remove(sorted_ids);
                }
            }
        }
    }
    */

    private HashMap<String,Object> rideData(Ride ride){
        HashMap<String,Object> data = new HashMap<>();

        data.put("rideId", ride.getRideId());
        data.put("createTime", ride.getOrderCreatedTime());

        HashMap<String,Object> pick_up_location = new HashMap<>();
        pick_up_location.put("longitude",ride.getDepartureLongitude());
        pick_up_location.put("latitude",ride.getDepartureLatitude());
        data.put("pickUpLocation",pick_up_location);

        HashMap<String,Object> dest_location = new HashMap<>();
        dest_location.put("longitude",ride.getDestinationLongitude());
        dest_location.put("latitude",ride.getDestinationLatitude());
        data.put("destinationLocation",dest_location);

        data.put("pickUp_Address", ride.getDepartureAddress());
        data.put("dest_Address",ride.getDestinationAddress());
        // TODO: Estimated Mileage
        data.put("ride_status", ride.getRideStatus());
        data.put("status", 0);
        data.put("channel","");
        HashMap<String, Object> nearby_vehicles = new HashMap<>();
        nearby_vehicles.put("longitude", new ArrayList<>());
        nearby_vehicles.put("latitude", new ArrayList<>());

        data.put("nearbyVehicles",nearby_vehicles);
        return data;
    }



    private boolean validateCreateSessionMap(Map<String, String> requestMap){
        // requirement: login - obtain token
        if(requestMap.containsKey("driverId")
                &&requestMap.containsKey("latitude")
                &&requestMap.containsKey("longitude")
                &&requestMap.containsKey("licensePlateNumber")){
                log.info("validate: create session map = success");
                return true;
        }
        log.info("validate: create session map = fail");
        return false;
    }
    private boolean validateGetSessionMap(Map<String,String> requestMap){
        if(requestMap.containsKey("longitude") && requestMap.containsKey("latitude")){
            log.info("validate: get session map = success");
            return true;
        }
        log.info("validate: get session map = fail");
        return false;
    }
    private boolean validateRequestRideMap(Map<String,String> requestMap){
        // requirement: login - obtain token
        if(requestMap.containsKey("uid") && requestMap.containsKey("pickUpLong")
        && requestMap.containsKey("pickUpLat") && requestMap.containsKey("pickUpResolvedAddress")
        && requestMap.containsKey("destResolvedAddress") && requestMap.containsKey("rideType")
                && requestMap.containsKey("destLat") && requestMap.containsKey("destLong")
        && requestMap.containsKey("province") && requestMap.containsKey("city")){
            log.info("validate: request ride map = success");
            return true;
        }

        log.info("validate: request ride map = fail");
        return false;
    }

    private boolean validateCancelRideMap(Integer rid, Map<String,String> requestMap){
        if(rideDao.findById(rid) == null) {
            log.info("non existed rid");
            return false;
        }
        if(requestMap.containsKey("uid") && requestMap.containsKey("cancel")){
            if(requestMap.get("cancel").equals("true")){
                return true;
            }else{
                log.info("validate: cancel ride map, false");
                return false;
            }
        }
        return false;
    }

    private Ride getRideFromMap(Map<String, String> requestMap){
        Ride ride = new Ride();
        if(ride == null)
            log.info("ride is null");
        log.info("ride is {}", ride.getRideId());
        if(requestMap.containsKey("uid")){
            User passenger = userDao.findById(Integer.parseInt(requestMap.get("uid"))).orElseThrow();
            ride.setPassengerId(passenger);
        }
        if(requestMap.containsKey("pickUpLong")){
            ride.setDepartureLongitude(Float.parseFloat(requestMap.get("pickUpLong")));
        }
        if(requestMap.containsKey("pickUpLat")){
            ride.setDepartureLatitude(Float.parseFloat(requestMap.get("pickUpLat")));
        }
        if(requestMap.containsKey("pickUpResolvedAddress")){
            ride.setDepartureAddress(requestMap.get("pickUpResolvedAddress"));
        }
        if(requestMap.containsKey("destLat")){
            ride.setDestinationLatitude(Float.parseFloat(requestMap.get("destLat")));
        }
        if(requestMap.containsKey("destLong")){
            ride.setDestinationLongitude(Float.parseFloat(requestMap.get("destLong")));
        }
        if(requestMap.containsKey("destResolvedAddress")){
            ride.setDestinationAddress(requestMap.get("destResolvedAddress"));
        }
        if(requestMap.containsKey("rideType")){
            String rideTypeString = (String) requestMap.get("rideType");
            try{
                ride.setRideType(RideType.valueOf(rideTypeString.toUpperCase()));
            }catch(IllegalArgumentException e){
                System.out.println("Invalid ride type: "+ rideTypeString);
            }

        }
        return ride;

    }

    public Response deleteAllRides(){
        try{
            rideDao.deleteAllRides();
            return Response.successResponse();
        }catch(Exception e){
            e.printStackTrace();
        }

        return Response.failedResponse("failed: delete all rides", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


