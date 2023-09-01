package org.mqtt.broker.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import org.mqtt.broker.handler.BrokerHandler;
import org.mqtt.broker.properties.BrokerProperties;
import org.mqtt.broker.protocal.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Map;

@Component
public class BrokerServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerServer.class);
    private LogLevel logLevel;

    // 读取的配置属性
    private BrokerProperties brokerProperties;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel channel;
    private Map<String, EventHandler> eventHandlerMap;

    public BrokerServer(BrokerProperties properties, Map<String, EventHandler> ehm) {
        this.brokerProperties = properties;
        logLevel = LOGGER.isDebugEnabled() ? LogLevel.DEBUG : LogLevel.INFO;
        this.eventHandlerMap = ehm;
    }


    @PostConstruct
    public void start() throws Exception {
        LOGGER.info("Initializing {} MQTT Broker ...", "[" + brokerProperties.getBrokerId() + "]");
        bossGroup = this.useEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        workerGroup = this.useEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
//        KeyStore keyStore = KeyStore.getInstance("PKCS12");
//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("keystore/mqtt-broker.pfx");
//        keyStore.load(inputStream, brokerProperties.getSslPwd().toCharArray());
//        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//        kmf.init(keyStore, brokerProperties.getSslPwd().toCharArray());
//        sslContext = SslContextBuilder.forServer(kmf).build();
        initMqttServerAndRun();
//        websocketServer();
        LOGGER.info("MQTT Broker {} is up and running on port: {}", "[" + brokerProperties.getBrokerId() + "]", brokerProperties.getPort());
    }

    private void initMqttServerAndRun() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {

            bootstrap.group(bossGroup, workerGroup)
                    .channel(this.useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    // 初始化时就执行此handler
                    .handler(new LoggingHandler(logLevel))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 心跳检测
                            pipeline.addFirst("idle", new IdleStateHandler(0, 0, brokerProperties.getSoBackLog()));
                            pipeline.addLast("decoder", new MqttDecoder());
                            pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                            pipeline.addLast("brokerHandler", new BrokerHandler(eventHandlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBackLog())
                    .option(ChannelOption.SO_KEEPALIVE, brokerProperties.isKeepAlive());
            channel = bootstrap.bind(brokerProperties.getPort()).sync().channel();
        } catch (Exception e) {
            LOGGER.error("初始化mqtt broker失败:", e);
        }
    }

    private boolean useEpoll() {
        // todo 待实现,根据系统判断
        return false;
    }

}
