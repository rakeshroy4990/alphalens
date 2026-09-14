const GIS_SCRIPT_SELECTOR = 'script[data-alphalens-gis="1"]';

export function googleOAuthClientId(): string {
  return (
    String(import.meta.env.VITE_GOOGLE_OAUTH_CLIENT_ID ?? '').trim() ||
    String(import.meta.env.VITE_GOOGLE_CLIENT_ID ?? '').trim()
  );
}

let cachedServerClientId: string | null = null;

export async function resolveGoogleOAuthClientId(
  fetchServerClientId: () => Promise<string>
): Promise<string> {
  const fromEnv = googleOAuthClientId();
  if (fromEnv) {
    return fromEnv;
  }
  if (cachedServerClientId !== null) {
    return cachedServerClientId;
  }
  cachedServerClientId = (await fetchServerClientId()).trim();
  return cachedServerClientId;
}

function googleOAuthScopeString(): string {
  return [
    'openid',
    'email',
    'profile',
    'https://www.googleapis.com/auth/userinfo.email',
    'https://www.googleapis.com/auth/userinfo.profile'
  ].join(' ');
}

export function loadGoogleIdentityScript(): Promise<void> {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('Google sign-in requires a browser.'));
  }
  if (window.google?.accounts?.oauth2) {
    return Promise.resolve();
  }
  const existing = document.querySelector(GIS_SCRIPT_SELECTOR);
  if (existing instanceof HTMLScriptElement) {
    return new Promise((resolve, reject) => {
      if (window.google?.accounts?.oauth2) {
        resolve();
        return;
      }
      existing.addEventListener('load', () => resolve(), { once: true });
      existing.addEventListener('error', () => reject(new Error('Failed to load Google Sign-In')), { once: true });
    });
  }
  return new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.dataset.alphalensGis = '1';
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Failed to load Google Sign-In'));
    document.head.appendChild(script);
  });
}

export function requestGoogleAccessToken(clientId: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const oauth2 = window.google?.accounts?.oauth2;
    if (!oauth2) {
      reject(new Error('Google Sign-In is not available.'));
      return;
    }
    const client = oauth2.initTokenClient({
      client_id: clientId,
      scope: googleOAuthScopeString(),
      callback: (resp) => {
        if (resp.error) {
          const detail = resp.error_description ? `${resp.error}: ${resp.error_description}` : resp.error;
          reject(new Error(detail || 'Google sign-in failed'));
          return;
        }
        const token = String(resp.access_token ?? '').trim();
        if (!token) {
          reject(new Error('Google sign-in was cancelled.'));
          return;
        }
        resolve(token);
      }
    });
    client.requestAccessToken();
  });
}
