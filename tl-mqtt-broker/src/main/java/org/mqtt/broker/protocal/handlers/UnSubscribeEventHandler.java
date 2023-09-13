package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.mqtt.broker.protocal.EventHandler;
import org.mqtt.cache.api.SubscribeStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 处理取消订阅事件
 */
@Component(value = "unsubscribeEventHandler")
public class UnSubscribeEventHandler implements EventHandler<MqttUnsubscribeMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnSubscribeEventHandler.class);
    private SubscribeStoreService subscribeStoreService;

    public UnSubscribeEventHandler(SubscribeStoreService subscribeStoreService) {
        this.subscribeStoreService = subscribeStoreService;
    }

    @Override
    public void eventProcess(Channel channel, MqttUnsubscribeMessage msg) {
        List<String> topicFilters = msg.payload().topics();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        topicFilters.forEach(topicFilter -> {
            subscribeStoreService.remove(topicFilter, clientId);
            LOGGER.debug("unsubscribe - clientId:{}, topicFilter:{}", clientId, topicFilter);
        });
        MqttUnsubAckMessage unsubAckMessage = (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
        channel.writeAndFlush(unsubAckMessage);
    }
}
