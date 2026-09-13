<script setup lang="ts">
import { onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import HeroSection from '../components/primitives/HeroSection.vue';
import SectionCard from '../components/primitives/SectionCard.vue';
import { homeContent } from '../configs/siteChrome';
import { useInstrumentStore } from '../stores/instrument.store';

const instruments = useInstrumentStore();

onMounted(() => {
  void instruments.loadUniverse();
});
</script>

<template>
  <div class="flex flex-col gap-6 sm:gap-8">
    <HeroSection
      :search-query="instruments.query"
      @update:search-query="instruments.query = $event"
      @search="instruments.loadUniverse()"
    />

    <SectionCard id="universe">
      <h2 class="text-2xl font-bold text-slate-900 sm:text-3xl">
        {{ homeContent.sections.universe.heading }}
      </h2>
      <p class="mt-2 text-slate-600">{{ homeContent.sections.universe.subheading }}</p>
      <p v-if="instruments.loading" class="mt-4 text-sm text-slate-500">Loading instruments…</p>
      <p v-if="instruments.error" class="mt-4 text-sm text-rose-600">{{ instruments.error }}</p>
      <ul class="mt-6 divide-y divide-slate-200 rounded-xl border border-slate-200">
        <li v-for="item in instruments.items" :key="item.instrumentId">
          <RouterLink
            class="flex items-center justify-between px-4 py-3 text-sm hover:bg-slate-50"
            :to="`/stocks/${item.instrumentId}`"
          >
            <span>
              <span class="font-medium text-slate-900">{{ item.shortName }}</span>
              <span class="ml-2 text-slate-500">{{ item.nseSymbol }}</span>
            </span>
            <span class="text-xs text-slate-500">{{ item.companyName }}</span>
          </RouterLink>
        </li>
      </ul>
    </SectionCard>

    <SectionCard>
      <h2 class="text-2xl font-bold text-slate-900 sm:text-3xl">
        {{ homeContent.sections.method.heading }}
      </h2>
      <p class="mt-2 text-slate-600">{{ homeContent.sections.method.subheading }}</p>
      <ul class="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <li
          v-for="item in homeContent.sections.method.items"
          :key="item.title"
          class="rounded-xl border border-slate-200 p-4"
        >
          <h3 class="font-semibold text-slate-900">{{ item.title }}</h3>
          <p class="mt-2 text-sm text-slate-600">{{ item.description }}</p>
        </li>
      </ul>
    </SectionCard>
  </div>
</template>
