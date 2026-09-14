import axios from 'axios';
import { resolveApiBaseUrl } from './apiBaseUrl';

const baseURL = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL);

export const apiClient = axios.create({
  baseURL,
  timeout: 10000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
});

type RetryConfig = { _retry?: boolean };

let refreshInFlight: Promise<boolean> | null = null;

function isAuthUrl(url: string): boolean {
  return (
    url.includes('/auth/login') ||
    url.includes('/auth/register') ||
    url.includes('/auth/refresh') ||
    url.includes('/auth/google-login') ||
    url.includes('/auth/logout') ||
    url.includes('/auth/me') ||
    url.includes('/auth/client-config')
  );
}

async function refreshSession(): Promise<boolean> {
  try {
    await apiClient.post('/auth/refresh', {});
    return true;
  } catch {
    return false;
  }
}

async function expireSessionAndOpenLogin(): Promise<void> {
  const { useAuthStore } = await import('../stores/auth.store');
  useAuthStore().expireSession();
  useAuthStore().openLogin('You are not logged in. Please login.');
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config as (typeof error.config & RetryConfig) | undefined;
    if (!original || error.response?.status !== 401) {
      return Promise.reject(error);
    }
    const url = String(original.url ?? '');
    if (isAuthUrl(url)) {
      return Promise.reject(error);
    }
    if (original._retry) {
      await expireSessionAndOpenLogin();
      return Promise.reject(error);
    }
    original._retry = true;
    if (!refreshInFlight) {
      refreshInFlight = refreshSession().finally(() => {
        refreshInFlight = null;
      });
    }
    const refreshed = await refreshInFlight;
    if (!refreshed) {
      await expireSessionAndOpenLogin();
      return Promise.reject(error);
    }
    return apiClient(original);
  }
);
