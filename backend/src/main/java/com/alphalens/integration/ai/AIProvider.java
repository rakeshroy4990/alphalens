package com.alphalens.integration.ai;

import java.util.Map;

public interface AIProvider {

    String name();

    String explain(Map<String, Object> structuredInputs);
}
