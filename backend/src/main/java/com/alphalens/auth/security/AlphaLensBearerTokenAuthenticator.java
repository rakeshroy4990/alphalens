package com.alphalens.auth.security;

import com.alphalens.auth.JwtService;
import com.alphalens.domain.UserAccount;
import com.alphalens.domain.UserStatus;
import com.alphalens.repository.UserAccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AlphaLensBearerTokenAuthenticator implements BearerTokenAuthenticator {
    private final JwtService jwtService;
    private final UserAccountRepository users;

    public AlphaLensBearerTokenAuthenticator(JwtService jwtService, UserAccountRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    public Authentication authenticate(String token) throws AuthTokenException {
        final Claims claims;
        try {
            claims = jwtService.parseAndValidate(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AuthTokenException("Invalid or expired token, Please login again.");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new AuthTokenException("Token subject is missing");
        }
        String tokenType = claims.get("tokenType", String.class);
        if ("refresh".equalsIgnoreCase(tokenType)) {
            throw new AuthTokenException("Refresh token cannot be used as access token");
        }
        if (claims.getAudience() == null || !claims.getAudience().contains("web")) {
            throw new AuthTokenException("Invalid token audience");
        }

        UUID userId;
        try {
            userId = UUID.fromString(subject.trim());
        } catch (IllegalArgumentException ex) {
            throw new AuthTokenException("Token subject is invalid");
        }

        UserAccount user = users.findById(userId).orElseThrow(() -> new AuthTokenException("User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthTokenException("Account is inactive");
        }
        Number tokenVersionClaim = claims.get("tokenVersion", Number.class);
        long tokenVersion = tokenVersionClaim == null ? 0L : tokenVersionClaim.longValue();
        if (tokenVersion != user.getTokenVersion()) {
            throw new AuthTokenException("Token version mismatch");
        }

        return new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
