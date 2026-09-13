import { afterEach, describe, expect, it, vi } from 'vitest';
import axios from 'axios';
import { fetchHealth } from './health.service';
import { apiClient } from './apiClient';

vi.mock('./apiClient', () => ({
  apiClient: {
    get: vi.fn()
  }
}));

describe('fetchHealth', () => {
  afterEach(() => {
    vi.resetAllMocks();
  });

  it('returns healthy payload from the backend', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: {
        status: 'UP',
        database: 'UP',
        service: 'alphalens-backend',
        timestamp: '2026-09-13T09:30:00Z'
      }
    });

    await expect(fetchHealth()).resolves.toEqual({
      status: 'UP',
      database: 'UP',
      service: 'alphalens-backend',
      timestamp: '2026-09-13T09:30:00Z'
    });
  });

  it('returns a 503 health body instead of throwing', async () => {
    const downBody = {
      status: 'DOWN',
      database: 'DOWN',
      service: 'alphalens-backend',
      timestamp: '2026-09-13T09:30:00Z'
    };
    const axiosError = new axios.AxiosError('Service Unavailable');
    axiosError.response = {
      status: 503,
      data: downBody,
      statusText: 'Service Unavailable',
      headers: {},
      config: { headers: {} as never }
    };
    vi.mocked(apiClient.get).mockRejectedValue(axiosError);

    await expect(fetchHealth()).resolves.toEqual(downBody);
  });
});
