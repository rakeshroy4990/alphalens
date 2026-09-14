import type { AuthUser } from '../types/auth';
import { apiClient } from './apiClient';
import { loadGoogleIdentityScript, requestGoogleAccessToken, resolveGoogleOAuthClientId } from './googleIdentity';

interface AuthEnvelope<T> {
  success?: boolean;
  Success?: boolean;
  message?: string;
  Message?: string;
  errorCode?: string;
  ErrorCode?: string;
  data?: T;
  Data?: T;
}

function unwrap<T>(payload: AuthEnvelope<T> | T): T {
  if (payload && typeof payload === 'object' && ('data' in payload || 'Data' in payload)) {
    const envelope = payload as AuthEnvelope<T>;
    return (envelope.data ?? envelope.Data) as T;
  }
  return payload as T;
}

export async function registerAccount(email: string, password: string): Promise<AuthUser> {
  const { data } = await apiClient.post<AuthEnvelope<AuthUser>>('/auth/register', { email, password });
  return unwrap(data);
}

export async function loginWithPassword(email: string, password: string): Promise<AuthUser> {
  const { data } = await apiClient.post<AuthEnvelope<AuthUser>>('/auth/login', {
    emailId: email,
    EmailId: email,
    password,
    Password: password
  });
  return unwrap(data);
}

export async function loginWithGoogleAccessToken(accessToken: string): Promise<AuthUser> {
  const { data } = await apiClient.post<AuthEnvelope<AuthUser>>('/auth/google-login', {
    accessToken,
    AccessToken: accessToken
  });
  return unwrap(data);
}

async function fetchServerGoogleClientId(): Promise<string> {
  try {
    const { data } = await apiClient.get<AuthEnvelope<{ googleClientId?: string }>>('/auth/client-config');
    return String(unwrap(data)?.googleClientId ?? '').trim();
  } catch {
    return '';
  }
}

export async function requestGoogleSignInAccessToken(): Promise<string> {
  const clientId = await resolveGoogleOAuthClientId(fetchServerGoogleClientId);
  if (!clientId) {
    throw new Error(
      'Google sign-in is not configured. Set APP_GOOGLE_OAUTH_WEB_CLIENT_ID on the server (public Web client id).'
    );
  }
  await loadGoogleIdentityScript();
  return requestGoogleAccessToken(clientId);
}

export async function fetchSession(): Promise<AuthUser | null> {
  try {
    const { data } = await apiClient.get<AuthEnvelope<AuthUser>>('/auth/me');
    return unwrap(data);
  } catch {
    return null;
  }
}

export async function logoutSession(): Promise<void> {
  try {
    await apiClient.post('/auth/logout', {});
  } catch {
    // Cookies are cleared server-side when the request reaches /auth/logout.
  }
}
