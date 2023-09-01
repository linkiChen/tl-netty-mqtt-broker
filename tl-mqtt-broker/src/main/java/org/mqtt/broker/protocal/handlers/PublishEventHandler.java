package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.mqtt.broker.protocal.EventHandler;
import org.springframework.stereotype.Component;

/**
 * 处理发布消息事件
 */
@Component(value = "publishEventHandler")
public class PublishEventHandler implements EventHandler<MqttPublishMessage> {
    @Override
    public void eventProcess(Channel channel, MqttPublishMessage msg) {

    }
}
