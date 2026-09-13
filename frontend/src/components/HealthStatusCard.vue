<script setup lang="ts">
import type { HealthResponse } from '../types/health';

defineProps<{
  health: HealthResponse | null;
  loading: boolean;
  error: string | null;
}>();

function statusClass(status: string | undefined): string {
  if (status === 'UP') {
    return 'text-emerald-400';
  }
  if (status === 'DOWN') {
    return 'text-rose-400';
  }
  return 'text-slate-400';
}
</script>

<template>
  <section class="rounded-xl border border-slate-800 bg-slate-900/60 p-6">
    <h2 class="text-sm font-medium uppercase tracking-[0.18em] text-slate-400">System status</h2>

    <p v-if="loading" class="mt-4 text-slate-300">Checking API and database…</p>
    <p v-else-if="error" class="mt-4 text-rose-400">{{ error }}</p>
    <dl v-else-if="health" class="mt-4 grid gap-3 text-sm">
      <div class="flex justify-between">
        <dt class="text-slate-400">API</dt>
        <dd :class="statusClass(health.status)">{{ health.status }}</dd>
      </div>
      <div class="flex justify-between">
        <dt class="text-slate-400">Database</dt>
        <dd :class="statusClass(health.database)">{{ health.database }}</dd>
      </div>
      <div class="flex justify-between">
        <dt class="text-slate-400">Service</dt>
        <dd class="text-slate-200">{{ health.service }}</dd>
      </div>
    </dl>
  </section>
</template>
