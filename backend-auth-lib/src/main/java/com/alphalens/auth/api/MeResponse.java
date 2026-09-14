package com.alphalens.auth.api;

public class MeResponse {
    private String userId;
    private String email;
    private String displayName;
    private String authProvider;
    private String status;

    public MeResponse() {
    }

    public MeResponse(String userId, String email, String displayName, String authProvider, String status) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.authProvider = authProvider;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
