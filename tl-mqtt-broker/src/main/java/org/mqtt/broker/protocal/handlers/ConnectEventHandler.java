package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.mqtt.broker.protocal.EventHandler;
import org.springframework.stereotype.Component;

/**
 * 客户端连接事件处理器
 */
@Component(value = "connectEventHandler")
public class ConnectEventHandler implements EventHandler<MqttConnectMessage> {
    @Override
    public void eventProcess(Channel channel, MqttConnectMessage msg) {
        MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, true), null);
        channel.writeAndFlush(okResp);
    }
}
