package com.alphalens.config;

import com.alphalens.auth.i18n.ApiMessageResolver;
import com.alphalens.auth.security.BearerTokenAuthenticator;
import com.alphalens.controller.HealthController;
import com.alphalens.mapper.HealthResponseMapper;
import com.alphalens.service.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthController.class, properties = {
        "spring.application.name=alphalens-backend",
        "app.cors.allowed-origin-patterns=https://alphalens-a3cce.web.app,https://*.web.app,https://*.run.app",
        "app.auth.public-path-prefixes=/api/health,/error"
})
@Import({SecurityConfig.class, CorsConfig.class, HealthResponseMapper.class})
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @MockitoBean
    private BearerTokenAuthenticator bearerTokenAuthenticator;

    @MockitoBean
    private ApiMessageResolver apiMessageResolver;

    @Test
    void credentialedPreflightFromFirebaseEchoesRequestedHeaders() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "https://alphalens-a3cce.web.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://alphalens-a3cce.web.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Headers", allOf(
                        containsString("content-type"),
                        not("*"))))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }
}
