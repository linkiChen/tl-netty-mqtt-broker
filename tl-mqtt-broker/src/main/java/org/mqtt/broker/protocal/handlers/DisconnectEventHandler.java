package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.mqtt.broker.protocal.EventHandler;
import org.springframework.stereotype.Component;

/**
 * 处理连接断开事件
 */
@Component(value = "disconnectEventHandler")
public class DisconnectEventHandler implements EventHandler<MqttMessage> {
    @Override
    public void eventProcess(Channel channel, MqttMessage msg) {

    }
}
