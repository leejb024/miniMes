<template>
  <div class="page-card">
    <div class="page-header">
      <h1>부서 관리</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadDepts">조회</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>DEPT_ID</th>
            <th>DEPT_NAME</th>
            <th>USE_YN</th>
            <th>CRE_ID</th>
            <th>CRE_DT</th>
            <th>MOD_ID</th>
            <th>MOD_DT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="depts.length === 0">
            <td colspan="7" class="empty">조회된 부서가 없습니다.</td>
          </tr>
          <tr v-for="dept in depts" v-else :key="dept.deptId">
            <td>{{ dept.deptId }}</td>
            <td>{{ dept.deptName }}</td>
            <td>{{ dept.useYn }}</td>
            <td>{{ dept.creId }}</td>
            <td>{{ formatDate(dept.creDt) }}</td>
            <td>{{ dept.modId }}</td>
            <td>{{ formatDate(dept.modDt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchDepts } from '@/api/dept'

const depts = ref([])
const loading = ref(false)
const errorMessage = ref('')

function formatDate(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadDepts() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchDepts()
    depts.value = Array.isArray(data) ? data : []
  } catch (error) {
    depts.value = []
    errorMessage.value = error.response?.data?.message || '부서 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadDepts)
</script>
