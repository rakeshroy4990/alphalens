package com.alphalens.integration.broker;

import java.util.List;
import java.util.Map;

public interface BrokerProvider {

    String name();

    Map<String, Object> loginUrl();

    Map<String, Object> status(boolean consent);

    List<Map<String, Object>> holdings();

    List<Map<String, Object>> positions();

    List<Map<String, Object>> orders();

    List<Map<String, Object>> trades();
}
