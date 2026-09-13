package com.alphalens.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dashboard {@code postgresql://} URLs are not valid Hikari/Flyway JDBC URLs.
 * This bean wins over {@code SPRING_DATASOURCE_URL} env binding.
 */
@Configuration
public class JdbcConnectionDetailsConfig {

    @Bean
    @ConditionalOnProperty("SPRING_DATASOURCE_URL")
    @ConditionalOnMissingBean(JdbcConnectionDetails.class)
    JdbcConnectionDetails alphalensJdbcConnectionDetails(
            @Value("${SPRING_DATASOURCE_URL}") String url,
            @Value("${SPRING_DATASOURCE_USERNAME:${spring.datasource.username:}}") String username,
            @Value("${SPRING_DATASOURCE_PASSWORD:${spring.datasource.password:}}") String password) {
        JdbcUrlEnvironmentPostProcessor.ResolvedJdbc resolved = JdbcUrlEnvironmentPostProcessor.resolve(url, password);
        String user = resolved.username() != null ? resolved.username() : username;
        String pass = resolved.password() != null ? resolved.password() : password;
        return new JdbcConnectionDetails() {
            @Override
            public String getUsername() {
                return user;
            }

            @Override
            public String getPassword() {
                return pass == null ? "" : pass;
            }

            @Override
            public String getJdbcUrl() {
                return resolved.url();
            }
        };
    }
}
