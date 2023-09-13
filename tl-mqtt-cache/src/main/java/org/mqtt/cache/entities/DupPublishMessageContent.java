package org.mqtt.cache.entities;

import java.io.Serializable;

/**
 * 存储PUBLISH重发的消息
 */
public class DupPublishMessageContent implements Serializable {


    private static final long serialVersionUID = -8112511377194421600L;

    private String clientId;

    private String topic;

    private int mqttQoS;

    private int messageId;

    private byte[] messageBytes;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getMqttQoS() {
        return mqttQoS;
    }

    public void setMqttQoS(int mqttQoS) {
        this.mqttQoS = mqttQoS;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public void setMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
    }
}
