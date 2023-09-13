package org.mqtt.broker.protocal.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.mqtt.broker.protocal.EventHandler;
import org.mqtt.cache.api.MessageIdService;
import org.mqtt.cache.api.RetainMessageStoreService;
import org.mqtt.cache.api.SubscribeStoreService;
import org.mqtt.cache.entities.RetainMessageContent;
import org.mqtt.cache.entities.SubscribeContent;
import org.mqtt.common.matcher.TopicMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理订阅请求事件
 */
@Component(value = "subscribeEventHandler")
public class SubscribeEventHandler implements EventHandler<MqttSubscribeMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeEventHandler.class);

    private MessageIdService messageIdService;
    private SubscribeStoreService subscribeStoreService;
    private RetainMessageStoreService retainMessageStoreService;
    private TopicMatcher topicMatcher;


    public SubscribeEventHandler(MessageIdService messageIdService,
                                 SubscribeStoreService subscribeStoreService,
                                 RetainMessageStoreService retainMessageStoreService,
                                 TopicMatcher topicMatcher) {
        this.messageIdService = messageIdService;
        this.subscribeStoreService = subscribeStoreService;
        this.retainMessageStoreService = retainMessageStoreService;
        this.topicMatcher = topicMatcher;
    }

    @Override
    public void eventProcess(Channel channel, MqttSubscribeMessage msg) {
        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        List<String> topicNameList = topicSubscriptions.stream().map(MqttTopicSubscription::topicName).collect(Collectors.toList());
        if (topicMatcher.validateTopicFilter(topicNameList)) {
            String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
            List<Integer> mqttQoSList = new ArrayList<>();
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                SubscribeContent subscribeContent = new SubscribeContent(clientId, topicFilter, mqttQoS.value());
                subscribeStoreService.put(topicFilter, subscribeContent);
                mqttQoSList.add(mqttQoS.value());
                LOGGER.debug("subscribe - clientId:{}, topicFilter:{}, qos:{}", clientId, topicFilter, mqttQoS.value());
            });

            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(mqttQoSList));
            channel.writeAndFlush(subAckMessage);
            // 发送retain消息
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                this.sendRetainMessage(channel, topicFilter, mqttQoS);
            });
        } else {
            LOGGER.debug("there is/are illegal subscribe topic in:{}", topicNameList);
            channel.close();
        }
    }

    /**
     * 发送retain消息
     *
     * @param channel     netty channel
     * @param topicFilter 主题
     * @param mqttQos     消息质量
     */
    private void sendRetainMessage(Channel channel, String topicFilter, MqttQoS mqttQos) {
        List<RetainMessageContent> retainMsgList = retainMessageStoreService.search(topicFilter);
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        retainMsgList.forEach(content -> {
            MqttQoS respQos = content.getMqttQos() > mqttQos.value() ? mqttQos : MqttQoS.valueOf(content.getMqttQos());
            int messageId = respQos == MqttQoS.AT_MOST_ONCE ? 0 : messageIdService.nextMessageId();
            MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQos, false, 0),
                    new MqttPublishVariableHeader(content.getTopic(), messageId), Unpooled.buffer().writeBytes(content.getMessageBytes())
            );
            LOGGER.debug("publish - clientId:{}, topic:{}, qos:{}, messageId:{}", clientId, content.getTopic(), respQos.value(), messageId);
            channel.writeAndFlush(publishMessage);
        });
    }
}
