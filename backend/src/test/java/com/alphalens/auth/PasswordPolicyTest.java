package com.alphalens.auth;

import com.alphalens.auth.api.AuthApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void rejectsShortAndUnmixedPasswords() {
        PasswordPolicy policy = new PasswordPolicy(10, true);
        assertThatThrownBy(() -> policy.validateOrThrow("short"))
                .isInstanceOf(AuthApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", "AUTH_PASSWORD_POLICY");
        assertThatThrownBy(() -> policy.validateOrThrow("alllowercase1!"))
                .isInstanceOf(AuthApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", "AUTH_PASSWORD_POLICY");
        policy.validateOrThrow("CorrectHorse1!");
    }
}
