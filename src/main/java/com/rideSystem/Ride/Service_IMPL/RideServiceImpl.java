package com.rideSystem.Ride.Service_IMPL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.DAO.UserDao;
import com.rideSystem.Ride.POJO.Ride;
import com.rideSystem.Ride.POJO.RideStatus;
import com.rideSystem.Ride.POJO.RideType;
import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.Service.RideService;
import com.rideSystem.Ride.mqtt.MqttConfiguration;
import com.rideSystem.Ride.mqtt.MqttPushClient;
import com.rideSystem.Ride.mqtt.MqttSubClient;
import com.rideSystem.Ride.utils.ObjectToHashMapConverter;
import com.rideSystem.Ride.utils.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.rideSystem.Ride.utils.Response.failedResponse;

@NoArgsConstructor
@Slf4j
@Service
public class RideServiceImpl implements RideService {
    @Autowired
    UserDao userDao;
    @Autowired
    RideDao rideDao; // no autowire will cause null in rideDao
    @Autowired
    MqttConfiguration mqttConfiguration;


    public RideServiceImpl(UserDao userDao, RideDao rideDao){
        this.userDao = userDao;
        this.rideDao = rideDao;
    }
    @Override
    public Response driverAcceptRideSession(Integer rideId, Map<String,String> requestMap){

        log.info("Inside createRideSession rideId {}, requestMap {}", rideId, requestMap);
        Response sessionResponse = new Response();
        if(validateCreateSessionMap(requestMap)){
           User driver = userDao.findById(Integer.parseInt(requestMap.get("driverId"))).orElseThrow();

            if(driver != null){
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
                    if(emqxSubscriberFinder(channel)){
                        if(acceptableRide(rideId)) {
                            mqttSubClient.subscribe(channel, 2);
                            rideMessage.put("status", 0);
                            rideMessage.put("msg", "success");
                            rideMessage.put("data", "channel: " + channel);

                            ride.setRideStatus(RideStatus.Received_Order);
                            mqttPushClient.publish(2, false,channel, rideMessage);
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
                    }else{
                        log.info("something went wrong in emqx");
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
    public Response requestRide(Map<String, String> requestMap){
        log.info("inside request ride requestMap: {}" ,requestMap);
        try{
            if(validateRequestRideMap(requestMap)){
                Ride ride = getRideFromMap(requestMap);
                log.info("request Ride after init: ride {}", ride.getRideId());
                User passenger = userDao.findById(Integer.parseInt(requestMap.get("uid"))).orElseThrow();
                // mqtt send
                Map<String,Object> request_ride_response = ObjectToHashMapConverter.convertObjectToMap(ride);
                request_ride_response.put("province", passenger.getState());
                request_ride_response.put("city", passenger.getCity());
                String channel = passenger.getState();
                ride.setMQTTTopic(channel);
                log.info("request Ride: ride {}", ride.getRideId());
                log.info(ride.getMQTTTopic());
                ride.setRideStatus(RideStatus.Created);
                rideDao.save(ride);

                mqttConfiguration.setTopic(channel);
                MqttPushClient mqttPushClient;
                mqttPushClient = mqttConfiguration.getMqttPushClient();
                if(mqttPushClient!=null){
                    MqttSubClient mqttSubClient = new MqttSubClient(mqttPushClient);
                    mqttSubClient.subscribe(channel, 2);
                    mqttPushClient.publish(2, false,channel,request_ride_response);
                }
                // response

                Response response = Response.successResponse();
                response.setData(request_ride_response);
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
        String baseUrl = "http://192.168.12.218:18083"; // Replace with your EMQX host and port
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
        String baseUrl = "http://192.168.12.218:18083"; // Replace with your EMQX host and port
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

}


