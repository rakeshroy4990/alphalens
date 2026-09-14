<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import GoogleMark from '../primitives/GoogleMark.vue';
import { useAuthStore } from '../../stores/auth.store';
import { usePopupStore } from '../../stores/popup.store';

const auth = useAuthStore();
const popup = usePopupStore();
const router = useRouter();

const canSubmit = computed(
  () =>
    auth.identity.trim().length > 0 &&
    auth.password.length > 0 &&
    !auth.emailError &&
    !auth.loading
);

async function afterLogin() {
  const redirect = auth.consumePendingRedirect();
  popup.close();
  if (redirect) {
    await router.replace(redirect);
  }
}

async function submit() {
  try {
    await auth.login();
    await afterLogin();
  } catch {
    // Form error is set on the auth store.
  }
}

async function signInWithGoogle() {
  try {
    await auth.loginWithGoogle();
    await afterLogin();
  } catch {
    // Form error is set on the auth store.
  }
}
</script>

<template>
  <div class="mx-auto flex w-full max-w-md flex-col gap-6">
    <div class="relative flex min-h-10 items-center justify-center">
      <h2 class="text-center text-2xl font-bold text-slate-900 sm:text-3xl">Login</h2>
      <button
        class="absolute top-1/2 right-0 -translate-y-1/2 rounded-lg px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
        type="button"
        aria-label="Close"
        @click="popup.close()"
      >
        X
      </button>
    </div>
    <p v-if="auth.loginInfoMessage" class="text-center text-sm font-semibold text-sky-700">
      {{ auth.loginInfoMessage }}
    </p>
    <form class="flex flex-col gap-4" @submit.prevent="submit">
      <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:gap-3">
        <label class="w-full shrink-0 text-base font-semibold text-slate-900 sm:w-28" for="auth-login-email">Email</label>
        <input
          id="auth-login-email"
          :value="auth.identity"
          type="email"
          autocomplete="username"
          placeholder="Email"
          required
          class="min-w-0 w-full rounded-lg border border-slate-300 px-3 py-2.5 text-slate-800 outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-100 sm:flex-1"
          @input="auth.setIdentity(($event.target as HTMLInputElement).value)"
        />
      </div>
      <p v-if="auth.emailError" class="text-sm text-red-600 sm:ml-[7.75rem]">{{ auth.emailError }}</p>
      <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:gap-3">
        <label class="w-full shrink-0 text-base font-semibold text-slate-900 sm:w-28" for="auth-login-password">Password</label>
        <input
          id="auth-login-password"
          :value="auth.password"
          type="password"
          autocomplete="current-password"
          placeholder="Password"
          required
          class="min-w-0 w-full rounded-lg border border-slate-300 px-3 py-2.5 text-slate-800 outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-100 sm:flex-1"
          @input="auth.setPassword(($event.target as HTMLInputElement).value)"
        />
      </div>
      <p v-if="auth.error" class="text-center text-sm text-red-600">{{ auth.error }}</p>
      <div class="flex flex-wrap items-center justify-center gap-3">
        <button
          class="inline-flex h-12 min-w-40 items-center justify-center rounded-xl border border-sky-600 bg-white px-6 text-base font-semibold text-sky-700 hover:bg-sky-50"
          type="button"
          @click="popup.close()"
        >
          Cancel
        </button>
        <button
          class="inline-flex h-12 min-w-40 items-center justify-center rounded-xl bg-sky-600 px-6 text-base font-semibold text-white hover:bg-sky-700 disabled:cursor-not-allowed disabled:border disabled:border-slate-300 disabled:bg-slate-200 disabled:text-slate-500"
          type="submit"
          :disabled="!canSubmit"
        >
          Login
        </button>
      </div>
    </form>
    <div class="flex flex-col items-center gap-3">
      <p class="w-full text-center text-sm font-semibold text-slate-500">or</p>
      <button
        class="inline-flex h-12 w-full max-w-xs items-center justify-center gap-3 rounded-xl border border-sky-600 bg-white px-6 text-base font-semibold text-sky-700 hover:bg-sky-50 disabled:opacity-60"
        type="button"
        :disabled="auth.loading"
        @click="signInWithGoogle"
      >
        <GoogleMark />
        Sign In With Google
      </button>
    </div>
    <p class="text-center text-sm text-slate-600">
      No account?
      <button
        class="font-semibold text-sky-700 underline underline-offset-2 hover:text-sky-800"
        type="button"
        @click="auth.openRegister()"
      >
        Create one
      </button>
    </p>
  </div>
</template>
