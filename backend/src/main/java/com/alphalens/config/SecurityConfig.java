package com.alphalens.config;

import com.alphalens.auth.security.BearerTokenAuthenticator;
import com.alphalens.auth.security.JwtAuthenticationFilter;
import io.alphalens.auth.autoconfigure.AuthHttpSecurity;
import io.alphalens.auth.autoconfigure.AuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            BearerTokenAuthenticator bearerTokenAuthenticator,
            AuthProperties authProperties) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                bearerTokenAuthenticator,
                authProperties.getPublicPathPrefixes(),
                authProperties.getCookie().getAccessTokenName());
        return AuthHttpSecurity.statelessJwt(http, corsConfigurationSource, jwtAuthenticationFilter, authProperties)
                .build();
    }
}
