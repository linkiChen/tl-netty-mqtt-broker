package org.mqtt.cache.local;

import org.mqtt.cache.api.DupPublishMessageService;
import org.mqtt.cache.api.MessageIdService;
import org.mqtt.cache.entities.DupPublishMessageContent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultDupPublishMessageService implements DupPublishMessageService {

    private MessageIdService messageIdService;

    private Map<String, ConcurrentHashMap<Integer, DupPublishMessageContent>> cache;

    public DefaultDupPublishMessageService(MessageIdService messageIdService) {
        this.messageIdService = messageIdService;
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public void put(String clientId, DupPublishMessageContent messageContent) {
        ConcurrentHashMap<Integer, DupPublishMessageContent> msgMap = cache.getOrDefault(clientId, new ConcurrentHashMap<>());
        msgMap.put(messageContent.getMessageId(), messageContent);
        cache.put(clientId, msgMap);
    }

    @Override
    public List<DupPublishMessageContent> get(String clientId) {
        ConcurrentHashMap<Integer, DupPublishMessageContent> msgMap = cache.getOrDefault(clientId, new ConcurrentHashMap<>());
        return msgMap.values().stream().collect(Collectors.toList());
    }

    @Override
    public void remove(String clientId, Integer msgId) {
        if (cache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPublishMessageContent> map = cache.get(clientId);
            if (map.containsKey(msgId)) {
                map.remove(msgId);
                if (map.size() > 0) {
                    cache.put(clientId, map);
                } else {
                    cache.remove(clientId);
                }
            }
        }
    }

    @Override
    public void removeByClientId(String clientId) {
        if (cache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPublishMessageContent> map = cache.get(clientId);
            map.forEach((messageId, dupPublishMessageStore) -> {
                messageIdService.releaseMessageId(messageId);
            });
            map.clear();
            cache.remove(clientId);
        }
    }
}
