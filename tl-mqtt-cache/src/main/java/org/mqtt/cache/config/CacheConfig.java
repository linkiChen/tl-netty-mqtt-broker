package org.mqtt.cache.config;

import org.mqtt.cache.api.SessionStoreService;
import org.mqtt.cache.api.SubscribeStoreService;
import org.mqtt.cache.local.DefaultSessionStoreService;
import org.mqtt.cache.local.DefaultSubscribeStoreService;
import org.mqtt.common.matcher.TopicMatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnMissingBean(SubscribeStoreService.class)
    public SubscribeStoreService subscribeStoreService(TopicMatcher topicMatcher) {
        return new DefaultSubscribeStoreService(topicMatcher);
    }

    @Bean
    @ConditionalOnMissingBean(SessionStoreService.class)
    public SessionStoreService sessionStoreService() {
        return new DefaultSessionStoreService();
    }

    @Bean
    @ConditionalOnMissingBean(TopicMatcher.class)
    public TopicMatcher topicMatcher() {
        return new TopicMatcher();
    }
}
