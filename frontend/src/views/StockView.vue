<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import SectionCard from '../components/primitives/SectionCard.vue';
import StockChart from '../components/StockChart.vue';
import { explainStock } from '../services/research.service';
import { useStockStore } from '../stores/stock.store';
import { capitalizeDisplay } from '../utils/display';

const route = useRoute();
const stock = useStockStore();
const timeframe = ref('1D');
const chartType = ref<'candlestick' | 'line'>('candlestick');
const explanation = ref<string | null>(null);

const instrumentId = computed(() => Number(route.params.instrumentId));

async function load() {
  await stock.load(instrumentId.value);
}

onMounted(load);
watch(instrumentId, load);

async function loadExplanation() {
  const payload = await explainStock(instrumentId.value);
  explanation.value = payload.explanation as string;
}

function format(value: unknown) {
  if (value == null) {
    return '—';
  }
  if (typeof value === 'number') {
    return value.toFixed(2);
  }
  return String(value);
}
</script>

<template>
  <div v-if="stock.loading" class="text-sm text-slate-500">Loading stock…</div>
  <div v-else-if="stock.error" class="text-sm text-rose-600">{{ stock.error }}</div>
  <article v-else-if="stock.page" class="flex flex-col gap-6 sm:gap-8">
    <SectionCard>
      <header class="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p class="text-xs uppercase tracking-[0.2em] text-slate-500">
            {{ stock.page.instrument.nseSymbol }} · {{ stock.page.instrument.isin }}
          </p>
          <h1 class="mt-1 text-3xl font-extrabold text-slate-900">{{ stock.page.instrument.companyName }}</h1>
        </div>
        <div class="text-right">
          <p class="text-3xl font-semibold text-slate-900">{{ format(stock.page.quote.lastPrice) }}</p>
          <p class="text-sm" :class="stock.page.quote.change >= 0 ? 'text-emerald-700' : 'text-rose-600'">
            {{ format(stock.page.quote.change) }} ({{ format(stock.page.quote.changePercent) }}%)
          </p>
          <p class="text-xs text-slate-500">{{ stock.page.quote.source }} quote</p>
        </div>
      </header>
    </SectionCard>

    <StockChart
      v-model:timeframe="timeframe"
      :instrument-id="instrumentId"
      :timeframe="timeframe"
      :chart-type="chartType"
    />

    <div class="grid gap-4 md:grid-cols-2">
      <SectionCard>
        <h2 class="text-sm font-semibold text-slate-900">Score</h2>
        <p class="mt-2 text-4xl font-semibold">{{ format(stock.page.overallScore) }}</p>
        <ul class="mt-4 space-y-1 text-sm text-slate-600">
          <li v-for="(value, key) in stock.page.componentScores" :key="key">
            {{ capitalizeDisplay(key) }} · {{ format(value) }}
            <span class="text-slate-400">({{ capitalizeDisplay('w') }} {{ format(stock.page.scoreWeights[key]) }})</span>
          </li>
        </ul>
      </SectionCard>
      <SectionCard>
        <h2 class="text-sm font-semibold text-slate-900">
          Why · {{ capitalizeDisplay(stock.page.algorithm.name) }}
        </h2>
        <p class="mt-2 text-sm text-slate-600">Algo score {{ format(stock.page.algorithm.score) }}</p>
        <p class="mt-3 text-xs font-semibold text-emerald-700">Passed</p>
        <ul class="text-sm text-slate-700">
          <li v-for="rule in stock.page.algorithm.passedRules" :key="rule">
            {{ capitalizeDisplay(rule) }}
          </li>
        </ul>
        <p class="mt-3 text-xs font-semibold text-rose-600">Failed</p>
        <ul class="text-sm text-slate-700">
          <li v-for="rule in stock.page.algorithm.failedRules" :key="rule">
            {{ capitalizeDisplay(rule) }}
          </li>
        </ul>
      </SectionCard>
    </div>

    <SectionCard>
      <h2 class="text-sm font-semibold text-slate-900">Fundamentals</h2>
      <p class="mt-1 text-xs text-slate-500">
        {{ stock.page.fundamentals.source }} · {{ stock.page.fundamentals.unit }}
        {{ stock.page.fundamentals.currency }}
      </p>
      <dl class="mt-4 grid grid-cols-2 gap-3 text-sm md:grid-cols-4">
        <div v-for="key in ['revenueCagr', 'roe', 'roce', 'patMargin']" :key="key">
          <dt class="text-slate-500">{{ capitalizeDisplay(key) }}</dt>
          <dd>{{ format(stock.page.analytics[key]) }}</dd>
        </div>
      </dl>
    </SectionCard>

    <SectionCard>
      <h2 class="text-sm font-semibold text-slate-900">Valuation</h2>
      <dl class="mt-4 grid grid-cols-2 gap-3 text-sm md:grid-cols-4">
        <div>
          <dt class="text-slate-500">PE</dt>
          <dd>{{ format(stock.page.valuation.pe) }}</dd>
        </div>
        <div>
          <dt class="text-slate-500">PB</dt>
          <dd>{{ format(stock.page.valuation.pb) }}</dd>
        </div>
        <div>
          <dt class="text-slate-500">EV/EBITDA</dt>
          <dd>{{ format(stock.page.valuation.evEbitda) }}</dd>
        </div>
        <div>
          <dt class="text-slate-500">DCF / share</dt>
          <dd>{{ format((stock.page.valuation.dcf as { valuePerShare?: number } | undefined)?.valuePerShare) }}</dd>
        </div>
      </dl>
      <p class="mt-4 text-xs text-slate-500">DCF assumptions are returned by the API and must stay visible.</p>
    </SectionCard>

    <SectionCard>
      <h2 class="text-sm font-semibold text-slate-900">Risk</h2>
      <ul class="mt-2 list-disc pl-5 text-sm text-slate-600">
        <li v-for="note in stock.page.riskNotes" :key="note">{{ capitalizeDisplay(note) }}</li>
      </ul>
    </SectionCard>

    <SectionCard>
      <h2 class="text-sm font-semibold text-slate-900">News / notes</h2>
      <ul class="mt-2 text-sm text-slate-600">
        <li v-for="item in stock.page.news" :key="item">{{ capitalizeDisplay(item) }}</li>
      </ul>
      <button class="mt-4 text-sm font-semibold text-sky-700" type="button" @click="loadExplanation">
        Explain these figures
      </button>
      <p v-if="explanation" class="mt-3 text-sm text-slate-700">{{ explanation }}</p>
    </SectionCard>
  </article>
</template>
