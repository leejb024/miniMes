<template>
  <div class="page-card">
    <div class="page-header">
      <h1>창고/위치 관리</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadWarehouses">조회</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>WAREHOUSE_ID</th>
            <th>WAREHOUSE_NAME</th>
            <th>WAREHOUSE_TYPE</th>
            <th>PARENT_WAREHOUSE_ID</th>
            <th>CRE_ID</th>
            <th>CRE_DT</th>
            <th>MOD_ID</th>
            <th>MOD_DT</th>
            <th>USE_YN</th>
            <th>DEPT_ID</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="warehouses.length === 0">
            <td colspan="10" class="empty">조회된 창고가 없습니다.</td>
          </tr>
          <tr v-for="warehouse in warehouses" v-else :key="warehouse.warehouseId">
            <td>{{ warehouse.warehouseId }}</td>
            <td>{{ warehouse.warehouseName }}</td>
            <td>{{ warehouse.warehouseType }}</td>
            <td>{{ warehouse.parentWarehouseId }}</td>
            <td>{{ warehouse.creId }}</td>
            <td>{{ formatDate(warehouse.creDt) }}</td>
            <td>{{ warehouse.modId }}</td>
            <td>{{ formatDate(warehouse.modDt) }}</td>
            <td>{{ warehouse.useYn }}</td>
            <td>{{ warehouse.deptId }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchWarehouses } from '@/api/warehouse'

const warehouses = ref([])
const loading = ref(false)
const errorMessage = ref('')

function formatDate(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadWarehouses() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchWarehouses()
    warehouses.value = Array.isArray(data) ? data : []
  } catch (error) {
    warehouses.value = []
    errorMessage.value = error.response?.data?.message || '창고 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadWarehouses)
</script>
