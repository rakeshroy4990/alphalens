package com.alphalens.auth.security;

import org.springframework.security.core.Authentication;

public interface BearerTokenAuthenticator {
    Authentication authenticate(String token) throws AuthTokenException;
}
