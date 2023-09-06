package org.mqtt.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "org.mqtt")
public class AppBroker {

    public static void main(String[] args) {
        SpringApplication.run(AppBroker.class, args);
    }
}
