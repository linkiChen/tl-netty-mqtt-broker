package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.mqtt.broker.protocal.EventHandler;
import org.springframework.stereotype.Component;

@Component(value = "authEventHandler")
public class AuthEventHandler implements EventHandler<MqttMessage> {
    @Override
    public void eventProcess(Channel channel, MqttMessage msg) {

    }
}
