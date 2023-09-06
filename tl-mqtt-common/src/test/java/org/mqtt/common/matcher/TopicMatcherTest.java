package org.mqtt.common.matcher;

import org.junit.Before;
import org.junit.Test;

public class TopicMatcherTest {

    private TopicMatcher matcher;

    @Before
    public void before() {
        matcher = new TopicMatcher();
    }

    @Test
    public void matchTest() {
        String topicFilter = "a/b/c";
        String topic = "a/b/c";
        System.out.println(matcher.match(topicFilter, topic));

        topicFilter = "a/b/d";
        System.out.println(matcher.match(topicFilter, topic));

        topicFilter = "a/b/#";
        System.out.println(matcher.match(topicFilter, topic));

        topicFilter = "a/b/+";
        System.out.println(matcher.match(topicFilter, topic));

        topicFilter = "a/+/c";
        System.out.println(matcher.match(topicFilter, topic));

        topicFilter = "+/b/c";
        System.out.println(matcher.match(topicFilter, topic));

        System.out.println(matcher.match("a/b/#", "a/b/c/d/e"));
    }
}
