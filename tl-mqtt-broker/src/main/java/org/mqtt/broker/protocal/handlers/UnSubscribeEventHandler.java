package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import org.mqtt.broker.protocal.EventHandler;
import org.springframework.stereotype.Component;

/**
 * 处理取消订阅事件
 */
@Component(value = "unsubscribeEventHandler")
public class UnSubscribeEventHandler implements EventHandler<MqttUnsubscribeMessage> {
    @Override
    public void eventProcess(Channel channel, MqttUnsubscribeMessage msg) {

    }
}
