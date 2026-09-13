package com.alphalens.integration.notify;

import java.util.Map;

public interface NotificationPort {

    void send(String channel, String title, Map<String, Object> payload);
}
