package org.mqtt.broker.protocal;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;

public interface EventHandler<M extends MqttMessage> {

    void eventProcess(Channel channel, M msg);
}
