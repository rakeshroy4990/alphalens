package com.alphalens.integration.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoggingNotificationPort implements NotificationPort {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingNotificationPort.class);

    @Override
    public void send(String channel, String title, Map<String, Object> payload) {
        LOG.info("notification channel={} title={} payload={}", channel, title, payload);
    }
}
