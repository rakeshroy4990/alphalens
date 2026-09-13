<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { siteChrome, siteNav } from '../../configs/siteChrome';

const route = useRoute();

const activeId = computed(() => {
  if (route.path.startsWith('/stocks')) {
    return 'home';
  }
  const match = siteNav.find((item) => item.to !== '/' && route.path.startsWith(item.to));
  return match?.id ?? 'home';
});

function isActive(id: string) {
  return activeId.value === id;
}
</script>

<template>
  <header
    class="sticky top-0 z-20 flex min-w-0 flex-row flex-nowrap items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white/95 px-4 py-3 shadow-sm backdrop-blur"
  >
    <RouterLink to="/" class="flex min-w-0 shrink-0 items-center gap-2 sm:gap-3">
      <span class="inline-flex h-9 w-9 items-center justify-center rounded-full bg-sky-600 text-sm font-extrabold text-white sm:h-10 sm:w-10">
        AL
      </span>
      <span class="truncate text-base font-extrabold text-slate-900 sm:text-lg md:text-xl">
        {{ siteChrome.brand }}
      </span>
    </RouterLink>

    <nav class="flex min-w-0 flex-1 flex-row flex-nowrap items-center justify-end gap-1 overflow-x-auto sm:justify-center sm:gap-2">
      <RouterLink
        v-for="item in siteNav"
        :key="item.id"
        :to="item.to"
        class="shrink-0 rounded-lg px-2 py-2 text-sm font-medium whitespace-nowrap sm:px-3"
        :class="
          isActive(item.id)
            ? 'bg-sky-100 font-semibold text-sky-800'
            : 'text-slate-700 hover:bg-slate-100'
        "
      >
        {{ item.label }}
      </RouterLink>
    </nav>

    <RouterLink
      :to="siteChrome.headerCta.to"
      class="hidden shrink-0 rounded-lg bg-sky-600 px-3 py-2 text-sm font-semibold whitespace-nowrap text-white hover:bg-sky-700 sm:inline-flex"
    >
      {{ siteChrome.headerCta.label }}
    </RouterLink>
  </header>
</template>
