package org.mqtt.broker.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrokerHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage mqttMessage) throws Exception {
        MqttMessageType msgType = mqttMessage.fixedHeader().messageType();
        Channel channel = ctx.channel();
        LOGGER.info("mqtt message type:{}", msgType);
        switch (msgType) {
            case CONNECT:
                MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, true), null);
                channel.writeAndFlush(okResp);
                break;
            case PINGREQ:
                MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0), null, null);
                LOGGER.debug("PINGREQ - clientId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get());
                channel.writeAndFlush(pingRespMessage);
                break;
        }
    }
}
