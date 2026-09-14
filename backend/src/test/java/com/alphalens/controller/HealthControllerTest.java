package com.alphalens.controller;

import com.alphalens.auth.i18n.ApiMessageResolver;
import com.alphalens.auth.security.BearerTokenAuthenticator;
import com.alphalens.config.CorsConfig;
import com.alphalens.config.SecurityConfig;
import com.alphalens.domain.ComponentStatus;
import com.alphalens.domain.HealthSnapshot;
import com.alphalens.mapper.HealthResponseMapper;
import com.alphalens.service.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthController.class, properties = {
        "spring.application.name=alphalens-backend",
        "app.cors.allowed-origin-patterns=http://localhost:*",
        "app.auth.public-path-prefixes=/api/health,/error"
})
@Import({SecurityConfig.class, CorsConfig.class, HealthResponseMapper.class})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @MockitoBean
    private BearerTokenAuthenticator bearerTokenAuthenticator;

    @MockitoBean
    private ApiMessageResolver apiMessageResolver;

    @Test
    void returnsOkWhenHealthy() throws Exception {
        when(healthService.currentHealth()).thenReturn(
                new HealthSnapshot(ComponentStatus.UP, ComponentStatus.UP, Instant.parse("2026-09-13T09:30:00Z"))
        );

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"))
                .andExpect(jsonPath("$.service").value("alphalens-backend"))
                .andExpect(jsonPath("$.timestamp").value("2026-09-13T09:30:00Z"));
    }

    @Test
    void returnsServiceUnavailableWhenDatabaseIsDown() throws Exception {
        when(healthService.currentHealth()).thenReturn(
                new HealthSnapshot(ComponentStatus.DOWN, ComponentStatus.DOWN, Instant.parse("2026-09-13T09:30:00Z"))
        );

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.database").value("DOWN"));
    }
}
