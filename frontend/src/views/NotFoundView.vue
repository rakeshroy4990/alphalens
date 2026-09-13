<script setup lang="ts">
import { RouterLink } from 'vue-router';
import SectionCard from '../components/primitives/SectionCard.vue';
</script>

<template>
  <SectionCard>
    <h1 class="text-2xl font-bold text-slate-900">Page not found</h1>
    <p class="mt-2 text-sm leading-6 text-slate-600">That path is not part of the research shell.</p>
    <RouterLink to="/" class="mt-6 inline-flex text-sm font-semibold text-sky-700 underline underline-offset-4">
      Go to home
    </RouterLink>
  </SectionCard>
</template>
