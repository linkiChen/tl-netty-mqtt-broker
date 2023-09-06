package org.mqtt.broker.protocal.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.mqtt.broker.protocal.EventHandler;
import org.mqtt.cache.api.SessionStoreService;
import org.mqtt.cache.entities.SessionContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 客户端连接事件处理器
 */
@Component(value = "connectEventHandler")
public class ConnectEventHandler implements EventHandler<MqttConnectMessage> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectEventHandler.class);
    private SessionStoreService sessionStoreService;

    public ConnectEventHandler(SessionStoreService sessionStoreService) {
        this.sessionStoreService = sessionStoreService;
    }

    /**
     * 是否需要校验客户端
     */
    private boolean needAuth = true;

    @Override
    public void eventProcess(Channel channel, MqttConnectMessage msg) {
        // 消息解码器出现异常
        if (msg.decoderResult().isFailure()) {
            decodeFailProcess(channel, msg);
        }
        if (!clientAuthAndContinue(channel, msg)) {
            return;
        }

        SessionContent sessionContent = new SessionContent(msg.payload().clientIdentifier(), channel, msg.variableHeader().isCleanSession(), null);
        // 处理遗嘱信息
        if (msg.variableHeader().isWillFlag()) {
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(), 0), Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes()));
            sessionContent.setWillMessage(willMessage);
        }
        // 处理连接心跳包
        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            if (channel.pipeline().names().contains("idle")) {
                channel.pipeline().remove("idle");
            }
            channel.pipeline().addFirst("idle", new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }
        // 将客户端的连接信息保存到缓存中
        sessionStoreService.put(sessionContent.getClientId(), sessionContent);
        // 将客户端clientId存储到channel中
        channel.attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
        MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, true), null);
        channel.writeAndFlush(okResp);
    }

    /**
     * 校验mqtt 客户端、用户名和密码
     *
     * @param channel 通道
     * @param msg     消息
     * @return 返回true就断续执行后续逻辑
     */
    private boolean clientAuthAndContinue(Channel channel, MqttConnectMessage msg) {
        String clientId = msg.payload().clientIdentifier();
        String username = msg.payload().userName();
        String password = Objects.isNull(msg.payload().passwordInBytes()) ? null : new String(msg.payload().passwordInBytes(), StandardCharsets.UTF_8);

        // clientId是否为空校验
        if (needAuth && StringUtils.hasText(clientId)) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return false;
        }
        // 校验用户名密码是否正确, 看业务是否需要
        if (needAuth /* && 校验用户名密码 */) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false), null);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return false;
        }
        return true;
    }

    /**
     * 如果消息解码异常交由此方法处理
     *
     * @param channel 通道
     * @param msg     消息
     */
    private void decodeFailProcess(Channel channel, MqttConnectMessage msg) {
        Throwable cause = msg.decoderResult().cause();
        LOGGER.error("消息解码器异常:", cause);
        if (cause instanceof MqttUnacceptableProtocolVersionException) {
            // 协议版本不支付
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return;
        } else if (cause instanceof MqttIdentifierRejectedException) {
            // clientId不合格
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return;
        }
        channel.close();
        return;
    }
}
