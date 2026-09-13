import type { InstrumentDetail } from './instrument';

export interface Quote {
  instrumentId: number;
  lastPrice: number;
  change: number;
  changePercent: number;
  open: number;
  high: number;
  low: number;
  previousClose: number;
  volume: number;
  asOf: string;
  source: string;
  market: string;
}

export interface Candle {
  timestamp: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface CandleSeries {
  instrumentId: number;
  timeframe: string;
  from: string;
  to: string;
  source: string;
  candles: Candle[];
}

export interface StockPage {
  instrument: InstrumentDetail;
  quote: Quote;
  chart: CandleSeries;
  overallScore: number | null;
  componentScores: Record<string, number | null>;
  scoreWeights: Record<string, number>;
  algorithm: {
    name: string;
    score: number;
    factors: Record<string, number | null>;
    passedRules: string[];
    failedRules: string[];
    explanationData: Record<string, unknown>;
  };
  fundamentals: {
    currency: string;
    unit: string;
    source: string;
    periods: Array<Record<string, unknown>>;
    dividends: Array<Record<string, unknown>>;
  };
  analytics: Record<string, unknown>;
  valuation: Record<string, unknown>;
  riskNotes: string[];
  news: string[];
}
