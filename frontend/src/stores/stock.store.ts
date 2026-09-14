import { defineStore } from 'pinia';
import { fetchCandles, fetchStockPage } from '../services/research.service';
import type { CandleSeries, StockPage } from '../types/research';

export const useStockStore = defineStore('stock', {
  state: () => ({
    page: null as StockPage | null,
    candles: null as CandleSeries | null,
    loading: false,
    error: null as string | null
  }),
  actions: {
    async load(instrumentId: number) {
      this.loading = true;
      this.error = null;
      try {
        this.page = await fetchStockPage(instrumentId);
        this.candles = this.page.chart;
      } catch {
        this.page = null;
        this.candles = null;
        this.error = 'Unable to load this stock.';
      } finally {
        this.loading = false;
      }
    },
    async loadCandles(instrumentId: number, timeframe: string, from: string, to: string) {
      this.candles = await fetchCandles(instrumentId, timeframe, from, to);
    }
  }
});
