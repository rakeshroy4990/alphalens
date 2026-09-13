import { describe, expect, it } from 'vitest';
import { capitalizeDisplay } from './display';

describe('capitalizeDisplay', () => {
  it('capitalizes the first character only', () => {
    expect(capitalizeDisplay('risk')).toBe('Risk');
    expect(capitalizeDisplay('revenueCagr GTE 0.08')).toBe('RevenueCagr GTE 0.08');
    expect(capitalizeDisplay('w 0.10')).toBe('W 0.10');
    expect(capitalizeDisplay('Quality compounder')).toBe('Quality compounder');
  });
});
