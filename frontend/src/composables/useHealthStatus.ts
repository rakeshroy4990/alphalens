import { onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useHealthStore } from '../stores/health.store';

export function useHealthStatus() {
  const store = useHealthStore();
  const { health, loading, error } = storeToRefs(store);

  onMounted(() => {
    void store.load();
  });

  return {
    health,
    loading,
    error,
    refresh: store.load
  };
}
