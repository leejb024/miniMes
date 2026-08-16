<template>
  <div class="page-card">
    <h1>{{ title }}</h1>
    <p class="empty">화면을 준비 중입니다.</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '')
</script>
