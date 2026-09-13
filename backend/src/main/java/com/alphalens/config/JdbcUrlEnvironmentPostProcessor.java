package com.alphalens.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalizes Dashboard-style Postgres URLs for Spring JDBC and Flyway.
 *
 * <p>Supports {@code postgresql://} / {@code postgres://}, {@code {{password}}}
 * substitution, {@code sslmode=require} for Supabase, and transaction-pooler (6543) flags.
 */
public class JdbcUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawUrl = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("spring.datasource.url")
        );
        if (rawUrl == null) {
            return;
        }
        if (!needsRewrite(rawUrl) && firstNonBlank(
                environment.getProperty("SUPABASE_URL"),
                environment.getProperty("app.supabase.url")
        ) == null) {
            return;
        }
        String password = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
                environment.getProperty("spring.datasource.password")
        );
        ResolvedJdbc resolved = resolve(rawUrl, password);

        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("SPRING_DATASOURCE_URL", resolved.url());
        overrides.put("spring.datasource.url", resolved.url());
        if (resolved.username() != null) {
            overrides.put("SPRING_DATASOURCE_USERNAME", resolved.username());
            overrides.put("spring.datasource.username", resolved.username());
        }
        if (resolved.password() != null) {
            overrides.put("SPRING_DATASOURCE_PASSWORD", resolved.password());
            overrides.put("spring.datasource.password", resolved.password());
        }
        if (resolved.transactionPooler()) {
            overrides.put("app.datasource.transaction-pooler", "true");
            overrides.put("app.datasource.prepare-threshold", "0");
            overrides.put("spring.flyway.postgresql.transactional-lock", "false");
        }
        String supabaseUrl = firstNonBlank(
                environment.getProperty("SUPABASE_URL"),
                environment.getProperty("app.supabase.url")
        );
        if (supabaseUrl != null) {
            overrides.put("SUPABASE_URL", supabaseUrl);
            overrides.put("app.supabase.url", supabaseUrl);
        }
        environment.getPropertySources().addFirst(new MapPropertySource("alphalensJdbcUrlNormalize", overrides));
        // SPRING_DATASOURCE_URL from the OS env binds to spring.datasource.url and can
        // beat a MapPropertySource. System properties rank higher, so Hikari/Flyway
        // see jdbc:postgresql:// instead of postgresql://.
        environment.getSystemProperties().put("SPRING_DATASOURCE_URL", resolved.url());
        environment.getSystemProperties().put("spring.datasource.url", resolved.url());
        if (resolved.username() != null) {
            environment.getSystemProperties().put("SPRING_DATASOURCE_USERNAME", resolved.username());
            environment.getSystemProperties().put("spring.datasource.username", resolved.username());
        }
        if (resolved.password() != null) {
            environment.getSystemProperties().put("SPRING_DATASOURCE_PASSWORD", resolved.password());
            environment.getSystemProperties().put("spring.datasource.password", resolved.password());
        }
    }

    static ResolvedJdbc resolve(String rawUrl, String passwordFromEnv) {
        String url = rawUrl.trim();
        if (url.contains("{{password}}")) {
            if (passwordFromEnv == null || passwordFromEnv.isBlank() || "{{password}}".equals(passwordFromEnv)) {
                throw new IllegalStateException(
                        "SPRING_DATASOURCE_URL contains {{password}} but SPRING_DATASOURCE_PASSWORD is empty."
                );
            }
            url = url.replace("{{password}}", passwordFromEnv);
        }
        url = toJdbcUrl(url);

        String username = null;
        String password = passwordFromEnv;
        boolean transactionPooler = url.contains(":6543/") || url.contains(":6543?");
        try {
            URI parsed = URI.create(url.substring("jdbc:".length()));
            String userInfo = parsed.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                int colon = userInfo.indexOf(':');
                if (colon < 0) {
                    username = userInfo;
                } else {
                    username = userInfo.substring(0, colon);
                    String embedded = userInfo.substring(colon + 1);
                    if (!embedded.isBlank()) {
                        password = embedded;
                    }
                }
            }
            url = rebuildJdbcUrlWithoutUserInfo(parsed, url);
        } catch (IllegalArgumentException ignored) {
            // Leave username/password from env when the URL cannot be parsed.
        }
        url = ensureSslForSupabase(url);
        if (transactionPooler && url.contains("supabase.com")) {
            url = url.replace(":6543/", ":5432/").replace(":6543?", ":5432?");
            transactionPooler = false;
        }
        return new ResolvedJdbc(url, username, password, transactionPooler);
    }

    static String toJdbcUrl(String url) {
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        if (url.startsWith("postgresql://")) {
            return "jdbc:" + url;
        }
        return url;
    }

    /**
     * The PostgreSQL JDBC driver treats {@code user:pass@host} as a hostname.
     * Credentials belong on {@code JdbcConnectionDetails}, not in the URL.
     */
    static String rebuildJdbcUrlWithoutUserInfo(URI parsed, String originalJdbcUrl) {
        if (parsed.getHost() == null) {
            return originalJdbcUrl;
        }
        StringBuilder rebuilt = new StringBuilder("jdbc:postgresql://");
        rebuilt.append(parsed.getHost());
        if (parsed.getPort() > 0) {
            rebuilt.append(':').append(parsed.getPort());
        }
        if (parsed.getPath() != null) {
            rebuilt.append(parsed.getPath());
        }
        if (parsed.getQuery() != null) {
            rebuilt.append('?').append(parsed.getQuery());
        }
        return rebuilt.toString();
    }

    static String ensureSslForSupabase(String jdbcUrl) {
        if (!jdbcUrl.contains("supabase.com") || jdbcUrl.contains("sslmode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
    }

    static boolean needsRewrite(String url) {
        String trimmed = url.trim();
        return trimmed.startsWith("postgres://")
                || trimmed.startsWith("postgresql://")
                || trimmed.contains("{{password}}")
                || trimmed.contains("supabase.com")
                || trimmed.contains(":6543/")
                || trimmed.contains(":6543?");
    }

    static String normalize(String url) {
        return toJdbcUrl(url.trim());
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    record ResolvedJdbc(String url, String username, String password, boolean transactionPooler) {
    }
}
