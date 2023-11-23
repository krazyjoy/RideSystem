//package com.rideSystem.Ride.mqtt_ORIG;
//
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.paho.client.mqttv3.*;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Component
//public class MqttPushClient_orig {
//
//    @Autowired
//    private PushCallback_orig pushCallback;
//
//    private static MqttClient client;
//
//    private static String rideId;
//
//    public static void setClient(MqttClient client) {
//        MqttPushClient_orig.client = client;
//    }
//    public static void setRideId(String rideId){
//        MqttPushClient_orig.rideId = rideId;
//    }
//
//    public static MqttClient getClient() {
//        return client;
//    }
//
//    public static String getRideId(){return rideId;}
//
//    public void connect(String host, String clientID, String username, String password, int timeout, int keepalive) {
//        MqttClient client;
//        try {
//            client = new MqttClient(host, clientID, new MemoryPersistence());
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setCleanSession(true);
//            options.setUserName(username);
//            options.setPassword(password.toCharArray());
//            options.setConnectionTimeout(timeout);
//            options.setKeepAliveInterval(keepalive);
//            MqttPushClient_orig.setClient(client);
//            MqttPushClient_orig.setRideId(rideId);
//            try {
//                //设置回调类
//                client.setCallback(pushCallback);
//                //client.connect(options);
//                IMqttToken iMqttToken = client.connectWithResult(options);
//                boolean complete = iMqttToken.isComplete();
//                log.info("MQTT连接"+(complete?"成功":"失败"));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * 发布，默认qos为0，非持久化
//     *
//     * @param topic 主题名
//     * @param pushMessage 消息
//     */
//    public void publish(String topic, HashMap<String, Object> pushMessage) {
//        log.info("publish...");
//        publish(0, false, topic, pushMessage);
//    }
//
//    /**
//     * 发布
//     *
//     * @param qos
//     * @param retained
//     * @param topic
//     * @param pushMessage
//     */
//    public void publish(int qos, boolean retained, String topic, Map<String,Object> pushMessage) {
//        MqttMessage message = new MqttMessage();
//        message.setQos(qos);
//        message.setRetained(retained);
//        message.setPayload(pushMessage.toString().getBytes());
//        MqttTopic mTopic = MqttPushClient_orig.getClient().getTopic(topic);
//        log.info("checking mTopic: ", mTopic);
//
//
//        if (null == mTopic) {
//            log.info("主题不存在:{}",mTopic);
//        }
//        try {
//            log.info("trying to publish ...");
//            mTopic.publish(message);
//
//
//        } catch (Exception e) {
//            log.error("mqtt发送消息异常:",e);
//        }
//    }
//
//}