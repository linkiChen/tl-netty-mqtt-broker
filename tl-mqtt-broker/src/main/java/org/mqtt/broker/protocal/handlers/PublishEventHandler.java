package org.mqtt.broker.protocal.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.mqtt.broker.protocal.EventHandler;
import org.mqtt.cache.api.*;
import org.mqtt.cache.entities.DupPublishMessageContent;
import org.mqtt.cache.entities.RetainMessageContent;
import org.mqtt.cache.entities.SubscribeContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 处理发布消息事件
 */
@Component(value = "publishEventHandler")
public class PublishEventHandler implements EventHandler<MqttPublishMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishEventHandler.class);

    private SessionStoreService sessionStoreService;
    private SubscribeStoreService subscribeStoreService;
    private MessageIdService messageIdService;
    private RetainMessageStoreService retainMessageStoreService;
    private DupPublishMessageService dupPublishMessageService;

    public PublishEventHandler(SessionStoreService sessionStoreService,
                               SubscribeStoreService subscribeStoreService,
                               MessageIdService messageIdService,
                               RetainMessageStoreService retainMessageStoreService,
                               DupPublishMessageService dupPublishMessageService) {
        this.dupPublishMessageService = dupPublishMessageService;
        this.messageIdService = messageIdService;
        this.retainMessageStoreService = retainMessageStoreService;
        this.sessionStoreService = sessionStoreService;
        this.subscribeStoreService = subscribeStoreService;
    }

    @Override
    public void eventProcess(Channel channel, MqttPublishMessage msg) {
        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
        String topicName = msg.variableHeader().topicName();
        MqttQoS mqttQoS = msg.fixedHeader().qosLevel();

        this.sendPublishMessage(topicName, mqttQoS, messageBytes, false, false);
        if (mqttQoS == MqttQoS.AT_LEAST_ONCE) {
            this.sendPubAckMessage(channel, msg.variableHeader().packetId());
        }
        if (mqttQoS == MqttQoS.EXACTLY_ONCE) {
            this.sendPubRecMessage(channel, msg.variableHeader().packetId());
        }
        if (msg.fixedHeader().isRetain()) {
            if (messageBytes.length == 0) {
                retainMessageStoreService.remove(msg.variableHeader().topicName());
            } else {
                RetainMessageContent retainMessageContent = new RetainMessageContent()
                        .setTopic(topicName)
                        .setMqttQos(mqttQoS.value())
                        .setMessageBytes(messageBytes);
                retainMessageStoreService.put(topicName, retainMessageContent);
            }
        }
    }

    /**
     * 向订阅者发送消息
     *
     * @param topic        发送的消息主题
     * @param mqttQoS      消息质量
     * @param messageBytes 消息内容
     * @param retain       是否retain消息
     * @param dup          是否dup消息
     */
    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeContent> subscribeContents = subscribeStoreService.search(topic);
        subscribeContents.forEach(content -> {
            // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
            MqttQoS respQoS = mqttQoS.value() > content.getMqttQos() ? MqttQoS.valueOf(content.getMqttQos()) : mqttQoS;
            Integer messageId = respQoS == MqttQoS.AT_MOST_ONCE ? 0 : messageIdService.nextMessageId();

            // 向订阅者发送消息
            MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                    new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
            LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", content.getClientId(), topic, respQoS.value());
            sessionStoreService.get(content.getClientId()).getChannel().writeAndFlush(publishMessage);

            if (respQoS == MqttQoS.AT_LEAST_ONCE || respQoS == MqttQoS.EXACTLY_ONCE) {
                DupPublishMessageContent dupPublishMessageStore = new DupPublishMessageContent();
                dupPublishMessageStore.setClientId(content.getClientId());
                dupPublishMessageStore.setTopic(topic);
                dupPublishMessageStore.setMqttQoS(respQoS.value());
                dupPublishMessageStore.setMessageBytes(messageBytes);
                dupPublishMessageService.put(content.getClientId(), dupPublishMessageStore);
            }
        });
    }

    private void sendPubAckMessage(Channel channel, int messageId) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        channel.writeAndFlush(pubAckMessage);
    }

    private void sendPubRecMessage(Channel channel, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        channel.writeAndFlush(pubRecMessage);
    }
}
