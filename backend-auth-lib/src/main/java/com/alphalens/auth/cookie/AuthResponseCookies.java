package com.alphalens.auth.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Auth {@link ResponseCookie} construction.
 * {@code SameSite=None} is never applied without {@code Secure=true}.
 * Refresh tokens are scoped to {@link #REFRESH_TOKEN_PATH}.
 */
public final class AuthResponseCookies {

    public static final String REFRESH_TOKEN_PATH = "/api/auth";

    private AuthResponseCookies() {
    }

    public static EffectiveCookiePolicy resolvePolicy(
            boolean crossSiteDeployment,
            boolean configSecure,
            String configSameSite) {
        String raw = configSameSite == null ? "" : configSameSite.trim();
        String sameSite;
        boolean secure;
        if (crossSiteDeployment) {
            sameSite = "None";
            secure = true;
        } else if ("None".equalsIgnoreCase(raw)) {
            sameSite = "None";
            secure = true;
        } else {
            sameSite = raw.isEmpty() ? "Lax" : raw;
            secure = configSecure;
        }
        if ("None".equalsIgnoreCase(sameSite) && !secure) {
            secure = true;
        }
        return new EffectiveCookiePolicy(sameSite, secure);
    }

    public record EffectiveCookiePolicy(String sameSite, boolean secure) {
    }

    public static ResponseCookie buildRefreshCookie(
            String name,
            String value,
            EffectiveCookiePolicy policy,
            String domain,
            long maxAgeSeconds) {
        return buildHttpOnlyCookie(name, value, policy, domain, REFRESH_TOKEN_PATH, maxAgeSeconds);
    }

    public static ResponseCookie buildAccessCookie(
            String name,
            String value,
            EffectiveCookiePolicy policy,
            String domain,
            String path,
            long maxAgeSeconds) {
        String p = StringUtils.hasText(path) ? path : "/";
        return buildHttpOnlyCookie(name, value, policy, domain, p, maxAgeSeconds);
    }

    private static ResponseCookie buildHttpOnlyCookie(
            String name,
            String value,
            EffectiveCookiePolicy policy,
            String domain,
            String path,
            long maxAgeSeconds) {
        boolean clearing = value == null || value.isEmpty();
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, clearing ? "" : value)
                .httpOnly(true)
                .secure(policy.secure())
                .sameSite(policy.sameSite())
                .path(path);
        if (StringUtils.hasText(domain)) {
            b.domain(domain.trim());
        }
        if (clearing) {
            b.maxAge(Duration.ZERO);
        } else {
            long sec = maxAgeSeconds > 0 ? maxAgeSeconds : 1;
            b.maxAge(Duration.ofSeconds(sec));
        }
        return b.build();
    }
}
