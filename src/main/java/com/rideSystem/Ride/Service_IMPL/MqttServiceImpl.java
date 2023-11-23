package com.rideSystem.Ride.Service_IMPL;

import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.DAO.UserDao;
import com.rideSystem.Ride.POJO.Ride;
import com.rideSystem.Ride.POJO.RideStatus;
import com.rideSystem.Ride.POJO.User;
import com.rideSystem.Ride.Scheduled.ScheduleService;
import com.rideSystem.Ride.mqtt.CustomCallBack;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.rideSystem.Ride.Service_IMPL.RideServiceImpl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class MqttServiceImpl{
    private HashMap<String, List<Integer>> CreatedRidesMap = new HashMap<String, List<Integer>>();
    private HashMap<String, List<String>> payloadMap = new HashMap<>();
    @Autowired
    private RideDao rideDao;
    @Autowired
    private UserDao userDao;

    private String host = "tcp://192.168.12.218:1883";
    private String username = "admin";
    private String password = "public";
    private int timeout = 1000;
    private int keepalive = 10;

    private ScheduleService scheduleService;

    public MqttServiceImpl(ScheduleService scheduleService){
        this.scheduleService = scheduleService;
    }



    public HashMap<String, List<Integer>> getCreatedRidesMap(){

        return getCreatedRidesMap();
    }

    public HashMap<String, List<String>> getPayloadMap(){
        return getPayloadMap();
    }

    public MqttClient mqtt_connection_and_subscription(String identity, String mqtt_id, String topic){
        try{
            MqttClient mqttClient = new MqttClient(this.host, mqtt_id,  new MemoryPersistence());


            CustomCallBack customCallBack = new CustomCallBack(mqttClient, scheduleService);
            // Set the custom callback
            mqttClient.setCallback(customCallBack);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            // options.setAutomaticReconnect(true); /* add automatic reconnection */
            mqttClient.connect(options);
            log.info("connected: ");

            // Subscribe to a topic
            mqttClient.subscribe(topic);
            log.info("subscribed: ");

            // publish
            Map<String,HashMap<String,HashMap<String,String>>> connectedResponse = new HashMap<>();
            HashMap<String, HashMap<String, String>> connectedInnerResponse = new HashMap<>();

            HashMap<String, String> response = new HashMap<>();

            if(identity.equals("driver")){
                response.put("driver", "connected");
            }
            else if(identity.equals("passenger")){
                response.put("passenger", "connected");
            }

            connectedInnerResponse.put(mqtt_id, response);
            connectedResponse.put(topic, connectedInnerResponse);

            customCallBack.publishMessage(topic, connectedResponse, 2, false);


            return mqttClient;
        }catch (MqttException e){
            e.printStackTrace();
        }

        return null;
    }


}
