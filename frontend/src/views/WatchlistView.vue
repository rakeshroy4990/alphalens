<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { createWatchlist, listWatchlists } from '../services/research.service';
import SectionCard from '../components/primitives/SectionCard.vue';

const lists = ref<Array<Record<string, unknown>>>([]);
const name = ref('Core');
const error = ref<string | null>(null);

async function reload() {
  lists.value = (await listWatchlists()) as Array<Record<string, unknown>>;
}

onMounted(() => {
  void reload().catch(() => {
    error.value = 'Unable to load watchlists.';
  });
});

async function create() {
  await createWatchlist(name.value);
  await reload();
}
</script>

<template>
  <SectionCard>
    <h1 class="text-2xl font-bold text-slate-900 sm:text-3xl">Watchlists</h1>
    <form class="mt-6 flex gap-3" @submit.prevent="create">
      <input v-model="name" class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm" />
      <button class="rounded-lg bg-sky-600 px-4 py-2 text-sm font-semibold text-white" type="submit">Create</button>
    </form>
    <p v-if="error" class="mt-4 text-sm text-rose-600">{{ error }}</p>
    <ul class="mt-6 space-y-2 text-sm">
      <li v-for="list in lists" :key="String(list.id)" class="rounded-lg border border-slate-200 px-4 py-3">
        {{ list.name }} · score {{ list.score ?? '—' }}
      </li>
    </ul>
  </SectionCard>
</template>
