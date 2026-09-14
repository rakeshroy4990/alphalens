package io.alphalens.auth.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * Paths the JWT filter skips. Always include login/register/refresh/logout.
     * The consuming app adds its own public APIs (health, catalog, …).
     */
    private List<String> publicPathPrefixes = new ArrayList<>(List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/google-login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/change-password",
            "/error"
    ));

    private final Cookie cookie = new Cookie();

    public List<String> getPublicPathPrefixes() {
        return publicPathPrefixes;
    }

    public void setPublicPathPrefixes(List<String> publicPathPrefixes) {
        this.publicPathPrefixes = publicPathPrefixes == null ? new ArrayList<>() : new ArrayList<>(publicPathPrefixes);
    }

    public Cookie getCookie() {
        return cookie;
    }

    public static class Cookie {
        private boolean secure = false;
        private String sameSite = "Lax";
        private String domain = "";
        private boolean crossSiteDeployment = false;
        private long refreshMaxAgeSeconds = 2592000L;
        private long accessMaxAgeSeconds = 43200L;
        private String accessTokenName = "access_token";

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        public String getSameSite() {
            return sameSite;
        }

        public void setSameSite(String sameSite) {
            this.sameSite = sameSite;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public boolean isCrossSiteDeployment() {
            return crossSiteDeployment;
        }

        public void setCrossSiteDeployment(boolean crossSiteDeployment) {
            this.crossSiteDeployment = crossSiteDeployment;
        }

        public long getRefreshMaxAgeSeconds() {
            return refreshMaxAgeSeconds;
        }

        public void setRefreshMaxAgeSeconds(long refreshMaxAgeSeconds) {
            this.refreshMaxAgeSeconds = refreshMaxAgeSeconds;
        }

        public long getAccessMaxAgeSeconds() {
            return accessMaxAgeSeconds;
        }

        public void setAccessMaxAgeSeconds(long accessMaxAgeSeconds) {
            this.accessMaxAgeSeconds = accessMaxAgeSeconds;
        }

        public String getAccessTokenName() {
            return accessTokenName;
        }

        public void setAccessTokenName(String accessTokenName) {
            this.accessTokenName = accessTokenName;
        }
    }
}
