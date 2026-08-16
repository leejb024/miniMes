<template>
  <div class="page-card">
    <div class="page-header">
      <h1>공정관리</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadProcesses">조회</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>PROCESS_ID</th>
            <th>PROCESS_NAME</th>
            <th>USE_YN</th>
            <th>CRE_ID</th>
            <th>CRE_DT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="processes.length === 0">
            <td colspan="5" class="empty">조회된 공정이 없습니다.</td>
          </tr>
          <tr v-for="process in processes" v-else :key="process.processId">
            <td>{{ process.processId }}</td>
            <td>{{ process.processName }}</td>
            <td>{{ process.useYn }}</td>
            <td>{{ process.creId }}</td>
            <td>{{ formatDate(process.creDt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchProcesses } from '@/api/process'

const processes = ref([])
const loading = ref(false)
const errorMessage = ref('')

function formatDate(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadProcesses() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchProcesses()
    processes.value = Array.isArray(data) ? data : []
  } catch (error) {
    processes.value = []
    errorMessage.value = error.response?.data?.message || '공정 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadProcesses)
</script>
