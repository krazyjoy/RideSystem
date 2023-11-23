//package com.rideSystem.Ride.mqtt_ORIG;
//
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.springframework.stereotype.Component;
//
///**
// * @Author:Dong
// * @Date：2020/7/31 9:59
// */
//@Slf4j
//@Component
//public class MqttSubClient_orig {
//
//    private MqttPushClient_orig mqttPushClient;
//
//    public MqttSubClient_orig(MqttPushClient_orig mqttPushClient){
//
//        //subScribeDataPublishTopic(topic, mqttPushClient);
//
//    }
//
//    private void subScribeDataPublishTopic(String topic, MqttPushClient_orig mqttPushClient){
//        //订阅test_queue主题
//       subscribe(topic);
////
////       MqttClient client = mqttPushClient.getClient();
////       if(client == null)
////           return;
////       mqttPushClient.setClient(client);
////       mqttPushClient.publish("some_topic2","2 param hi");
////       mqttPushClient.publish(2, false, "some_topic2", "4 param hi");
//    }
//
//    /**
//     * 订阅某个主题，qos默认为0
//     *
//     * @param topic
//     */
//    public void subscribe(String topic) {
//        subscribe(topic, 2);
//    }
//
//    /**
//     * 订阅某个主题
//     *
//     * @param topic 主题名
//     * @param qos
//     */
//    public void subscribe(String topic, int qos) {
//        try {
//            MqttClient client = MqttPushClient_orig.getClient();
//            if (client == null) return;
//            client.subscribe(topic, qos);
//
//            log.info("订阅主题:{}",topic);
//
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void unsubscribe(String topic){
//        try{
//            MqttClient client = MqttPushClient_orig.getClient();
//            if(client == null) return;
//            client.unsubscribe(topic);
//        }catch(MqttException e){
//            e.printStackTrace();
//        }
//    }
//
//}
