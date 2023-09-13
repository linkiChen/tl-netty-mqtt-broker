package org.mqtt.cache.local;

import org.mqtt.cache.api.MessageIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultMessageIdService implements MessageIdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessageIdService.class);

    private final int MIN_MSG_ID = 1;

    private final int MAX_MSG_ID = 65535;

    private Lock rLock = new ReentrantLock();

    private int nextMsgId = MIN_MSG_ID - 1;

    private Map<Integer, Integer> msgIdCache = new ConcurrentHashMap<>();

    @Override
    public int nextMessageId() {
        rLock.lock();
        try {
            do {
                nextMsgId++;
                if (nextMsgId > MAX_MSG_ID) {
                    nextMsgId = MIN_MSG_ID;
                }
            } while (msgIdCache.containsKey(nextMsgId));
            msgIdCache.put(nextMsgId, nextMsgId);
        } catch (Exception e) {
            LOGGER.error("获取消息ID异常:", e);
        } finally {
            rLock.unlock();
        }
        return nextMsgId;
    }

    @Override
    public void releaseMessageId(int messageId) {
        rLock.lock();
        try {
            msgIdCache.remove(messageId);
        } catch (Exception e) {
            LOGGER.error("释放消息ID异常:", e);
        } finally {
            rLock.unlock();
        }
    }
}
