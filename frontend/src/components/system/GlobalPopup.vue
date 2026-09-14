<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';
import { usePopupStore } from '../../stores/popup.store';
import AuthLoginPopup from './AuthLoginPopup.vue';
import AuthRegisterPopup from './AuthRegisterPopup.vue';

const popup = usePopupStore();

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && popup.isOpen) {
    popup.close();
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown));
onUnmounted(() => window.removeEventListener('keydown', onKeydown));
</script>

<template>
  <Teleport to="body">
    <div
      v-if="popup.isOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      role="dialog"
      aria-modal="true"
      @click.self="popup.close()"
    >
      <div
        class="w-[min(96vw,42rem)] max-h-[92vh] overflow-y-auto rounded-[28px] bg-white p-6 shadow-2xl sm:p-8"
      >
        <AuthLoginPopup v-if="popup.isLogin" />
        <AuthRegisterPopup v-else-if="popup.isRegister" />
      </div>
    </div>
  </Teleport>
</template>
