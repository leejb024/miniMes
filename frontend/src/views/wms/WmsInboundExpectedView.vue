<template>
  <div class="page-card">
    <div class="page-header">
      <h1>입고예정</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadPlans">조회</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="section-head">
      <h2>입고예정 리스트</h2>
      <span class="result-count">검색결과 : {{ plans.length }}건</span>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>입고예정번호</th>
            <th>입고예정일</th>
            <th>작업지시번호</th>
            <th>품번</th>
            <th>품명</th>
            <th>LOT NO</th>
            <th>예정수량</th>
            <th>단위</th>
            <th>창고</th>
            <th>상태</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="plans.length === 0">
            <td colspan="10" class="empty">조회된 입고예정이 없습니다.</td>
          </tr>
          <tr v-for="plan in plans" v-else :key="plan.planNo">
            <td>{{ plan.planNo }}</td>
            <td>{{ plan.planDate }}</td>
            <td>{{ plan.workOrderId }}</td>
            <td>{{ plan.itemId }}</td>
            <td>{{ plan.itemName }}</td>
            <td>{{ plan.lotNo }}</td>
            <td>{{ formatQty(plan.qty) }}</td>
            <td>{{ plan.unit }}</td>
            <td>{{ plan.warehouseName }}</td>
            <td>{{ plan.status }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchWmsInboundPlans } from '@/api/wmsInboundPlan'

const plans = ref([])
const loading = ref(false)
const errorMessage = ref('')

function formatQty(value) {
  if (value === null || value === undefined || value === '') {
    return ''
  }
  const number = Number(value)
  if (Number.isNaN(number)) {
    return value
  }
  return number.toLocaleString('ko-KR')
}

async function loadPlans() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchWmsInboundPlans()
    plans.value = Array.isArray(data) ? data : []
  } catch (error) {
    plans.value = []
    errorMessage.value = error.response?.data?.message || '입고예정 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadPlans)
</script>
