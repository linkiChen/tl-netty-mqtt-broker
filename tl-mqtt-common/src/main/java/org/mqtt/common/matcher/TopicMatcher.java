package org.mqtt.common.matcher;

import org.mqtt.common.exception.IllegalTopicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class TopicMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicMatcher.class);

    public static final char TOPIC_WILDCARDS_ONE = '+';
    public static final char TOPIC_WILDCARDS_MORE = '#';

    /**
     * 校验订阅的主题是否合法
     *
     * @param topicFilter 订阅的主题(包含通配符 #、+)
     * @throws IllegalTopicException
     */
    public boolean validateTopicFilter(String topicFilter) throws IllegalTopicException {
        if (topicFilter == null || topicFilter.isEmpty()) {
            LOGGER.error("TopicFilter is blank:{}", topicFilter);
            return false;
//            throw new IllegalArgumentException("TopicFilter is blank:" + topicFilter);
        }
        char[] topicFilterChars = topicFilter.toCharArray();
        int topicFilterLength = topicFilterChars.length;
        int topicFilterIdxEnd = topicFilterLength - 1;
        char ch;
        for (int i = 0; i < topicFilterLength; i++) {
            ch = topicFilterChars[i];
            if (Character.isWhitespace(ch)) {
                LOGGER.error("Mqtt subscribe topicFilter has white space:{}", topicFilter);
                return false;
//                throw new IllegalTopicException("Mqtt subscribe topicFilter has white space:" + topicFilter);
            } else if (ch == TOPIC_WILDCARDS_MORE) {
                // 校验: # 通配符只能在最后一位
                if (i < topicFilterIdxEnd) {
//                    throw new IllegalTopicException("Mqtt subscribe topicFilter illegal:" + topicFilter);
                    LOGGER.error("Mqtt subscribe topicFilter illegal:{}", topicFilter);
                    return false;
                }
            } else if (ch == TOPIC_WILDCARDS_ONE) {
                // 校验: 单独 + 是允许的，判断 + 号前一位是否为 /，如果有后一位也必须为 /
                if ((i > 0 && topicFilterChars[i - 1] != '/') || (i < topicFilterIdxEnd && topicFilterChars[i + 1] != '/')) {
//                    throw new IllegalTopicException("Mqtt subscribe topicFilter illegal:" + topicFilter);
                    LOGGER.error("Mqtt subscribe topicFilter illegal:{}", topicFilter);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断给定的订阅主题中是否有不合法的主题
     *
     * @param topicSubscriptions 订阅主题列表
     * @return
     */
    public boolean validateTopicFilter(List<String> topicSubscriptions) {
        return !topicSubscriptions.stream().map(this::validateTopicFilter).collect(Collectors.toSet()).contains(false);
    }

    /**
     * 判断 topicFilter与topicName是否匹配
     *
     * @param topicFilter
     * @param topicName
     * @return
     */
    public boolean match(String topicFilter, String topicName) {
        char[] topicFilterChars = topicFilter.toCharArray();
        char[] topicNameChars = topicName.toCharArray();
        int topicFilterLength = topicFilterChars.length;
        int topicNameLength = topicNameChars.length;
        int topicFilterIdxEnd = topicFilterLength - 1;
        int topicNameIdxEnd = topicNameLength - 1;
        char ch;
        // 是否进入 + 号层级通配符
        boolean inLayerWildcard = false;
        int wildcardCharLen = 0;
        topicFilterLoop:
        for (int i = 0; i < topicFilterLength; i++) {
            ch = topicFilterChars[i];
            if (ch == TOPIC_WILDCARDS_MORE) {
                // 校验: # 通配符只能在最后一位
                if (i < topicFilterIdxEnd) {
                    throw new IllegalTopicException("Mqtt subscribe topicFilter illegal:" + topicFilter);
                }
                return true;
            } else if (ch == TOPIC_WILDCARDS_ONE) {
                // 校验: 单独 + 是允许的，判断 + 号前一位是否为 /，如果有后一位也必须为 /
                if ((i > 0 && topicFilterChars[i - 1] != '/') || (i < topicFilterIdxEnd && topicFilterChars[i + 1] != '/')) {
                    throw new IllegalTopicException("Mqtt subscribe topicFilter illegal:" + topicFilter);
                }
                // 如果 + 是最后一位，判断 topicName 中是否还存在层级 /
                // topicName index
                int topicNameIdx = i + wildcardCharLen;
                if (i == topicFilterIdxEnd && topicNameLength > topicNameIdx) {
                    for (int j = topicNameIdx; j < topicNameLength; j++) {
                        if (topicNameChars[j] == '/') {
                            return false;
                        }
                    }
                    return true;
                }
                inLayerWildcard = true;
            } else if (ch == '/') {
                if (inLayerWildcard) {
                    inLayerWildcard = false;
                }
                // 预读下一位，如果是 #，并且 topicName 位数已经不足
                int next = i + 1;
                if ((topicFilterLength > next) && topicFilterChars[next] == '#' && topicNameLength < next) {
                    return true;
                }
            }
            // topicName 长度不够了
            if (topicNameIdxEnd < i) {
                return false;
            }
            // 进入通配符
            if (inLayerWildcard) {
                for (int j = i + wildcardCharLen; j < topicNameLength; j++) {
                    if (topicNameChars[j] == '/') {
                        wildcardCharLen--;
                        continue topicFilterLoop;
                    } else {
                        wildcardCharLen++;
                    }
                }
            }
            // topicName index
            int topicNameIdx = i + wildcardCharLen;
            // topic 已经完成，topicName 还有数据
            if (topicNameIdx > topicNameIdxEnd) {
                return false;
            }
            if (ch != topicNameChars[topicNameIdx]) {
                return false;
            }
        }
        // 判断 topicName 是否还有数据
        return topicFilterLength + wildcardCharLen + 1 > topicNameLength;
    }
}
