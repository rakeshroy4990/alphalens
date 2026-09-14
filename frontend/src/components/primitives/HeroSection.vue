<script setup lang="ts">
import { RouterLink } from 'vue-router';
import { homeContent } from '../../configs/siteChrome';

defineProps<{
  searchQuery: string;
}>();

const emit = defineEmits<{
  'update:searchQuery': [value: string];
  search: [];
}>();
</script>

<template>
  <section class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm sm:p-6">
    <div class="grid grid-cols-1 items-stretch gap-6 lg:grid-cols-2">
      <div class="flex flex-col justify-center gap-4">
        <h1 class="text-3xl font-extrabold leading-tight text-slate-900 sm:text-4xl md:text-5xl">
          {{ homeContent.hero.title }}
        </h1>
        <p class="text-base text-slate-600 sm:text-lg">
          {{ homeContent.hero.subtitle }}
        </p>
        <div class="flex flex-wrap gap-3">
          <a
            :href="homeContent.hero.ctaPrimary.href"
            class="rounded-xl bg-sky-600 px-5 py-3 font-semibold text-white hover:bg-sky-700"
          >
            {{ homeContent.hero.ctaPrimary.label }}
          </a>
          <RouterLink
            :to="homeContent.hero.ctaSecondary.to"
            class="rounded-xl border border-sky-600 bg-white px-5 py-3 font-semibold text-sky-700"
          >
            {{ homeContent.hero.ctaSecondary.label }}
          </RouterLink>
        </div>
        <ul class="grid grid-cols-2 gap-3 sm:grid-cols-3">
          <li
            v-for="stat in homeContent.stats"
            :key="stat.label"
            class="flex flex-col items-center gap-1 text-center"
          >
            <span class="text-3xl font-extrabold text-sky-700">{{ stat.value }}</span>
            <span class="text-sm font-medium text-slate-600">{{ stat.label }}</span>
          </li>
        </ul>
      </div>
      <form class="flex min-h-0 min-w-0 flex-col justify-center gap-3" @submit.prevent="emit('search')">
        <label class="text-sm font-medium text-slate-700" for="hero-search">Search stocks</label>
        <input
          id="hero-search"
          :value="searchQuery"
          class="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-3 text-sm text-slate-900"
          placeholder="Name, NSE, BSE, or ISIN"
          @input="emit('update:searchQuery', ($event.target as HTMLInputElement).value)"
        />
        <button class="rounded-xl bg-slate-900 px-5 py-3 text-sm font-semibold text-white" type="submit">
          Search
        </button>
      </form>
    </div>
  </section>
</template>
