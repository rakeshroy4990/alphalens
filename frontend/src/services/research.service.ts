import { apiClient } from './apiClient';
import type { CandleSeries, Quote, StockPage } from '../types/research';

export async function fetchQuote(instrumentId: number): Promise<Quote> {
  const { data } = await apiClient.get<Quote>(`/instruments/${instrumentId}/quote`);
  return data;
}

export async function fetchCandles(
  instrumentId: number,
  timeframe: string,
  from: string,
  to: string
): Promise<CandleSeries> {
  const { data } = await apiClient.get<CandleSeries>(`/instruments/${instrumentId}/candles`, {
    params: { timeframe, from, to }
  });
  return data;
}

export async function fetchStockPage(instrumentId: number): Promise<StockPage> {
  const { data } = await apiClient.get<StockPage>(`/instruments/${instrumentId}/page`);
  return data;
}

export async function fetchIndicators(instrumentId: number, timeframe: string, from: string, to: string) {
  const { data } = await apiClient.get(`/instruments/${instrumentId}/indicators`, {
    params: { timeframe, from, to }
  });
  return data;
}

export async function explainStock(instrumentId: number) {
  const { data } = await apiClient.get(`/research/${instrumentId}/explain`);
  return data;
}

export async function screenStocks(filters: Record<string, unknown>) {
  const { data } = await apiClient.post('/screener', filters);
  return data;
}

export async function listWatchlists() {
  const { data } = await apiClient.get('/watchlists');
  return data;
}

export async function createWatchlist(name: string) {
  const { data } = await apiClient.post('/watchlists', { name });
  return data;
}

export async function addToWatchlist(watchlistId: number, instrumentId: number) {
  const { data } = await apiClient.post(`/watchlists/${watchlistId}/items`, { instrumentId });
  return data;
}

export async function fetchPortfolio() {
  const { data } = await apiClient.get('/portfolio');
  return data;
}

export async function addPortfolioHolding(instrumentId: number, quantity: number, averagePrice: number) {
  const { data } = await apiClient.post('/portfolio/holdings', { instrumentId, quantity, averagePrice });
  return data;
}
