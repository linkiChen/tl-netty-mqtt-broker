package org.mqtt.cache.local;

import org.apache.commons.lang3.StringUtils;
import org.mqtt.cache.api.SubscribeStoreService;
import org.mqtt.cache.entities.SubscribeContent;
import org.mqtt.common.matcher.TopicMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultSubscribeStoreService implements SubscribeStoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSubscribeStoreService.class);

    private Map<String/*topicFilter*/, ConcurrentHashMap<String/*clientId*/, SubscribeContent>> wildCardCache = new ConcurrentHashMap();
    private Map<String/*topicFilter*/, ConcurrentHashMap<String/*clientId*/, SubscribeContent>> notWildCardCache = new ConcurrentHashMap();

    private TopicMatcher topicMatcher;

    public DefaultSubscribeStoreService(TopicMatcher topicMatcher) {
        this.topicMatcher = topicMatcher;
    }

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
        wildCardCache.forEach((topicFilter, subMap) -> {
            if (subMap.containsKey(clientId)) {
                subMap.remove(clientId);
            }
            if (subMap.isEmpty()) {
                wildCardCache.remove(topicFilter);
            }
        });

        notWildCardCache.forEach((topicFilter, subMap) -> {
            if (subMap.containsKey(clientId)) {
                subMap.remove(clientId);
            }
            if (subMap.isEmpty()) {
                notWildCardCache.remove(topicFilter);
            }
        });
    }

    @Override
    public List<SubscribeContent> search(String topic) {
        List<SubscribeContent> resultList = new ArrayList<>();
        if (notWildCardCache.containsKey(topic)) {
            resultList.addAll(notWildCardCache.get(topic).values().stream().collect(Collectors.toList()));
        }

        wildCardCache.forEach((topicFilter, subMap) -> {
            if (topicMatcher.match(topicFilter, topic)) {
                resultList.addAll(wildCardCache.get(topicFilter).values().stream().collect(Collectors.toList()));
            }
        });
        return resultList;
    }
}
