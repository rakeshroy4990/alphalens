package io.alphalens.auth.autoconfigure;

import com.alphalens.auth.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;

/**
 * Shared Security DSL so consuming apps do not copy cookie/JWT filter wiring.
 */
public final class AuthHttpSecurity {

    private AuthHttpSecurity() {
    }

    public static HttpSecurity statelessJwt(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthProperties properties) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                    if (corsConfigurationSource != null) {
                        cors.configurationSource(corsConfigurationSource);
                    }
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    for (String prefix : properties.getPublicPathPrefixes()) {
                        auth.requestMatchers(prefix, prefix + "/**").permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write(
                                    "{\"message\":\"Authentication required\",\"errorCode\":\"AUTH_REQUIRED\",\"code\":\"AUTH_REQUIRED\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write(
                                    "{\"message\":\"Forbidden\",\"errorCode\":\"ACCESS_DENIED\",\"code\":\"ACCESS_DENIED\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http;
    }
}
