package com.alphalens.mapper;

import com.alphalens.domain.HealthSnapshot;
import com.alphalens.dto.response.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HealthResponseMapper {

    private final String serviceName;

    public HealthResponseMapper(@Value("${spring.application.name}") String serviceName) {
        this.serviceName = serviceName;
    }

    public HealthResponse toResponse(HealthSnapshot snapshot) {
        return new HealthResponse(
                snapshot.status(),
                snapshot.database(),
                serviceName,
                snapshot.timestamp()
        );
    }
}
