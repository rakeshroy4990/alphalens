import { createPinia, setActivePinia } from 'pinia';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useHealthStore } from './health.store';
import { fetchHealth } from '../services/health.service';

vi.mock('../services/health.service', () => ({
  fetchHealth: vi.fn()
}));

describe('useHealthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('loads health from the API service', async () => {
    vi.mocked(fetchHealth).mockResolvedValue({
      status: 'UP',
      database: 'UP',
      service: 'alphalens-backend',
      timestamp: '2026-09-13T09:30:00Z'
    });

    const store = useHealthStore();
    await store.load();

    expect(store.health?.status).toBe('UP');
    expect(store.error).toBeNull();
    expect(store.loading).toBe(false);
  });

  it('records an error when the API cannot be reached', async () => {
    vi.mocked(fetchHealth).mockRejectedValue(new Error('network'));

    const store = useHealthStore();
    await store.load();

    expect(store.health).toBeNull();
    expect(store.error).toBe('Unable to reach the AlphaLens API.');
  });
});
