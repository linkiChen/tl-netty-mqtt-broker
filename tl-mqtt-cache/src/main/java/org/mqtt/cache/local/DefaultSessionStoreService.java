package org.mqtt.cache.local;

import org.mqtt.cache.api.SessionStoreService;
import org.mqtt.cache.entities.SessionContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSessionStoreService implements SessionStoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSessionStoreService.class);
    private Map<String/*clientId*/, SessionContent> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String clientId, SessionContent session) {
        cache.put(clientId, session);
    }

    @Override
    public SessionContent get(String clientId) {
        return cache.get(clientId);
    }

    @Override
    public boolean containsClientId(String clientId) {
        return cache.containsKey(clientId);
    }

    @Override
    public void removeSession(String clientId) {
        cache.remove(clientId);
    }
}
