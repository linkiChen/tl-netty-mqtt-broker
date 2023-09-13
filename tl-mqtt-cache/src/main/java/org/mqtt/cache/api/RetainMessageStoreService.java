package org.mqtt.cache.api;

import org.mqtt.cache.entities.RetainMessageContent;

import java.util.List;

public interface RetainMessageStoreService {

    /**
     * 存在retain标志消息
     *
     * @param topic   主题
     * @param content retain标识消息内容
     */
    void put(String topic, RetainMessageContent content);

    /**
     * 获取retain消息
     *
     * @param topic 主题
     * @return
     */
    RetainMessageContent get(String topic);

    /**
     * 删除retain标志的消息
     *
     * @param topic 主题
     */
    void remove(String topic);

    /**
     * 判断给定topic的retain消息是否存在
     *
     * @param topic 主题
     * @return
     */
    boolean containsKey(String topic);

    /**
     * 获取指定主题的retain消息集合
     * @param topicFilter
     * @return
     */
    List<RetainMessageContent> search(String topicFilter);
}
