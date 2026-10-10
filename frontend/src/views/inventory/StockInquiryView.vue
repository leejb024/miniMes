<template>
  <div class="page-card">
    <div class="page-header">
      <h1>재고조회</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" :disabled="loading" @click="loadStocks">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="section-head">
      <h2>재고 리스트</h2>
      <span class="result-count">검색결과 : {{ stocks.length }}건</span>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>창고ID</th>
            <th>창고명</th>
            <th>품번</th>
            <th>품명</th>
            <th>LOT NO</th>
            <th>단위</th>
            <th>재고수량</th>
            <th>위치</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="stocks.length === 0">
            <td colspan="8" class="empty">조회된 재고가 없습니다.</td>
          </tr>
          <tr v-for="stock in stocks" v-else :key="stock.stockSeq">
            <td>{{ stock.warehouseId }}</td>
            <td>{{ stock.warehouseName }}</td>
            <td>{{ stock.itemId }}</td>
            <td>{{ stock.itemName }}</td>
            <td>{{ stock.lotNo }}</td>
            <td>{{ stock.unit }}</td>
            <td>{{ formatQty(stock.qty) }}</td>
            <td>{{ stock.locationName }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchStocks } from '@/api/stock'

const stocks = ref([])
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

async function loadStocks() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchStocks()
    stocks.value = Array.isArray(data) ? data : []
  } catch (error) {
    stocks.value = []
    errorMessage.value = error.response?.data?.message || '재고 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadStocks)
</script>
