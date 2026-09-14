import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import {
  fetchSession,
  loginWithGoogleAccessToken,
  loginWithPassword,
  logoutSession,
  registerAccount,
  requestGoogleSignInAccessToken
} from '../services/auth.service';
import type { AuthUser } from '../types/auth';
import { usePopupStore } from './popup.store';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateEmail(value: string): string {
  const trimmed = value.trim();
  if (!trimmed) {
    return '';
  }
  return EMAIL_PATTERN.test(trimmed) ? '' : 'Please enter a valid email address.';
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthUser | null>(null);
  const bootstrapped = ref(false);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const identity = ref('');
  const password = ref('');
  const emailError = ref('');
  const loginInfoMessage = ref('');
  const registerEmail = ref('');
  const registerPassword = ref('');
  const registerEmailError = ref('');
  const pendingRedirect = ref<string | null>(null);

  const isAuthenticated = computed(() => user.value !== null);

  function popup() {
    return usePopupStore();
  }

  function toErrorMessage(cause: unknown, fallback: string): string {
    if (cause && typeof cause === 'object' && 'response' in cause) {
      const response = (cause as { response?: { data?: { message?: string; Message?: string } } }).response;
      return response?.data?.message || response?.data?.Message || fallback;
    }
    if (cause instanceof Error && cause.message) {
      return cause.message;
    }
    return fallback;
  }

  async function bootstrap() {
    if (bootstrapped.value) {
      return;
    }
    user.value = await fetchSession();
    bootstrapped.value = true;
  }

  function setIdentity(value: string) {
    identity.value = value;
    emailError.value = validateEmail(value);
    error.value = null;
  }

  function setPassword(value: string) {
    password.value = value;
    error.value = null;
  }

  function setRegisterEmail(value: string) {
    registerEmail.value = value;
    registerEmailError.value = validateEmail(value);
    error.value = null;
  }

  function setRegisterPassword(value: string) {
    registerPassword.value = value;
    error.value = null;
  }

  function openLogin(infoMessage = '') {
    error.value = null;
    emailError.value = '';
    loginInfoMessage.value = infoMessage;
    popup().openLogin();
  }

  function openRegister() {
    error.value = null;
    registerEmailError.value = '';
    loginInfoMessage.value = '';
    popup().openRegister();
  }

  function openLoginAfterRegister() {
    identity.value = registerEmail.value.trim();
    password.value = '';
    emailError.value = validateEmail(identity.value);
    openLogin('You have successfully registered. Please login to continue.');
  }

  function requireAuth(redirectPath: string, infoMessage = 'Please sign in to continue.') {
    pendingRedirect.value = redirectPath;
    openLogin(infoMessage);
  }

  function consumePendingRedirect(): string | null {
    const value = pendingRedirect.value;
    pendingRedirect.value = null;
    return value;
  }

  function expireSession() {
    user.value = null;
    bootstrapped.value = true;
  }

  async function login() {
    const email = identity.value.trim();
    if (!email || !password.value) {
      error.value = 'Email and password are required.';
      throw new Error(error.value);
    }
    if (emailError.value) {
      throw new Error(emailError.value);
    }
    loading.value = true;
    error.value = null;
    try {
      user.value = await loginWithPassword(email, password.value);
      bootstrapped.value = true;
      password.value = '';
    } catch (cause) {
      error.value = toErrorMessage(cause, 'Invalid email or password');
      throw cause;
    } finally {
      loading.value = false;
    }
  }

  async function register() {
    const email = registerEmail.value.trim();
    if (!email || !registerPassword.value) {
      error.value = 'Email and password are required.';
      throw new Error(error.value);
    }
    if (registerEmailError.value) {
      throw new Error(registerEmailError.value);
    }
    loading.value = true;
    error.value = null;
    try {
      await registerAccount(email, registerPassword.value);
      registerPassword.value = '';
    } catch (cause) {
      error.value = toErrorMessage(cause, 'Unable to create this account');
      throw cause;
    } finally {
      loading.value = false;
    }
  }

  async function loginWithGoogle() {
    loading.value = true;
    error.value = null;
    try {
      const accessToken = await requestGoogleSignInAccessToken();
      user.value = await loginWithGoogleAccessToken(accessToken);
      bootstrapped.value = true;
    } catch (cause) {
      const message = toErrorMessage(cause, 'Google sign-in failed');
      if (!/cancel|popup|closed|denied|access_denied/i.test(message)) {
        error.value = message;
      }
      throw cause;
    } finally {
      loading.value = false;
    }
  }

  async function logout() {
    await logoutSession();
    user.value = null;
    bootstrapped.value = true;
    popup().close();
  }

  return {
    user,
    bootstrapped,
    loading,
    error,
    identity,
    password,
    emailError,
    loginInfoMessage,
    registerEmail,
    registerPassword,
    registerEmailError,
    pendingRedirect,
    isAuthenticated,
    bootstrap,
    setIdentity,
    setPassword,
    setRegisterEmail,
    setRegisterPassword,
    openLogin,
    openRegister,
    openLoginAfterRegister,
    requireAuth,
    consumePendingRedirect,
    expireSession,
    login,
    register,
    loginWithGoogle,
    logout
  };
});
