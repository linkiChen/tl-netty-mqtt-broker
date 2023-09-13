package org.mqtt.cache.api;

import org.mqtt.cache.entities.DupPublishMessageContent;

import java.util.List;

/**
 * PUBLISH重发消息的存储服务接口,当QoS=1和QoS2时存在重发机制
 */
public interface DupPublishMessageService {

    /**
     * 保存重发消息
     *
     * @param clientId       客户端ID
     * @param messageContent 重发消息的实体
     */
    void put(String clientId, DupPublishMessageContent messageContent);

    /**
     * 根据客户端ID获取所有的重发消息列表
     *
     * @param clientId 客户端ID
     * @return
     */
    List<DupPublishMessageContent> get(String clientId);

    /**
     * 根据客户端ID及消息ID删除重发消息
     *
     * @param clientId 客户端ID
     * @param msgId    消息ID
     */
    void remove(String clientId, Integer msgId);

    /**
     * 移除客户端ID相关的重发消息
     *
     * @param clientId 客户端ID
     */
    void removeByClientId(String clientId);
}
