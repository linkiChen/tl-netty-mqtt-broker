package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.mqtt.broker.protocal.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 处理客户端发送心跳事件
 */
@Component(value = "pingreqEventHandler")
public class PingReqEventHandler implements EventHandler<MqttMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingReqEventHandler.class);

    @Override
    public void eventProcess(Channel channel, MqttMessage msg) {
        MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0), null, null);
        LOGGER.info("PINGREQ - clientId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get());
        channel.writeAndFlush(pingRespMessage);
    }
}
