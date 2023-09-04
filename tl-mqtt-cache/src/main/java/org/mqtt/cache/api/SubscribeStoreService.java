package org.mqtt.cache.api;

import org.mqtt.cache.entities.SubscribeContent;

import java.util.List;

/**
 *
 */
public interface SubscribeStoreService {
    /**
     * 添加指定主题的订单
     *
     * @param topicFilter 主题
     * @param subscribe   订阅
     */
    void put(String topicFilter, SubscribeContent subscribe);

    /**
     * 删除指定客户端中给定主题的订阅
     *
     * @param topicFilter 主题
     * @param clientId    客户端id
     */
    void remove(String topicFilter, String clientId);

    /**
     * 删除指定clientId的所有订阅
     *
     * @param clientId 客户端ID
     */
    void remoteByClient(String clientId);

    /**
     * 获取指定主题的所有订阅
     *
     * @param topic 主题
     * @return
     */
    List<SubscribeContent> search(String topic);
}
