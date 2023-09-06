package org.mqtt.cache.local;

import org.junit.Before;
import org.junit.Test;
import org.mqtt.cache.entities.SubscribeContent;
import org.mqtt.common.matcher.TopicMatcher;

public class DefaultSubscribeStoreServiceTest {

    private TopicMatcher topicMatcher;
    private DefaultSubscribeStoreService subscribeStoreService;

    @Before
    public void before() {
        topicMatcher = new TopicMatcher();
        subscribeStoreService = new DefaultSubscribeStoreService(topicMatcher);
    }

    @Test
    public void notWildcardTest() {
        SubscribeContent sc1 = new SubscribeContent("client_001", "a/b/c", 0);
        SubscribeContent sc2 = new SubscribeContent("client_002", "a/b/c", 0);
        SubscribeContent sc3 = new SubscribeContent("client_003", "a/b/d", 0);
        SubscribeContent sc4 = new SubscribeContent("client_001", "a/b/e", 0);

        subscribeStoreService.put(sc1.getTopicFilter(), sc1);
        subscribeStoreService.put(sc2.getTopicFilter(), sc2);
        subscribeStoreService.put(sc3.getTopicFilter(), sc3);
        subscribeStoreService.put(sc4.getTopicFilter(), sc4);

        System.out.println(subscribeStoreService.search("a/b/c"));
        System.out.println(subscribeStoreService.search("a/b/d"));
        System.out.println(subscribeStoreService.search("a/b/f"));

        subscribeStoreService.remove("a/b/c", "client_002");
        System.out.println(subscribeStoreService.search("a/b/c"));

        System.out.println("-----通过clientId移除-----");
        System.out.println(subscribeStoreService.search("a/b/e"));
        subscribeStoreService.remoteByClient("client_001");
        System.out.println(subscribeStoreService.search("a/b/e"));
        System.out.println(subscribeStoreService.search("a/b/c"));
    }


    @Test
    public void wildcardTest() {
        SubscribeContent sc1 = new SubscribeContent("client_001", "+/b/c", 0);
        SubscribeContent sc2 = new SubscribeContent("client_002", "a/+/c", 0);
        SubscribeContent sc3 = new SubscribeContent("client_003", "a/b/+", 0);
        SubscribeContent sc4 = new SubscribeContent("client_001", "a/b/#", 0);

        subscribeStoreService.put(sc1.getTopicFilter(), sc1);
        subscribeStoreService.put(sc2.getTopicFilter(), sc2);
        subscribeStoreService.put(sc3.getTopicFilter(), sc3);
        subscribeStoreService.put(sc4.getTopicFilter(), sc4);

        System.out.println(subscribeStoreService.search("ab/b/c"));

        System.out.println(subscribeStoreService.search("a/bb/c"));
    }
}
