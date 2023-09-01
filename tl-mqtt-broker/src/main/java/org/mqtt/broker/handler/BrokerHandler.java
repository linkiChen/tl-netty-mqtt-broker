package org.mqtt.broker.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.mqtt.broker.protocal.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BrokerHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerHandler.class);
    private static final String EVENT_HANDLER_SUFFIX = "EventHandler";
    private Map<String, EventHandler> eventHandlerMap;

    public BrokerHandler(Map<String, EventHandler> eventHandlerMap) {
        this.eventHandlerMap = eventHandlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage mqttMessage) throws Exception {
        MqttMessageType msgType = mqttMessage.fixedHeader().messageType();
        String eventHandlerName = msgType.name().toLowerCase(Locale.ROOT) + EVENT_HANDLER_SUFFIX;
        EventHandler eventHandler = eventHandlerMap.get(eventHandlerName);
        if (Objects.isNull(eventHandler)) {
            LOGGER.error("没有[{}]事件相关的事件处理器", msgType);
        }
        LOGGER.info("eventHandler:{}", eventHandler);
        eventHandler.eventProcess(ctx.channel(), mqttMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("发生异常: ", cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
                // clientId属性值是在客户端连接成功的时候存到channel中的
                String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("clientId")).get();

            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
