<script setup lang="ts">
import { createChart, type IChartApi, type ISeriesApi } from 'lightweight-charts';
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { fetchCandles } from '../services/research.service';
import type { Candle } from '../types/research';

const props = defineProps<{
  instrumentId: number;
  timeframe: string;
  chartType: 'candlestick' | 'line';
}>();

const emit = defineEmits<{
  'update:timeframe': [value: string];
}>();

const host = ref<HTMLDivElement | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const source = ref('MOCK');
const timeframes = ['1D', '1W', '1H'];

let chart: IChartApi | null = null;
let series: ISeriesApi<'Candlestick'> | ISeriesApi<'Line'> | ISeriesApi<'Histogram'> | null = null;
let volumeSeries: ISeriesApi<'Histogram'> | null = null;

async function render() {
  if (!host.value) {
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - (props.timeframe === '1W' ? 720 : 180));
    const seriesData = await fetchCandles(
      props.instrumentId,
      props.timeframe,
      from.toISOString().slice(0, 10),
      to.toISOString().slice(0, 10)
    );
    source.value = seriesData.source;
    draw(seriesData.candles);
  } catch {
    error.value = 'Chart data unavailable.';
  } finally {
    loading.value = false;
  }
}

function draw(candles: Candle[]) {
  if (!host.value) {
    return;
  }
  chart?.remove();
  chart = createChart(host.value, {
    width: host.value.clientWidth,
    height: 360,
    layout: { background: { color: '#020617' }, textColor: '#cbd5e1' },
    grid: { vertLines: { color: '#1e293b' }, horzLines: { color: '#1e293b' } },
    crosshair: { mode: 1 },
    rightPriceScale: { borderColor: '#334155' },
    timeScale: { borderColor: '#334155' }
  });
  if (props.chartType === 'line') {
    const line = chart.addLineSeries({ color: '#38bdf8' });
    line.setData(candles.map((c) => ({ time: toTime(c.timestamp), value: Number(c.close) })));
    series = line;
  } else {
    const candleSeries = chart.addCandlestickSeries({
      upColor: '#34d399',
      downColor: '#fb7185',
      borderVisible: false,
      wickUpColor: '#34d399',
      wickDownColor: '#fb7185'
    });
    candleSeries.setData(
      candles.map((c) => ({
        time: toTime(c.timestamp),
        open: Number(c.open),
        high: Number(c.high),
        low: Number(c.low),
        close: Number(c.close)
      }))
    );
    series = candleSeries;
  }
  volumeSeries = chart.addHistogramSeries({
    priceFormat: { type: 'volume' },
    priceScaleId: ''
  });
  volumeSeries.setData(
    candles.map((c) => ({
      time: toTime(c.timestamp),
      value: c.volume,
      color: Number(c.close) >= Number(c.open) ? '#065f46' : '#9f1239'
    }))
  );
  chart.timeScale().fitContent();
}

function toTime(value: string): string {
  return value.slice(0, 10);
}

onMounted(async () => {
  await nextTick();
  await render();
});

watch(
  () => [props.instrumentId, props.timeframe, props.chartType],
  () => {
    void render();
  }
);

onBeforeUnmount(() => {
  chart?.remove();
});
</script>

<template>
  <section class="overflow-hidden rounded-2xl border border-slate-200 bg-slate-950 p-4 shadow-sm">
    <div class="mb-3 flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-sm font-medium text-slate-200">Chart</h2>
        <p class="text-xs text-slate-500">Source {{ source }} · backend candles only</p>
      </div>
      <div class="flex gap-2">
        <button
          v-for="tf in timeframes"
          :key="tf"
          class="rounded px-2 py-1 text-xs"
          :class="tf === timeframe ? 'bg-sky-500 text-slate-950' : 'bg-slate-800 text-slate-300'"
          @click="emit('update:timeframe', tf)"
        >
          {{ tf }}
        </button>
      </div>
    </div>
    <p v-if="loading" class="text-sm text-slate-400">Loading chart…</p>
    <p v-if="error" class="text-sm text-rose-400">{{ error }}</p>
    <div ref="host" class="h-[360px] w-full" />
  </section>
</template>
