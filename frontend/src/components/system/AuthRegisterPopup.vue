<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '../../stores/auth.store';
import { usePopupStore } from '../../stores/popup.store';

const auth = useAuthStore();
const popup = usePopupStore();

const canSubmit = computed(
  () =>
    auth.registerEmail.trim().length > 0 &&
    auth.registerPassword.length > 0 &&
    !auth.registerEmailError &&
    !auth.loading
);

async function submit() {
  try {
    await auth.register();
    auth.openLoginAfterRegister();
  } catch {
    // Form error is set on the auth store.
  }
}
</script>

<template>
  <div class="mx-auto flex w-full max-w-md flex-col gap-6">
    <div class="relative flex min-h-10 items-center justify-center">
      <h2 class="text-center text-2xl font-bold text-slate-900 sm:text-3xl">Create account</h2>
      <button
        class="absolute top-1/2 right-0 -translate-y-1/2 rounded-lg px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
        type="button"
        aria-label="Close"
        @click="popup.close()"
      >
        X
      </button>
    </div>
    <p class="text-center text-sm text-slate-600">
      Use a password with at least 10 characters, including uppercase, lowercase, a number, and a special character.
    </p>
    <form class="flex flex-col gap-4" @submit.prevent="submit">
      <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:gap-3">
        <label class="w-full shrink-0 text-base font-semibold text-slate-900 sm:w-28" for="auth-register-email">Email</label>
        <input
          id="auth-register-email"
          :value="auth.registerEmail"
          type="email"
          autocomplete="username"
          placeholder="Email"
          required
          class="min-w-0 w-full rounded-lg border border-slate-300 px-3 py-2.5 text-slate-800 outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-100 sm:flex-1"
          @input="auth.setRegisterEmail(($event.target as HTMLInputElement).value)"
        />
      </div>
      <p v-if="auth.registerEmailError" class="text-sm text-red-600 sm:ml-[7.75rem]">{{ auth.registerEmailError }}</p>
      <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:gap-3">
        <label class="w-full shrink-0 text-base font-semibold text-slate-900 sm:w-28" for="auth-register-password">Password</label>
        <input
          id="auth-register-password"
          :value="auth.registerPassword"
          type="password"
          autocomplete="new-password"
          placeholder="Password"
          required
          minlength="10"
          class="min-w-0 w-full rounded-lg border border-slate-300 px-3 py-2.5 text-slate-800 outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-100 sm:flex-1"
          @input="auth.setRegisterPassword(($event.target as HTMLInputElement).value)"
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
          Create account
        </button>
      </div>
    </form>
    <p class="text-center text-sm text-slate-600">
      Already registered?
      <button
        class="font-semibold text-sky-700 underline underline-offset-2 hover:text-sky-800"
        type="button"
        @click="auth.openLogin()"
      >
        Sign in
      </button>
    </p>
  </div>
</template>
