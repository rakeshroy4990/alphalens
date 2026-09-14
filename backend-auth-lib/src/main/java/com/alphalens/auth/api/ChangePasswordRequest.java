package com.alphalens.auth.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @JsonAlias({"EmailId", "email"})
    @NotBlank(message = "Email is required")
    private String emailId;

    @JsonAlias({"OldPassword"})
    @NotBlank(message = "Current password is required")
    private String oldPassword;

    @JsonAlias({"NewPassword"})
    @NotBlank(message = "New password is required")
    @Size(min = 10, message = "New password must be at least 10 characters")
    private String newPassword;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
