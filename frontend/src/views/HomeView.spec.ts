import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';
import { describe, expect, it, vi } from 'vitest';
import HomeView from './HomeView.vue';

vi.mock('../services/instrument.service', () => ({
  listInstruments: vi.fn().mockResolvedValue({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }),
  searchInstruments: vi.fn().mockResolvedValue({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 })
}));

describe('HomeView', () => {
  it('renders hero and stock information section without system status', async () => {
    setActivePinia(createPinia());
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/', component: HomeView }]
    });
    await router.push('/');
    const wrapper = mount(HomeView, {
      global: { plugins: [router] }
    });

    expect(wrapper.text()).toContain('See why a stock looks attractive or unattractive.');
    expect(wrapper.text()).toContain('Stock information');
    expect(wrapper.text()).not.toContain('System status');
    expect(wrapper.text()).not.toContain('Checking API and database');
  });
});
