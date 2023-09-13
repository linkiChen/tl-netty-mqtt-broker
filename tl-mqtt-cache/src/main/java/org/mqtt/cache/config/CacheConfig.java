package org.mqtt.cache.config;

import org.mqtt.cache.api.*;
import org.mqtt.cache.local.*;
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

    @Bean
    @ConditionalOnMissingBean(MessageIdService.class)
    public MessageIdService messageIdService() {
        return new DefaultMessageIdService();
    }

    @Bean
    @ConditionalOnMissingBean(DupPublishMessageService.class)
    public DupPublishMessageService dupPublishMessageService(MessageIdService messageIdService) {
        return new DefaultDupPublishMessageService(messageIdService);
    }

    @Bean
    @ConditionalOnMissingBean(DupPubRelMessageService.class)
    public DupPubRelMessageService dupPubRelMessageService(MessageIdService messageIdService) {
        return new DefaultDupPubRelMessageService(messageIdService);
    }

    @Bean
    @ConditionalOnMissingBean(RetainMessageStoreService.class)
    public RetainMessageStoreService retainMessageStoreService(TopicMatcher topicMatcher) {
        return new DefaultRetainMessageStoreService(topicMatcher);
    }

}
