import { computed, ref } from 'vue';
import { defineStore } from 'pinia';

export type AuthPopupPage = 'login-popup' | 'register-popup';

export const usePopupStore = defineStore('popup', () => {
  const pageId = ref<AuthPopupPage | null>(null);

  const isOpen = computed(() => pageId.value !== null);
  const isLogin = computed(() => pageId.value === 'login-popup');
  const isRegister = computed(() => pageId.value === 'register-popup');

  function openLogin() {
    pageId.value = 'login-popup';
  }

  function openRegister() {
    pageId.value = 'register-popup';
  }

  function close() {
    pageId.value = null;
  }

  return { pageId, isOpen, isLogin, isRegister, openLogin, openRegister, close };
});
