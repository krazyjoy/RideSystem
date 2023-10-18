package com.rideSystem.Ride.controller;

import com.rideSystem.Ride.mqtt.MqttPushClient;
import com.rideSystem.Ride.mqtt.MqttSubClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @Author:Dong
 * @Date：2020/8/26 16:12
 */
//@RestController
//@RequestMapping("mqttDemo")
@Component
public class MqttDemoController {
    @Autowired
    private MqttPushClient mqttPushClient;
    private MqttSubClient mqttSubClient;

    public void publishMessage(String topic, String message){

        MqttPushClient mqttPushClient1 = new MqttPushClient();
        HashMap<String,Object> pushMessage = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
//        pushMessage.put(now.toString(), new ).add();
//        mqttPushClient1.publish("hi", "this is client connecting from demo controller");
    }

    /**
     * 通过MqttPushClient类publish方法的发送"这是一条测试消息"到名为"test_queue"的主题，如果需要拿到这条消息，
     * 需要在MqttSubClient类的subScribeDataPublishTopic方法进行配置和订阅该主题（这个DEMO已经配置好,并在连接mqtt服务器时就已经订阅），
     * 配置完成后 PushCallBack类的messageArrived方法会接收到已订阅主题接收到的消息(订阅主题后可以在该方法中处理接收到的消息)
     *
     *Send "This is a test message" to the topic named "test_queue" through the publish method of the MqttPushClient class.
     * If you need to get this message,Need to configure and subscribe to the topic in the subScribeDataPublishTopic
     * method of the MqttSubClient class (this DEMO has been configured and subscribed when connecting to the mqtt server),
     * After the configuration is completed, the messageArrived method of the PushCallBack class will receive the message
     * received by the subscribed topic (you can process the received message in this method after subscribing to the topic)
     */

    @RequestMapping("testPublishMessage1")
    public void testPublishMessage() {
//        mqttPushClient.publish("some_topic", "this is from spring boot channel some topic");
//        mqttPushClient.publish("some_topic","这是一条测试消息");
    }

    @RequestMapping("testPublishMessage2")
    public void testPublishMessage2(String message){
        //mqttPushClient.publish("test_queue",message);
    }

//    @RequestMapping("testPublishMessage3")
//    public void testPublishMessage3(String message){
//        mqttPushClient.publish("publish a message from emqx w/ qos2",message);
//    }
}
