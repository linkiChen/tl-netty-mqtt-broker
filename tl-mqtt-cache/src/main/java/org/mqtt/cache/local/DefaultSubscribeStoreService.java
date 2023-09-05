package org.mqtt.cache.local;

import org.apache.commons.lang3.StringUtils;
import org.mqtt.cache.api.SubscribeStoreService;
import org.mqtt.cache.entities.SubscribeContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultSubscribeStoreService implements SubscribeStoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSubscribeStoreService.class);

    private Map<String/*topicFilter*/, ConcurrentHashMap<String/*clientId*/, SubscribeContent>> wildCardCache = new ConcurrentHashMap();
    private Map<String/*topicFilter*/, ConcurrentHashMap<String/*clientId*/, SubscribeContent>> notWildCardCache = new ConcurrentHashMap();

    @Override
    public void put(String topicFilter, SubscribeContent subscribe) {
        if (StringUtils.contains(topicFilter, "#") || StringUtils.contains(topicFilter, "+")) {
            ConcurrentHashMap<String, SubscribeContent> topicMap = wildCardCache.getOrDefault(topicFilter, new ConcurrentHashMap<>());
            topicMap.put(subscribe.getClientId(), subscribe);
            wildCardCache.put(topicFilter, topicMap);
        } else {
            ConcurrentHashMap<String, SubscribeContent> topicMap = notWildCardCache.getOrDefault(topicFilter, new ConcurrentHashMap<>());
            topicMap.put(subscribe.getClientId(), subscribe);
            notWildCardCache.put(topicFilter, topicMap);
        }
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        if (StringUtils.contains(topicFilter, "#") || StringUtils.contains(topicFilter, "+")) {
            ConcurrentHashMap<String, SubscribeContent> topicMap = wildCardCache.getOrDefault(topicFilter, new ConcurrentHashMap<>());
            topicMap.remove(clientId);
            // 给定的主题没有了订阅者就把这个主题从缓存中移除
            if (topicMap.isEmpty()) wildCardCache.remove(topicFilter);
        } else {
            ConcurrentHashMap<String, SubscribeContent> topicMap = notWildCardCache.getOrDefault(topicFilter, new ConcurrentHashMap<>());
            topicMap.remove(clientId);
            // 给定的主题没有了订阅者就把这个主题从缓存中移除
            if (topicMap.isEmpty()) notWildCardCache.remove(topicFilter);
        }
    }

    @Override
    public void remoteByClient(String clientId) {
    }

    @Override
    public List<SubscribeContent> search(String topic) {
        ConcurrentHashMap<String, SubscribeContent> resultMap = notWildCardCache.getOrDefault(topic, new ConcurrentHashMap<>());

        return null;
    }
}
