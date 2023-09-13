package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import org.mqtt.broker.protocal.EventHandler;
import org.mqtt.cache.api.DupPubRelMessageService;
import org.mqtt.cache.api.DupPublishMessageService;
import org.mqtt.cache.api.SessionStoreService;
import org.mqtt.cache.api.SubscribeStoreService;
import org.mqtt.cache.entities.SessionContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 处理连接断开事件
 */
@Component(value = "disconnectEventHandler")
public class DisconnectEventHandler implements EventHandler<MqttMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisconnectEventHandler.class);

    private SessionStoreService sessionStoreService;
    private SubscribeStoreService subscribeStoreService;
    private DupPublishMessageService dupPublishMessageService;
    private DupPubRelMessageService dupPubRelMessageService;

    public DisconnectEventHandler(SessionStoreService sessionStoreService,
                                  SubscribeStoreService subscribeStoreService,
                                  DupPublishMessageService dupPublishMessageService,
                                  DupPubRelMessageService dupPubRelMessageService) {
        this.sessionStoreService = sessionStoreService;
        this.subscribeStoreService = subscribeStoreService;
        this.dupPublishMessageService = dupPublishMessageService;
        this.dupPubRelMessageService = dupPubRelMessageService;
    }

    @Override
    public void eventProcess(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        SessionContent sessionContent = sessionStoreService.get(clientId);
        if (sessionContent.isCleanSession()) {
            subscribeStoreService.remoteByClient(clientId);
            dupPublishMessageService.removeByClientId(clientId);
            dupPubRelMessageService.removeByClient(clientId);
        }
        LOGGER.debug("DISCONNECT: clientId:{}, cleanSession:{}", clientId, sessionContent.isCleanSession());
        sessionStoreService.removeSession(clientId);
        channel.close();
    }
}
