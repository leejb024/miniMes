<template>
  <div class="page-card">
    <div class="page-header">
      <h1>워크센터 관리</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadWorkcenters">조회</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>WORKCENTER_ID</th>
            <th>WORKCENTER_NAME</th>
            <th>USE_YN</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="3" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="workcenters.length === 0">
            <td colspan="3" class="empty">조회된 워크센터가 없습니다.</td>
          </tr>
          <tr v-for="workcenter in workcenters" v-else :key="workcenter.workcenterId">
            <td>{{ workcenter.workcenterId }}</td>
            <td>{{ workcenter.workcenterName }}</td>
            <td>{{ workcenter.useYn }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchWorkcenters } from '@/api/workcenter'

const workcenters = ref([])
const loading = ref(false)
const errorMessage = ref('')

async function loadWorkcenters() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchWorkcenters()
    workcenters.value = Array.isArray(data) ? data : []
  } catch (error) {
    workcenters.value = []
    errorMessage.value = error.response?.data?.message || '워크센터 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadWorkcenters)
</script>
