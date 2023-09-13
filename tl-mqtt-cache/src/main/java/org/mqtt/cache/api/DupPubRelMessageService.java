package org.mqtt.cache.api;

import org.mqtt.cache.entities.DupPubRelMessageContent;

import java.util.List;

/**
 * PUBREL重发消息存储的服务接口, 当QoS=2时存在该 重发机制
 */
public interface DupPubRelMessageService {
    /**
     * 保存重发消息
     *
     * @param clientId       客户湍ID
     * @param messageContent 重发消息实体
     */
    void put(String clientId, DupPubRelMessageContent messageContent);

    /**
     * 获取指定客户端ID相关的重发消息
     *
     * @param clientId 客户湍ID
     * @return
     */
    List<DupPubRelMessageContent> get(String clientId);

    /**
     * 删除指定客户端ID及消息ID的重发消息
     *
     * @param clientId 客户湍ID
     * @param msgId    消息ID
     */
    void remove(String clientId, int msgId);

    /**
     * 删除客户端ID相关的消息
     *
     * @param clientId 客户湍ID
     */
    void removeByClient(String clientId);
}
