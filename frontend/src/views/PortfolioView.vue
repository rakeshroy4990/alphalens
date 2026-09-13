<script setup lang="ts">
import { onMounted, ref } from 'vue';
import SectionCard from '../components/primitives/SectionCard.vue';
import { addPortfolioHolding, fetchPortfolio } from '../services/research.service';

const snapshot = ref<Record<string, unknown> | null>(null);
const instrumentId = ref('1');
const quantity = ref('10');
const averagePrice = ref('100');

async function reload() {
  snapshot.value = (await fetchPortfolio()) as Record<string, unknown>;
}

onMounted(() => {
  void reload();
});

async function add() {
  await addPortfolioHolding(Number(instrumentId.value), Number(quantity.value), Number(averagePrice.value));
  await reload();
}
</script>

<template>
  <SectionCard>
    <h1 class="text-2xl font-bold text-slate-900 sm:text-3xl">Portfolio</h1>
    <p class="mt-2 text-sm text-slate-600">Manual holdings only. Broker sync stays server-side and is not live.</p>
    <form class="mt-6 grid gap-3 md:grid-cols-4" @submit.prevent="add">
      <input v-model="instrumentId" class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm" />
      <input v-model="quantity" class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm" />
      <input v-model="averagePrice" class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm" />
      <button class="rounded-lg bg-sky-600 px-4 py-2 text-sm font-semibold text-white" type="submit">Add</button>
    </form>
    <dl v-if="snapshot" class="mt-6 grid grid-cols-2 gap-3 text-sm md:grid-cols-4">
      <div>
        <dt class="text-slate-500">Value</dt>
        <dd class="font-semibold">{{ snapshot.totalMarketValue }}</dd>
      </div>
      <div>
        <dt class="text-slate-500">P&L</dt>
        <dd class="font-semibold">{{ snapshot.pnl }}</dd>
      </div>
      <div>
        <dt class="text-slate-500">Score</dt>
        <dd class="font-semibold">{{ snapshot.portfolioScore }}</dd>
      </div>
      <div>
        <dt class="text-slate-500">Concentration</dt>
        <dd class="font-semibold">{{ snapshot.concentration }}</dd>
      </div>
    </dl>
  </SectionCard>
</template>
