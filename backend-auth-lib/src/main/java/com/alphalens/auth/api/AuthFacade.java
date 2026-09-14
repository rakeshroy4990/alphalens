package com.alphalens.auth.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Login/register/refresh contract. The consuming app implements this with its own store and token strategy.
 */
public interface AuthFacade {
    Optional<LoginResponse> login(String emailId, String password);

    default Optional<LoginResponse> loginWithGoogleAccessToken(String accessToken) {
        return Optional.empty();
    }

    default Optional<LoginResponse> loginWithGoogleIdToken(String idToken) {
        return Optional.empty();
    }

    Optional<RegisterResponse> register(RegisterRequest request);

    void changePassword(ChangePasswordRequest request);

    default Optional<RefreshTokenResponse> refresh(RefreshTokenRequest request) {
        return Optional.empty();
    }

    default boolean logout(LogoutRequest request) {
        return false;
    }

    default Optional<MeResponse> me(UUID userId) {
        return Optional.empty();
    }
}
