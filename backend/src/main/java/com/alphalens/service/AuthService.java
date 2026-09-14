package com.alphalens.service;

import com.alphalens.auth.JwtService;
import com.alphalens.auth.PasswordPolicy;
import com.alphalens.auth.api.AuthApiException;
import com.alphalens.auth.api.AuthFacade;
import com.alphalens.auth.api.ChangePasswordRequest;
import com.alphalens.auth.api.LoginResponse;
import com.alphalens.auth.api.LogoutRequest;
import com.alphalens.auth.api.MeResponse;
import com.alphalens.auth.api.RefreshTokenRequest;
import com.alphalens.auth.api.RefreshTokenResponse;
import com.alphalens.auth.api.RegisterRequest;
import com.alphalens.auth.api.RegisterResponse;
import com.alphalens.auth.google.GoogleIdTokenVerifier;
import com.alphalens.domain.AuthProvider;
import com.alphalens.domain.RefreshToken;
import com.alphalens.domain.UserAccount;
import com.alphalens.domain.UserStatus;
import com.alphalens.repository.RefreshTokenRepository;
import com.alphalens.repository.UserAccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService implements AuthFacade {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final RestClient GOOGLE_HTTP = RestClient.builder().build();
    private static final String WEB_AUDIENCE = "web";
    private static final String BROWSER_DEVICE = "browser";

    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public AuthService(
            UserAccountRepository users,
            RefreshTokenRepository refreshTokens,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy,
            GoogleIdTokenVerifier googleIdTokenVerifier) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
    }

    @Override
    @Transactional
    public Optional<LoginResponse> login(String emailId, String password) {
        String identity = emailId == null ? "" : emailId.trim();
        if (identity.isEmpty() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        Optional<UserAccount> user = users.findByEmailIgnoreCase(identity);
        if (user.isEmpty()) {
            log.warn("login_attempt status=fail reason=invalid_credentials");
            return Optional.empty();
        }
        UserAccount account = user.get();
        assertEligibleForSignIn(account);
        String hash = account.getPasswordHash();
        if (hash == null || hash.isBlank() || !passwordEncoder.matches(password, hash)) {
            log.warn("login_attempt status=fail reason=invalid_credentials userId={}", account.getId());
            return Optional.empty();
        }
        return Optional.of(completeLoginWithTokens(account));
    }

    @Override
    @Transactional
    public Optional<LoginResponse> loginWithGoogleIdToken(String idToken) {
        if (idToken == null || idToken.isBlank() || !googleIdTokenVerifier.isConfigured()) {
            return Optional.empty();
        }
        return completeGoogleLogin(
                googleIdTokenVerifier.verify(idToken.trim()).map(GoogleIdTokenVerifier.VerifiedGoogleProfile::email));
    }

    @Override
    @Transactional
    public Optional<LoginResponse> loginWithGoogleAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }
        return completeGoogleLogin(fetchVerifiedGoogleEmail(accessToken.trim()));
    }

    private Optional<LoginResponse> completeGoogleLogin(Optional<String> emailOpt) {
        if (emailOpt.isEmpty()) {
            return Optional.empty();
        }
        String email = emailOpt.get().trim().toLowerCase(Locale.ROOT);
        UserAccount account = users.findByEmailIgnoreCase(email).orElse(null);
        if (account == null) {
            try {
                account = users.save(new UserAccount(email, null, AuthProvider.GOOGLE));
                log.info("Created user from Google sign-in email={}", email);
            } catch (DataIntegrityViolationException ex) {
                account = users.findByEmailIgnoreCase(email).orElse(null);
                if (account == null) {
                    log.warn("Google sign-in duplicate key but user not found email={}", email);
                    return Optional.empty();
                }
            }
        }
        assertEligibleForSignIn(account);
        return Optional.of(completeLoginWithTokens(account));
    }

    private Optional<String> fetchVerifiedGoogleEmail(String googleAccessToken) {
        try {
            Map<String, Object> body = GOOGLE_HTTP
                    .get()
                    .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + googleAccessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (body == null) {
                return Optional.empty();
            }
            Object rawEmail = body.get("email");
            String email = rawEmail == null ? "" : String.valueOf(rawEmail).trim().toLowerCase(Locale.ROOT);
            if (email.isEmpty()) {
                return Optional.empty();
            }
            if (!isGoogleEmailClaimVerified(body)) {
                log.warn("Google login rejected: email not verified");
                return Optional.empty();
            }
            return Optional.of(email);
        } catch (RestClientException ex) {
            log.warn("Google userinfo request failed: {}", ex.toString());
            return Optional.empty();
        }
    }

    private static boolean isGoogleEmailClaimVerified(Map<String, Object> body) {
        Object verified = body.get("email_verified");
        if (verified == null) {
            verified = body.get("verified_email");
        }
        if (verified instanceof Boolean b) {
            return b;
        }
        if (verified != null) {
            String s = String.valueOf(verified).trim();
            if ("false".equalsIgnoreCase(s)) {
                return false;
            }
            if ("true".equalsIgnoreCase(s)) {
                return true;
            }
        }
        return true;
    }

    private void assertEligibleForSignIn(UserAccount account) {
        if (account.getStatus() != UserStatus.ACTIVE) {
            log.info("Login blocked deactivated userId={} email={}", account.getId(), account.getEmail());
            throw new AuthApiException(
                    "Your account has been deactivated. You cannot sign in until an administrator reactivates your account.",
                    "AUTH_ACCOUNT_DEACTIVATED");
        }
    }

    private LoginResponse completeLoginWithTokens(UserAccount account) {
        String subject = account.getId().toString();
        String accessToken = jwtService.generateAccessToken(subject, WEB_AUDIENCE, account.getTokenVersion());
        String refreshToken = jwtService.generateRefreshToken(
                subject, WEB_AUDIENCE, BROWSER_DEVICE, account.getTokenVersion());
        persistRefreshToken(account.getId(), refreshToken, BROWSER_DEVICE);

        LoginResponse response = new LoginResponse(accessToken, jwtService.getAccessExpirationSeconds());
        response.setRefreshToken(refreshToken);
        response.setRefreshExpiresInSeconds(jwtService.getRefreshExpirationSeconds());
        response.setUserId(subject);
        response.setEmail(account.getEmail());
        response.setDisplayName(displayName(account.getEmail()));
        response.setAuthProvider(account.getAuthProvider().name());
        response.setStatus(account.getStatus().name());
        return response;
    }

    @Override
    @Transactional
    public Optional<RegisterResponse> register(RegisterRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase(Locale.ROOT);
        String rawPassword = request.getPassword() == null ? "" : request.getPassword();
        if (email.isEmpty() || rawPassword.isBlank()) {
            throw new AuthApiException("Email and password are required.", "AUTH_VALIDATION_FAILED");
        }
        passwordPolicy.validateOrThrow(rawPassword);
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new AuthApiException("An account already exists for this email address.", "AUTH_ACCOUNT_EXISTS");
        }
        UserAccount saved;
        try {
            saved = users.save(new UserAccount(email, passwordEncoder.encode(rawPassword), AuthProvider.EMAIL));
        } catch (DataIntegrityViolationException ex) {
            throw new AuthApiException("An account already exists for this email address.", "AUTH_ACCOUNT_EXISTS");
        }
        return Optional.of(new RegisterResponse(
                saved.getId().toString(),
                saved.getEmail(),
                displayName(saved.getEmail()),
                saved.getAuthProvider().name(),
                saved.getStatus().name()));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (request == null) {
            throw new AuthApiException("Unable to change password right now.", "AUTH_CHANGE_PASSWORD_FAILED");
        }
        String emailRaw = request.getEmailId() == null ? "" : request.getEmailId().trim();
        String oldPassword = request.getOldPassword() == null ? "" : request.getOldPassword();
        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword();
        if (emailRaw.isEmpty() || oldPassword.isBlank() || newPassword.isBlank()) {
            throw new AuthApiException("Email, current password, and new password are required.", "AUTH_VALIDATION_FAILED");
        }
        if (oldPassword.equals(newPassword)) {
            throw new AuthApiException(
                    "New password must be different from your current password.",
                    "AUTH_PASSWORD_UNCHANGED");
        }
        passwordPolicy.validateOrThrow(newPassword);
        UserAccount account = users.findByEmailIgnoreCase(emailRaw)
                .orElseThrow(() -> new AuthApiException("No account found for this email address.", "AUTH_USER_NOT_FOUND"));
        if (account.getStatus() != UserStatus.ACTIVE) {
            throw new AuthApiException(
                    "Your account has been deactivated. You cannot sign in until an administrator reactivates your account.",
                    "AUTH_ACCOUNT_DEACTIVATED");
        }
        String hash = account.getPasswordHash();
        if (hash == null || hash.isBlank() || !passwordEncoder.matches(oldPassword, hash)) {
            throw new AuthApiException("Current password is incorrect.", "AUTH_INVALID_OLD_PASSWORD");
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setTokenVersion(account.getTokenVersion() + 1L);
        users.save(account);
        log.info("Password changed userId={}", account.getId());
    }

    @Override
    @Transactional
    public Optional<RefreshTokenResponse> refresh(RefreshTokenRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        String supplied = request.getRefreshToken() == null ? "" : request.getRefreshToken().trim();
        if (supplied.isEmpty()) {
            return Optional.empty();
        }
        Optional<RefreshToken> stored = refreshTokens.findByTokenHash(hashToken(supplied));
        if (stored.isEmpty()) {
            log.warn("Refresh token replay or invalid token attempt");
            return Optional.empty();
        }
        RefreshToken tokenEntity = stored.get();
        if (tokenEntity.getExpiresAt() == null || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            refreshTokens.delete(tokenEntity);
            return Optional.empty();
        }
        String deviceId = request.getDeviceId() == null ? "" : request.getDeviceId().trim();
        if (!deviceId.isEmpty() && !deviceId.equals(tokenEntity.getDeviceId())) {
            log.warn("Refresh token device mismatch userId={}", tokenEntity.getUserId());
            return Optional.empty();
        }
        final Claims refreshClaims;
        try {
            refreshClaims = jwtService.parseAndValidate(supplied);
        } catch (JwtException | IllegalArgumentException ex) {
            refreshTokens.delete(tokenEntity);
            return Optional.empty();
        }
        if (!"refresh".equalsIgnoreCase(refreshClaims.get("tokenType", String.class))) {
            return Optional.empty();
        }
        if (refreshClaims.getAudience() == null || !refreshClaims.getAudience().contains(WEB_AUDIENCE)) {
            return Optional.empty();
        }
        if (refreshClaims.getSubject() == null || !refreshClaims.getSubject().equals(tokenEntity.getUserId().toString())) {
            return Optional.empty();
        }
        Optional<UserAccount> userOptional = users.findById(tokenEntity.getUserId());
        if (userOptional.isEmpty() || userOptional.get().getStatus() != UserStatus.ACTIVE) {
            return Optional.empty();
        }
        UserAccount user = userOptional.get();
        Number tokenVersionClaim = refreshClaims.get("tokenVersion", Number.class);
        long tokenVersion = tokenVersionClaim == null ? 0L : tokenVersionClaim.longValue();
        if (tokenVersion != user.getTokenVersion()) {
            return Optional.empty();
        }
        String newAccessToken = jwtService.generateAccessToken(
                user.getId().toString(), WEB_AUDIENCE, user.getTokenVersion());
        String newRefreshToken = jwtService.generateRefreshToken(
                user.getId().toString(), WEB_AUDIENCE, tokenEntity.getDeviceId(), user.getTokenVersion());
        refreshTokens.delete(tokenEntity);
        persistRefreshToken(user.getId(), newRefreshToken, tokenEntity.getDeviceId());
        return Optional.of(new RefreshTokenResponse(
                newAccessToken,
                jwtService.getAccessExpirationSeconds(),
                newRefreshToken,
                jwtService.getRefreshExpirationSeconds()));
    }

    @Override
    @Transactional
    public boolean logout(LogoutRequest request) {
        if (request == null) {
            return true;
        }
        String supplied = request.getRefreshToken() == null ? "" : request.getRefreshToken().trim();
        if (supplied.isEmpty()) {
            return true;
        }
        refreshTokens.findByTokenHash(hashToken(supplied)).ifPresent(refreshTokens::delete);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MeResponse> me(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return users.findById(userId).map(account -> new MeResponse(
                account.getId().toString(),
                account.getEmail(),
                displayName(account.getEmail()),
                account.getAuthProvider().name(),
                account.getStatus().name()));
    }

    private void persistRefreshToken(UUID userId, String token, String deviceId) {
        Instant now = Instant.now();
        refreshTokens.save(new RefreshToken(
                userId,
                hashToken(token),
                deviceId,
                now,
                now.plusSeconds(jwtService.getRefreshExpirationSeconds())));
    }

    static String displayName(String email) {
        String normalized = email == null ? "" : email.trim();
        if (normalized.isBlank()) {
            return "User";
        }
        String localPart = normalized.contains("@") ? normalized.substring(0, normalized.indexOf('@')) : normalized;
        String[] parts = localPart.split("[._-]+");
        StringBuilder builder = new StringBuilder();
        for (String raw : parts) {
            String token = raw == null ? "" : raw.trim();
            if (token.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(token.charAt(0)));
            if (token.length() > 1) {
                builder.append(token.substring(1));
            }
        }
        return builder.isEmpty() ? "User" : builder.toString();
    }

    static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
