import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import HealthStatusCard from './HealthStatusCard.vue';

describe('HealthStatusCard', () => {
  it('renders API and database status from props', () => {
    const wrapper = mount(HealthStatusCard, {
      props: {
        loading: false,
        error: null,
        health: {
          status: 'UP',
          database: 'UP',
          service: 'alphalens-backend',
          timestamp: '2026-09-13T09:30:00Z'
        }
      }
    });

    expect(wrapper.text()).toContain('UP');
    expect(wrapper.text()).toContain('alphalens-backend');
  });

  it('renders the error message when the API is unreachable', () => {
    const wrapper = mount(HealthStatusCard, {
      props: {
        loading: false,
        error: 'Unable to reach the AlphaLens API.',
        health: null
      }
    });

    expect(wrapper.text()).toContain('Unable to reach the AlphaLens API.');
  });
});
