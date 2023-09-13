package org.mqtt.cache.entities;

import java.io.Serializable;

public class RetainMessageContent implements Serializable {
    private static final long serialVersionUID = -7548204047370972779L;

    private String topic;

    private byte[] messageBytes;

    private int mqttQos;

    public String getTopic() {
        return topic;
    }

    public RetainMessageContent setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public RetainMessageContent setMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
        return this;
    }

    public int getMqttQos() {
        return mqttQos;
    }

    public RetainMessageContent setMqttQos(int mqttQos) {
        this.mqttQos = mqttQos;
        return this;
    }
}
