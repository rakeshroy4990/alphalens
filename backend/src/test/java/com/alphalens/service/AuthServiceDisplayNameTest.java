package com.alphalens.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceDisplayNameTest {

    @Test
    void derivesDisplayNameFromEmailLocalPart() {
        assertThat(AuthService.displayName("jane.doe@example.com")).isEqualTo("Jane Doe");
        assertThat(AuthService.displayName("")).isEqualTo("User");
    }
}
