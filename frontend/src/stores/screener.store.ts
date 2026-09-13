import { defineStore } from 'pinia';
import { screenStocks } from '../services/research.service';
import type { InstrumentSummary } from '../types/instrument';

export interface ScreenRow {
  instrument: InstrumentSummary;
  score?: { overall?: number | null };
}

export const useScreenerStore = defineStore('screener', {
  state: () => ({
    roceMin: '0.10',
    rows: [] as ScreenRow[],
    loading: false,
    error: null as string | null,
    ran: false
  }),
  actions: {
    async run() {
      this.loading = true;
      this.error = null;
      try {
        const result = await screenStocks({
          roce: { min: Number(this.roceMin) }
        });
        this.rows = (result.items ?? []) as ScreenRow[];
        this.ran = true;
      } catch {
        this.rows = [];
        this.error = 'Unable to run the screener.';
      } finally {
        this.loading = false;
      }
    }
  }
});
