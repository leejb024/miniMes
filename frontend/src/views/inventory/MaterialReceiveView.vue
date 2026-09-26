<template>
  <div class="page-card">
    <div class="page-header">
      <h1>자재 인수</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" :disabled="!canReceive || acting" @click="onConfirm">인수확정</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadOrders">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p class="hint">피킹이 끝난 요청을 인수확정하면 재고가 입고 창고로 이동합니다.</p>

    <div class="section-head">
      <h2>인수 대상</h2>
      <span class="result-count">검색결과 : {{ orders.length }}건</span>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>No.</th>
            <th>요청번호</th>
            <th>상태</th>
            <th>품명</th>
            <th>LOT</th>
            <th>수량</th>
            <th>출고창고</th>
            <th>입고창고</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="orders.length === 0">
            <td colspan="8" class="empty">인수할 요청이 없습니다.</td>
          </tr>
          <tr
            v-for="(order, index) in orders"
            v-else
            :key="order.orderNo"
            :class="{ selected: selectedNo === order.orderNo, clickable: true }"
            @click="selectedNo = order.orderNo"
          >
            <td>{{ index + 1 }}</td>
            <td>{{ order.orderNo }}</td>
            <td>{{ statusLabel(order.status) }}</td>
            <td>{{ order.itemName }}</td>
            <td>{{ order.lotNo }}</td>
            <td>{{ formatQty(order.qty) }}</td>
            <td>{{ order.fromWarehouseName }}</td>
            <td>{{ order.toWarehouseName }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { confirmMaterialReceive, fetchMaterialOrders } from '@/api/material'

const RECEIPT_STATUSES = ['MOVING', 'MOVE_INFO', 'IN_MOVE', 'DISPATCHED', 'RECEIVED', 'MOVED']

const orders = ref([])
const selectedNo = ref('')
const loading = ref(false)
const acting = ref(false)
const errorMessage = ref('')

const selectedOrder = computed(() => orders.value.find((order) => order.orderNo === selectedNo.value) || null)
const canReceive = computed(() => ['MOVING', 'MOVE_INFO', 'IN_MOVE', 'DISPATCHED'].includes(selectedOrder.value?.status))

function statusLabel(status) {
  return {
    MOVING: '이동중',
    MOVE_INFO: '이동정보',
    IN_MOVE: '인계',
    DISPATCHED: '불출',
    RECEIVED: '인수확정',
    MOVED: '인수확정'
  }[status] || status
}

function formatQty(value) {
  if (value === null || value === undefined || value === '') {
    return ''
  }
  const number = Number(value)
  return Number.isNaN(number) ? value : number.toLocaleString('ko-KR')
}

async function loadOrders() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchMaterialOrders()
    const rows = Array.isArray(data) ? data : []
    orders.value = rows.filter((order) => RECEIPT_STATUSES.includes(order.status))
    if (selectedNo.value && !orders.value.some((order) => order.orderNo === selectedNo.value)) {
      selectedNo.value = ''
    }
  } catch (error) {
    orders.value = []
    errorMessage.value = error.response?.data?.message || '인수 대상 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function onConfirm() {
  if (!selectedOrder.value) {
    return
  }
  const order = selectedOrder.value
  if (!window.confirm(`${order.orderNo} 재고 ${order.qty}을 ${order.toWarehouseName}로 인수확정하시겠습니까?`)) {
    return
  }
  acting.value = true
  errorMessage.value = ''
  try {
    await confirmMaterialReceive(order.orderNo)
    await loadOrders()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '인수확정에 실패했습니다.'
  } finally {
    acting.value = false
  }
}

onMounted(loadOrders)
</script>
