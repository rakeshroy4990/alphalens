import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { screenStocks } from '../services/research.service';
import { useScreenerStore } from './screener.store';

vi.mock('../services/research.service', () => ({
  screenStocks: vi.fn()
}));

describe('screener store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.mocked(screenStocks).mockReset();
  });

  it('loads matching instruments from the API', async () => {
    vi.mocked(screenStocks).mockResolvedValue({
      items: [
        {
          instrument: {
            instrumentId: 1,
            shortName: 'AlphaTest',
            nseSymbol: 'DEMA',
            companyName: 'Alpha Test Industrials Limited'
          }
        }
      ]
    });
    const store = useScreenerStore();
    await store.run();
    expect(screenStocks).toHaveBeenCalledWith({ roce: { min: 0.1 } });
    expect(store.rows).toHaveLength(1);
    expect(store.error).toBeNull();
  });
});
