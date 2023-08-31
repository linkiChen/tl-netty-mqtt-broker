package org.mqtt.broker.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tl.mqtt.broker")
public class BrokerProperties {

    private String brokerId;

    private String sslPwd;

    private Integer port = 1883;
    /**
     * socket参数, 存放已完成三次握手请求的队列最大长度, 默认511长度
     */
    private Integer soBackLog = 511;

    private boolean keepAlive = true;


    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }


    public String getSslPwd() {
        return sslPwd;
    }

    public void setSslPwd(String sslPwd) {
        this.sslPwd = sslPwd;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getSoBackLog() {
        return soBackLog;
    }

    public void setSoBackLog(Integer soBackLog) {
        this.soBackLog = soBackLog;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
}
