import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from './apiClient';
import { searchInstruments } from './instrument.service';

vi.mock('./apiClient', () => ({
  apiClient: { get: vi.fn() }
}));

describe('searchInstruments', () => {
  afterEach(() => {
    vi.resetAllMocks();
  });

  it('calls the instrument search API', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }
    });
    await searchInstruments('DEMA');
    expect(apiClient.get).toHaveBeenCalledWith('/instruments/search', {
      params: { q: 'DEMA', page: 0, size: 20 }
    });
  });
});
