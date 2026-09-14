<script setup lang="ts">
import { onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import SectionCard from '../components/primitives/SectionCard.vue';
import { useScreenerStore } from '../stores/screener.store';

const screener = useScreenerStore();

onMounted(() => {
  void screener.run();
});
</script>

<template>
  <SectionCard>
    <h1 class="text-2xl font-bold text-slate-900 sm:text-3xl">Screener</h1>
    <form class="mt-6 flex flex-wrap items-end gap-3" @submit.prevent="screener.run()">
      <label class="text-sm text-slate-600">
        Min ROCE
        <input
          v-model="screener.roceMin"
          class="mt-1 block rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-slate-900"
        />
      </label>
      <button class="rounded-lg bg-sky-600 px-4 py-2 text-sm font-semibold text-white" type="submit">
        {{ screener.loading ? 'Running…' : 'Run' }}
      </button>
    </form>
    <p v-if="screener.loading" class="mt-6 text-sm text-slate-500">Loading matches…</p>
    <p v-else-if="screener.error" class="mt-6 text-sm text-rose-600">{{ screener.error }}</p>
    <p v-else-if="screener.ran && screener.rows.length === 0" class="mt-6 text-sm text-slate-500">
      No stocks passed these filters.
    </p>
    <ul v-else class="mt-6 divide-y divide-slate-200 rounded-xl border border-slate-200">
      <li v-for="row in screener.rows" :key="row.instrument.instrumentId">
        <RouterLink
          class="flex items-center justify-between px-4 py-3 text-sm hover:bg-slate-50"
          :to="`/stocks/${row.instrument.instrumentId}`"
        >
          <span>
            <span class="font-medium text-slate-900">{{ row.instrument.shortName }}</span>
            <span class="ml-2 text-slate-500">{{ row.instrument.nseSymbol }}</span>
          </span>
          <span class="text-xs text-slate-500">{{ row.instrument.companyName }}</span>
        </RouterLink>
      </li>
    </ul>
  </SectionCard>
</template>
