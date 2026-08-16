<template>
  <div class="page-card">
    <div class="page-header">
      <h1>작업지시</h1>
      <div class="filter-bar">
        <label>
          부서
          <input v-model.trim="deptId" type="text" placeholder="DEPT_ID" />
        </label>
        <label>
          계획일
          <input v-model="planDateFrom" type="date" />
        </label>
        <span class="filter-sep">~</span>
        <input v-model="planDateTo" type="date" />
        <button type="button" class="search-btn" :disabled="loading" @click="loadWorkOrders">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>PLAN_DATE</th>
            <th>WORK_ORDER_ID</th>
            <th>WORKCENTER_ID</th>
            <th>ITEM_ID</th>
            <th>DEPT_ID</th>
            <th>STATE</th>
            <th>UNIT</th>
            <th>PLAN_QTY</th>
            <th>EXPIRED_DATE</th>
            <th>ITEM_VERSION</th>
            <th>BOM_VERSION</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="11" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="rows.length === 0">
            <td colspan="11" class="empty">조회된 작업지시가 없습니다.</td>
          </tr>
          <tr v-for="row in rows" v-else :key="row.workOrderId + '|' + (row.bomVersion || '')">
            <td>{{ row.planDate }}</td>
            <td>{{ row.workOrderId }}</td>
            <td>{{ row.workcenterId }}</td>
            <td>{{ row.itemId }}</td>
            <td>{{ row.deptId }}</td>
            <td>{{ row.state }}</td>
            <td>{{ row.unit }}</td>
            <td>{{ row.planQty }}</td>
            <td>{{ row.expiredDate }}</td>
            <td>{{ row.itemVersion }}</td>
            <td>{{ row.bomVersion }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchWorkOrders } from '@/api/workOrder'

const deptId = ref('27')
const planDateFrom = ref('2026-06-01')
const planDateTo = ref('2026-08-16')
const rows = ref([])
const loading = ref(false)
const errorMessage = ref('')

async function loadWorkOrders() {
  if (!deptId.value || !planDateFrom.value || !planDateTo.value) {
    errorMessage.value = '부서와 계획일 기간을 입력하세요.'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchWorkOrders(deptId.value, planDateFrom.value, planDateTo.value)
    rows.value = Array.isArray(data) ? data : []
  } catch (error) {
    rows.value = []
    errorMessage.value = error.response?.data?.message || '작업지시 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadWorkOrders)
</script>
