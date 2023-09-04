package org.mqtt.cache.entities;

import java.io.Serializable;

public class SubscribeContent implements Serializable {

    private static final long serialVersionUID = 1276156087085594264L;

    private String clientId;

    private String topicFilter;

    private int mqttQos;

    public SubscribeContent() {
    }

    public SubscribeContent(String clientId, String topicFilter, int mqttQos) {
        this.clientId = clientId;
        this.topicFilter = topicFilter;
        this.mqttQos = mqttQos;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public void setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
    }

    public int getMqttQos() {
        return mqttQos;
    }

    public void setMqttQos(int mqttQos) {
        this.mqttQos = mqttQos;
    }
}
