# Auth APIs

HTTP for these endpoints lives in the reusable `backend-auth-lib`. This backend implements `AuthFacade` (users, JWT, Google, refresh-token storage). See [backend-auth-lib/README.md](../../backend-auth-lib/README.md) to plug the same library into another project.

JSON is camelCase. Access and refresh tokens are set as **httpOnly** cookies (`access_token`, `refresh_token`). The Vue app must not persist tokens in localStorage. `withCredentials` is required.

Passwords: at least 10 characters, with uppercase, lowercase, a number, and a special character.

| Method | Path | Auth | Notes |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Public | Body `{ email, password }`. Does not return a password. |
| POST | `/api/auth/login` | Public | Body `{ emailId, password }`. Sets cookies. |
| GET | `/api/auth/client-config` | Public | `{ googleClientId }` for the GIS button. Empty when unset. |
| POST | `/api/auth/google-login` | Public | Body `{ idToken }` or `{ accessToken }`. |
| GET | `/api/auth/me` | Cookie or Bearer | Current account. |
| POST | `/api/auth/refresh` | Refresh cookie | Rotates refresh token. |
| POST | `/api/auth/logout` | Public | Revokes refresh token; clears cookies. |
| POST | `/api/auth/change-password` | Public | Body `{ emailId, oldPassword, newPassword }`. Bumps token version. |

## Success envelope

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "…",
    "refreshToken": "…",
    "tokenType": "Bearer",
    "expiresInSeconds": 43200,
    "userId": "…",
    "email": "analyst@example.com",
    "displayName": "Analyst",
    "authProvider": "EMAIL",
    "status": "ACTIVE"
  },
  "timestamp": "2026-09-14T09:00:00Z"
}
```

## Error envelope

```json
{
  "success": false,
  "message": "Invalid email or password",
  "errorCode": "AUTH_INVALID_CREDENTIALS",
  "timestamp": "2026-09-14T09:00:00Z"
}
```

Workspace APIs (watchlists, portfolio, alerts, saved screens, user algorithms, coverage, broker, orders, backtests) require a valid access cookie or `Authorization: Bearer`. Research APIs (`/api/health`, `/api/instruments/**`, `/api/screener`, `/api/research/**`) stay public.

Google OAuth: set `APP_GOOGLE_OAUTH_WEB_CLIENT_ID` (public Web client id) on the backend. `GET /api/auth/client-config` exposes it to Vue. Optional override: `VITE_GOOGLE_OAUTH_CLIENT_ID`. The UI uses Google Identity Services for an **access token**, then `POST /api/auth/google-login`. Never put `api_secret` in Vue.

On the OAuth **Web application** client, Authorized JavaScript origins must include every URL you open in the browser (scheme + host + port, no path):

- `http://localhost:5173`
- `http://127.0.0.1:5173` (only if you open the UI that way)
- production hosts (Firebase / Cloud Run) as `https://…` with no trailing slash

`localhost` and `127.0.0.1` are different origins. Local dev uses `http://localhost:5173`. An `origin_mismatch` error means the address bar origin is missing from that list. Console: [Google Auth Platform → Clients](https://console.cloud.google.com/auth/clients).
