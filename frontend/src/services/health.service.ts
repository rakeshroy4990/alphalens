import axios from 'axios';
import { apiClient } from './apiClient';
import type { HealthResponse } from '../types/health';

export async function fetchHealth(): Promise<HealthResponse> {
  try {
    const { data } = await apiClient.get<HealthResponse>('/health');
    return data;
  } catch (error) {
    if (axios.isAxiosError(error) && isHealthResponse(error.response?.data)) {
      return error.response.data;
    }
    throw error;
  }
}

function isHealthResponse(value: unknown): value is HealthResponse {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Partial<HealthResponse>;
  return candidate.status === 'UP' || candidate.status === 'DOWN';
}
