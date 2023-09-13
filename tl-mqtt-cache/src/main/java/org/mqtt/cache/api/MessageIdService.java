package org.mqtt.cache.api;

public interface MessageIdService {
    /**
     * 获取报文标识符
     *
     * @return
     */
    int nextMessageId();

    /**
     * 释放报文标识符
     *
     * @param messageId 报文标识符
     */
    void releaseMessageId(int messageId);
}
