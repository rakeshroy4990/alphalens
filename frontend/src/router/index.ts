import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';
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
        { path: 'login', name: 'login', component: HomeView },
        { path: 'register', name: 'register', component: HomeView },
        { path: 'stocks/:instrumentId', name: 'stock', component: StockView },
        { path: 'screener', name: 'screener', component: ScreenerView },
        { path: 'watchlists', name: 'watchlists', component: WatchlistView, meta: { requiresAuth: true } },
        { path: 'portfolio', name: 'portfolio', component: PortfolioView, meta: { requiresAuth: true } },
        { path: ':pathMatch(.*)*', name: 'not-found', component: NotFoundView }
      ]
    }
  ]
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (!auth.bootstrapped) {
    await auth.bootstrap();
  }
  if (to.name === 'login') {
    auth.openLogin();
    return { path: '/home' };
  }
  if (to.name === 'register') {
    auth.openRegister();
    return { path: '/home' };
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    auth.requireAuth(to.fullPath);
    return { path: '/home' };
  }
  return true;
});
