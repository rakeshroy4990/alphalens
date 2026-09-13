<script setup lang="ts">
import { RouterLink } from 'vue-router';
import { siteChrome } from '../../configs/siteChrome';
</script>

<template>
  <footer class="mt-auto w-full shrink-0 rounded-2xl border border-slate-200 bg-white px-4 py-3">
    <p class="text-center text-xs text-slate-500">{{ siteChrome.footer.tagline }}</p>
    <div class="flex flex-wrap items-center justify-center gap-3 pt-2">
      <RouterLink
        v-for="link in siteChrome.footer.links"
        :key="link.to"
        :to="link.to"
        class="text-sm font-semibold text-sky-700 underline underline-offset-2 hover:text-sky-800"
      >
        {{ link.label }}
      </RouterLink>
    </div>
  </footer>
</template>
