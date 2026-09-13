<script setup lang="ts">
import SiteFooter from '../components/system/SiteFooter.vue';
import SiteHeader from '../components/system/SiteHeader.vue';
</script>

<template>
  <div class="flex min-h-screen flex-col bg-slate-50 text-slate-900">
    <div class="mx-auto flex min-h-screen w-full max-w-6xl flex-1 flex-col gap-6 px-4 pt-4 pb-0 sm:gap-8 sm:px-6 sm:pt-6 md:px-8 md:pt-8">
      <SiteHeader />
      <main class="flex w-full min-h-0 flex-1 flex-col gap-6 sm:gap-8">
        <router-view />
      </main>
      <SiteFooter />
    </div>
  </div>
</template>
