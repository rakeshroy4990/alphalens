package com.alphalens.auth.api;

public class AuthClientConfig {
    private String googleClientId;

    public AuthClientConfig() {
    }

    public AuthClientConfig(String googleClientId) {
        this.googleClientId = googleClientId == null ? "" : googleClientId;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }
}
