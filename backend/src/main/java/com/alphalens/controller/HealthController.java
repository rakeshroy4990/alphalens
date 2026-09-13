package com.alphalens.controller;

import com.alphalens.domain.ComponentStatus;
import com.alphalens.dto.response.HealthResponse;
import com.alphalens.mapper.HealthResponseMapper;
import com.alphalens.service.HealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;
    private final HealthResponseMapper healthResponseMapper;

    public HealthController(HealthService healthService, HealthResponseMapper healthResponseMapper) {
        this.healthService = healthService;
        this.healthResponseMapper = healthResponseMapper;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        HealthResponse body = healthResponseMapper.toResponse(healthService.currentHealth());
        HttpStatus status = body.status() == ComponentStatus.UP
                ? HttpStatus.OK
                : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(body);
    }
}
