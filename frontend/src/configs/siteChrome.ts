export interface NavItem {
  id: string;
  label: string;
  to: string;
}

export const siteNav: NavItem[] = [
  { id: 'home', label: 'Home', to: '/' },
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
      'Search the instrument master, then read price, score, fundamentals, and valuation from the AlphaLens API. Mock fixtures until a licensed data feed is connected.',
    ctaPrimary: { label: 'Search instruments', href: '#universe' },
    ctaSecondary: { label: 'Open screener', to: '/screener' }
  },
  stats: [
    { value: '3', label: 'Demo instruments' },
    { value: 'MOCK', label: 'Quotes & filings' },
    { value: '0–100', label: 'Component scores' }
  ],
  sections: {
    universe: {
      heading: 'Instrument master',
      subheading: 'Search by name, NSE, BSE, or ISIN. The canonical id is instrumentId.'
    },
    method: {
      heading: 'How the research page is built',
      subheading: 'Same chrome as the hospital shell: header, hero, section cards, footer. No system status on the page.',
      items: [
        { title: 'Price & chart', description: 'Backend candles only. The UI never calls a market-data vendor.' },
        { title: 'Score & why', description: 'Component scores and configurable rules. Weights stay visible.' },
        { title: 'Fundamentals & valuation', description: 'Deterministic math. DCF assumptions are never hidden.' }
      ]
    }
  }
};
