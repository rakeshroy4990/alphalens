export interface NavItem {
  id: string;
  label: string;
  to: string;
}

export const siteNav: NavItem[] = [
  { id: 'home', label: 'Home', to: '/home' },
  { id: 'screener', label: 'Screener', to: '/screener' },
  { id: 'watchlists', label: 'Watchlists', to: '/watchlists' },
  { id: 'portfolio', label: 'Portfolio', to: '/portfolio' }
];

export const siteChrome = {
  brand: 'AlphaLens',
  tagline: 'Indian stock intelligence',
  headerCta: { label: 'Open screener', to: '/screener' },
  footer: {
    tagline: 'Research and education only. Not investment advice.',
    links: [
      { label: 'Screener', to: '/screener' },
      { label: 'Portfolio', to: '/portfolio' }
    ]
  }
};

export const homeContent = {
  hero: {
    title: 'See why a stock looks attractive or unattractive.',
    subtitle:
      'Search stock information, then read market data, financials, valuation, and algorithms from the AlphaLens API. Mock fixtures until a licensed data feed is connected.',
    ctaPrimary: { label: 'Search stocks', href: '#universe' },
    ctaSecondary: { label: 'Open screener', to: '/screener' }
  },
  stats: [
    { value: '3', label: 'Demo stocks' },
    { value: 'MOCK', label: 'Quotes & filings' },
    { value: '0–100', label: 'Component scores' }
  ],
  sections: {
    universe: {
      heading: 'Stock information',
      subheading:
        'Search by name, NSE, BSE, or ISIN. Each listed stock has one identification record so price, filings, and scores stay attached to the right company.'
    },
    method: {
      heading: 'How AlphaLens is organized',
      subheading:
        'Stock Information → Stock Identification → Market Data → Financial Data → Valuation → Algorithms → Portfolio.',
      items: [
        { title: 'Stock information', description: 'Who the company is: name, sector, industry, listing status.' },
        {
          title: 'Stock identification',
          description: 'One record per listed stock (ISIN, NSE/BSE symbols). The same company can list on both exchanges.'
        },
        { title: 'Market data', description: 'Price and chart from the AlphaLens API only. The UI never calls a vendor.' },
        { title: 'Financial data', description: 'Statements and derived metrics. Missing values stay missing.' },
        { title: 'Valuation', description: 'PE, PB, DCF. Assumptions are never hidden.' },
        { title: 'Algorithms & portfolio', description: 'Configurable rules and scores, then holdings, concentration, and P&L.' }
      ]
    }
  }
};
