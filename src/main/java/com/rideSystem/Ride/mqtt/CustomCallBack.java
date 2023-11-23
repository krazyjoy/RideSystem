package com.rideSystem.Ride.mqtt;
import com.google.gson.Gson;
import com.rideSystem.Ride.DAO.RideDao;
import com.rideSystem.Ride.Scheduled.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import com.rideSystem.Ride.Service_IMPL.MqttServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class CustomCallBack implements MqttCallback{
    RideDao rideDao;


    private MqttClient mqttClient;

    private String lastPayload;

    private ScheduleService scheduleService;
    public CustomCallBack(MqttClient mqttClient, ScheduleService scheduleService) {
        this.mqttClient = mqttClient;
        this.scheduleService = scheduleService;
    }

    @Override
    public void connectionLost(Throwable cause) {
        // Handle connection lost event
        System.out.println("Connection lost. Reconnecting...");
        // Implement your logic for handling connection loss and reconnection here
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // Handle incoming MQTT message
        String payload = new String(message.getPayload(), "UTF-8");
        System.out.println("Message arrived. Topic: " + topic + ", Payload: " + payload);
        synchronized (this) {
            this.scheduleService.addPayload(topic, payload);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Handle message delivery complete event
        System.out.println("Delivery complete. Token: " + token);
        // Implement your logic for handling message delivery completion here
    }

    public void publishMessage(String topic, Map<String, HashMap<String,HashMap<String,String>>> payload, int qos, boolean retained) throws MqttException {
        String jsonString = new Gson().toJson(payload);
        MqttMessage message = new MqttMessage(jsonString.getBytes());
        log.info("jsonString: {}", jsonString);
        message.setQos(qos);
        message.setRetained(retained);
        mqttClient.publish(topic, message);
    }


    public String getLastPayload(){
        return lastPayload;
    }

}
