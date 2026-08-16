<template>
  <div class="page-card">
    <div class="page-header">
      <h1>거래처관리</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadVendors">조회</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>VENDOR_ID</th>
            <th>USE_YN</th>
            <th>REG_DT</th>
            <th>MOD_DT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="4" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="vendors.length === 0">
            <td colspan="4" class="empty">조회된 거래처가 없습니다.</td>
          </tr>
          <tr v-for="vendor in vendors" v-else :key="vendor.vendorId">
            <td>{{ vendor.vendorId }}</td>
            <td>{{ vendor.useYn }}</td>
            <td>{{ formatDate(vendor.regDt) }}</td>
            <td>{{ formatDate(vendor.modDt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchVendors } from '@/api/vendor'

const vendors = ref([])
const loading = ref(false)
const errorMessage = ref('')

function formatDate(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadVendors() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchVendors()
    vendors.value = Array.isArray(data) ? data : []
  } catch (error) {
    vendors.value = []
    errorMessage.value = error.response?.data?.message || '거래처 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadVendors)
</script>
