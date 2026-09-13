export interface InstrumentSummary {
  instrumentId: number;
  isin: string;
  nseSymbol: string | null;
  bseSymbol: string | null;
  companyName: string;
  shortName: string;
  sectorCode: string | null;
  industryCode: string | null;
  exchangeCode: string | null;
  status: string;
}

export interface InstrumentDetail extends InstrumentSummary {
  sectorName: string | null;
  industryName: string | null;
  updatedAt: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
