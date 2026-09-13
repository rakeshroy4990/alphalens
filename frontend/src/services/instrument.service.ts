import { apiClient } from './apiClient';
import type { InstrumentDetail, InstrumentSummary, PageResponse } from '../types/instrument';

export async function listInstruments(page = 0, size = 20): Promise<PageResponse<InstrumentSummary>> {
  const { data } = await apiClient.get<PageResponse<InstrumentSummary>>('/instruments', {
    params: { page, size }
  });
  return data;
}

export async function searchInstruments(q: string, page = 0, size = 20): Promise<PageResponse<InstrumentSummary>> {
  const { data } = await apiClient.get<PageResponse<InstrumentSummary>>('/instruments/search', {
    params: { q, page, size }
  });
  return data;
}

export async function getInstrument(instrumentId: number): Promise<InstrumentDetail> {
  const { data } = await apiClient.get<InstrumentDetail>(`/instruments/${instrumentId}`);
  return data;
}
