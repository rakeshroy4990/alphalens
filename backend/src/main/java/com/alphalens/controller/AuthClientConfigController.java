package com.alphalens.controller;

import com.alphalens.auth.api.ApiResponse;
import com.alphalens.auth.api.AuthClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthClientConfigController {

    private final String googleWebClientId;

    public AuthClientConfigController(
            @Value("${app.google.oauth.web-client-id:}") String googleWebClientId) {
        this.googleWebClientId = googleWebClientId == null ? "" : googleWebClientId.trim();
    }

    @GetMapping(value = "/client-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AuthClientConfig>> clientConfig() {
        return ResponseEntity.ok(ApiResponse.success("ok", new AuthClientConfig(googleWebClientId)));
    }
}
