package org.mqtt.cache.entities;

import java.io.Serializable;

/**
 * 存储PUBREL重发的消息
 */
public class DupPubRelMessageContent implements Serializable {

    private static final long serialVersionUID = -4111642532532950980L;

    private String clientId;

    private int messageId;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
