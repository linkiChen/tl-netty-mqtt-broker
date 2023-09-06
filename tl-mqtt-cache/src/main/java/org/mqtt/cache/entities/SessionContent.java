package org.mqtt.cache.entities;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

import java.io.Serializable;

/**
 * 会话信息
 */
public class SessionContent implements Serializable {

    private static final long serialVersionUID = 5209539791996944490L;

    // 客户端id
    private String clientId;
    // 客户端连接的通道
    private Channel channel;
    // session是否已清除
    private boolean cleanSession;
    // 遗嘱信息
    private MqttPublishMessage willMessage;

    public SessionContent() {
    }

    public SessionContent(String clientId, Channel channel, boolean cleanSession, MqttPublishMessage willMessage) {
        this.clientId = clientId;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public MqttPublishMessage getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(MqttPublishMessage willMessage) {
        this.willMessage = willMessage;
    }

    @Override
    public String toString() {
        return "SessionContent{" +
                "clientId='" + clientId + '\'' +
                ", channel=" + channel +
                ", cleanSession=" + cleanSession +
                ", willMessage=" + willMessage +
                '}';
    }
}
