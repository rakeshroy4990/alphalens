# backend-auth-lib

Reusable Spring Boot auth library. Drop it into any backend, implement two interfaces, and you get login, register, Google login, refresh, logout, change-password, and `/api/auth/me` with httpOnly cookies.

The consuming app owns users, password hashing, JWT signing, and refresh-token storage. The library owns HTTP, cookies, and the JWT filter.

## Add the library

Local composite build (this repo):

```gradle
// settings.gradle
includeBuild('../backend-auth-lib')

// build.gradle
implementation 'com.alphalens:backend-auth-lib'
```

Or publish and depend on the Maven coordinates `com.alphalens:backend-auth-lib`.

## Implement in the app (required)

```java
@Service
public class AuthService implements AuthFacade {
    public Optional<LoginResponse> login(String email, String password) { /* verify + issue tokens */ }
    public Optional<RegisterResponse> register(RegisterRequest request) { /* persist user */ }
    public void changePassword(ChangePasswordRequest request) { /* verify old, store new */ }
    public Optional<RefreshTokenResponse> refresh(RefreshTokenRequest request) { /* rotate */ }
    public boolean logout(LogoutRequest request) { /* revoke refresh */ }
    public Optional<MeResponse> me(UUID userId) { /* profile */ }
}

@Component
public class AppBearerTokenAuthenticator implements BearerTokenAuthenticator {
    public Authentication authenticate(String accessToken) { /* parse JWT, load user */ }
}
```

That is the whole contract. `AuthController`, cookies, messages, and `JwtAuthenticationFilter` are auto-configured when those beans exist.

## Security

If the app does **not** declare a `SecurityFilterChain`, the library installs a default: CSRF off, stateless session, JWT filter, public `app.auth.public-path-prefixes`.

If the app needs extra public routes (this AlphaLens backend does), declare your own chain and reuse the helper:

```java
@Bean
SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        CorsConfigurationSource cors,
        JwtAuthenticationFilter jwtFilter,
        AuthProperties authProperties) throws Exception {
    authProperties.getPublicPathPrefixes().add("/api/health");
    return AuthHttpSecurity.statelessJwt(http, cors, jwtFilter, authProperties).build();
}
```

## Properties

```properties
app.auth.cookie.secure=false
app.auth.cookie.same-site=Lax
app.auth.cookie.cross-site-deployment=false
app.auth.public-path-prefixes=/api/auth/login,/api/auth/register,/api/auth/google-login,/api/auth/refresh,/api/auth/logout,/api/auth/change-password,/error
```

JWT secret, Google client id, and password policy stay in the app.

## What the library does not do

- User tables or JPA entities
- BCrypt / JWT signing
- Google token verification
- Vue or mobile UI
