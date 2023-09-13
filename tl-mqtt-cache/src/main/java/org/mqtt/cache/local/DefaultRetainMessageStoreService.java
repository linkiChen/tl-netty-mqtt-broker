package org.mqtt.cache.local;

import org.apache.commons.lang3.StringUtils;
import org.mqtt.cache.api.RetainMessageStoreService;
import org.mqtt.cache.entities.RetainMessageContent;
import org.mqtt.common.matcher.TopicMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRetainMessageStoreService implements RetainMessageStoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRetainMessageStoreService.class);

    private final ConcurrentHashMap<String/*topicFilter*/, RetainMessageContent> cache = new ConcurrentHashMap<>();
    private TopicMatcher topicMatcher;

    public DefaultRetainMessageStoreService(TopicMatcher topicMatcher) {
        this.topicMatcher = topicMatcher;
    }

    @Override
    public void put(String topic, RetainMessageContent content) {
        cache.put(topic, content);
    }

    @Override
    public RetainMessageContent get(String topic) {
        return cache.get(topic);
    }

    @Override
    public void remove(String topic) {
        cache.remove(topic);
    }

    @Override
    public boolean containsKey(String topic) {
        return cache.containsKey(topic);
    }

    @Override
    public List<RetainMessageContent> search(String topicFilter) {
        List<RetainMessageContent> retainMsgList = new ArrayList<>();
        /*if (!StringUtils.contains(topicFilter, "#") && !StringUtils.contains(topicFilter, "/")) {
            retainMsgList.add(cache.get(topicFilter));
        }*/
        cache.forEach((key, content) -> {
            if (topicMatcher.match(topicFilter, key)) {
                retainMsgList.add(content);
            }
        });
        return retainMsgList;
    }
}
