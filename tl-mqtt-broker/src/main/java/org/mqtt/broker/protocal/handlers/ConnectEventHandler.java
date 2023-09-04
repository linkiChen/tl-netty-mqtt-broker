package org.mqtt.broker.protocal.handlers;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.CharsetUtil;
import org.mqtt.broker.protocal.EventHandler;
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
