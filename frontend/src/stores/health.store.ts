import { defineStore } from 'pinia';
import { fetchHealth } from '../services/health.service';
import type { HealthResponse } from '../types/health';

export const useHealthStore = defineStore('health', {
  state: () => ({
    health: null as HealthResponse | null,
    loading: false,
    error: null as string | null
  }),
  actions: {
    async load() {
      this.loading = true;
      this.error = null;
      try {
        this.health = await fetchHealth();
      } catch {
        this.health = null;
        this.error = 'Unable to reach the AlphaLens API.';
      } finally {
        this.loading = false;
      }
    }
  }
});
