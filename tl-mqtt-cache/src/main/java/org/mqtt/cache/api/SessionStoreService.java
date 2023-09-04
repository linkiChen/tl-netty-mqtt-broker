package org.mqtt.cache.api;

import org.mqtt.cache.entities.SessionContent;

/**
 * 会话管理接口
 */
public interface SessionStoreService {
    /**
     * 存储会话
     *
     * @param clientId 客户端id
     * @param session  会话实体
     */
    void put(String clientId, SessionContent session);

    /**
     * 根据客户端id获取会话实体
     *
     * @param clientId 客户端id
     * @return
     */
    SessionContent get(String clientId);

    /**
     * 判断是否包含指定的客户端id
     *
     * @param clientId 客户端id
     * @return
     */
    boolean containsClientId(String clientId);

    /**
     * 移除会话
     *
     * @param clientId 客户端id
     */
    void removeSession(String clientId);

}
