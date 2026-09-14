package com.alphalens.auth.api;

import com.fasterxml.jackson.annotation.JsonAlias;

public class GoogleLoginRequest {
    @JsonAlias({"AccessToken"})
    private String accessToken;

    @JsonAlias({"IdToken"})
    private String idToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public boolean hasIdToken() {
        return idToken != null && !idToken.isBlank();
    }

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isBlank();
    }
}
