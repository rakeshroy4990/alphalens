import { defineStore } from 'pinia';
import { listInstruments, searchInstruments } from '../services/instrument.service';
import type { InstrumentSummary } from '../types/instrument';

export const useInstrumentStore = defineStore('instruments', {
  state: () => ({
    items: [] as InstrumentSummary[],
    query: '',
    loading: false,
    error: null as string | null
  }),
  actions: {
    async loadUniverse() {
      this.loading = true;
      this.error = null;
      try {
        const page = this.query.trim()
          ? await searchInstruments(this.query.trim())
          : await listInstruments();
        this.items = page.items;
      } catch {
        this.items = [];
        this.error = 'Unable to load stocks.';
      } finally {
        this.loading = false;
      }
    }
  }
});
