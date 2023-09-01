package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import org.mqtt.broker.protocal.EventHandler;
import org.springframework.stereotype.Component;

/**
 * 处理订阅请求事件
 */
@Component(value = "subscribeEventHandler")
public class SubscribeEventHandler implements EventHandler<MqttUnsubscribeMessage> {
    @Override
    public void eventProcess(Channel channel, MqttUnsubscribeMessage msg) {

    }
}
