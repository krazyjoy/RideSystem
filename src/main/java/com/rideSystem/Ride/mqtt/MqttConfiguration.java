package com.rideSystem.Ride.mqtt;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * mqtt配置类，获取mqtt连接
 */
@Component
@Configuration
@ConfigurationProperties(MqttConfiguration.PREFIX)
@Slf4j
public class MqttConfiguration {

    @Autowired
    private MqttPushClient mqttPushClient;
    //指定配置文件application-local.properties中的属性名前缀
    public static final String PREFIX = "ximo.mqtt";
    private String host;
    private String clientId;
    private String userName;
    private String password;
    private String topic;
    private int timeout;
    private int keepAlive;

    public String getClientid() {
        return clientId;
    }

    public void setClientid(String clientid) {
        this.clientId = clientid;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getKeepalive() {
        return keepAlive;
    }

    public void setKeepalive(int keepalive) {
        this.keepAlive = keepalive;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }



    /**
     * 连接至mqtt服务器，获取mqtt连接
     * @return
     */
    @Bean
    public MqttPushClient getMqttPushClient() {
        //连接至mqtt服务器，获取mqtt连接
        mqttPushClient = new MqttPushClient();
        mqttPushClient.connect(host, clientId, userName, password, timeout, keepAlive);

        //一连接mqtt,就订阅默认需要订阅的主题（如test_queue）
        topic = getTopic();
        new MqttSubClient(mqttPushClient);
        if(mqttPushClient != null){
            log.info("connected to mqttPushClient");
           // mqttPushClient.publish("some_topic2","2 param hi");
           // mqttPushClient.publish(2, false, "some_topic2", "4 param hi");
        }

        return mqttPushClient;
    }
}