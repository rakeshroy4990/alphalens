import { createRouter, createWebHistory } from 'vue-router';
import AppLayout from '../layouts/AppLayout.vue';
import HomeView from '../views/HomeView.vue';
import NotFoundView from '../views/NotFoundView.vue';
import PortfolioView from '../views/PortfolioView.vue';
import ScreenerView from '../views/ScreenerView.vue';
import StockView from '../views/StockView.vue';
import WatchlistView from '../views/WatchlistView.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: '/home' },
        { path: 'home', name: 'home', component: HomeView },
        { path: 'stocks/:instrumentId', name: 'stock', component: StockView },
        { path: 'screener', name: 'screener', component: ScreenerView },
        { path: 'watchlists', name: 'watchlists', component: WatchlistView },
        { path: 'portfolio', name: 'portfolio', component: PortfolioView },
        { path: ':pathMatch(.*)*', name: 'not-found', component: NotFoundView }
      ]
    }
  ]
});
