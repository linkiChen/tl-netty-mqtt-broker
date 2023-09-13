package org.mqtt.cache.local;

import org.mqtt.cache.api.DupPubRelMessageService;
import org.mqtt.cache.api.MessageIdService;
import org.mqtt.cache.entities.DupPubRelMessageContent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultDupPubRelMessageService implements DupPubRelMessageService {
    private MessageIdService messageIdService;

    private Map<String, ConcurrentHashMap<Integer, DupPubRelMessageContent>> cache;

    public DefaultDupPubRelMessageService(MessageIdService messageIdService) {
        this.messageIdService = messageIdService;
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public void put(String clientId, DupPubRelMessageContent messageContent) {
        ConcurrentHashMap<Integer, DupPubRelMessageContent> msgMap = cache.getOrDefault(clientId, new ConcurrentHashMap<>());
        msgMap.put(messageContent.getMessageId(), messageContent);
        cache.put(clientId, msgMap);
    }

    @Override
    public List<DupPubRelMessageContent> get(String clientId) {
        ConcurrentHashMap<Integer, DupPubRelMessageContent> msgMap = cache.getOrDefault(clientId, new ConcurrentHashMap<>());
        return msgMap.values().stream().collect(Collectors.toList());
    }

    @Override
    public void remove(String clientId, int msgId) {
        if (cache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPubRelMessageContent> map = cache.get(clientId);
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
    public void removeByClient(String clientId) {
        if (cache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPubRelMessageContent> map = cache.get(clientId);
            map.forEach((messageId, dupPublishMessageStore) -> {
                messageIdService.releaseMessageId(messageId);
            });
            map.clear();
            cache.remove(clientId);
        }
    }
}
