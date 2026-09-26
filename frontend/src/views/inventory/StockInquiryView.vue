<template>
  <div class="page-card">
    <div class="page-header">
      <h1>재고조회</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" :disabled="!selectedStock" @click="openCarry">이월/반입</button>
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
          <tr
            v-for="stock in stocks"
            v-else
            :key="stock.stockSeq"
            :class="{ selected: selectedSeq === stock.stockSeq, clickable: true }"
            @click="selectStock(stock)"
          >
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

    <div class="section-head spaced">
      <h2>이월/반입 내역</h2>
      <span class="result-count">검색결과 : {{ carries.length }}건</span>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>No.</th>
            <th>구분</th>
            <th>기준일자</th>
            <th>창고</th>
            <th>품명</th>
            <th>LOT NO</th>
            <th>수량</th>
            <th>비고</th>
            <th>등록일시</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="carryLoading">
            <td colspan="9" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="!selectedStock">
            <td colspan="9" class="empty">재고를 선택하세요.</td>
          </tr>
          <tr v-else-if="carries.length === 0">
            <td colspan="9" class="empty">이월 또는 반입 내역이 없습니다.</td>
          </tr>
          <tr v-for="(carry, index) in carries" v-else :key="carry.carrySeq">
            <td>{{ index + 1 }}</td>
            <td>{{ carryLabel(carry.carryType) }}</td>
            <td>{{ carry.baseDate }}</td>
            <td>{{ carry.warehouseName }}</td>
            <td>{{ carry.itemName }}</td>
            <td>{{ carry.lotNo }}</td>
            <td>{{ formatQty(carry.qty) }}</td>
            <td>{{ carry.remark }}</td>
            <td>{{ formatDateTime(carry.creDt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>재고 이월/반입</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            LOT NO
            <input :value="selectedStock?.lotNo || ''" type="text" readonly />
          </label>
          <label>
            품명
            <input :value="selectedStock?.itemName || ''" type="text" readonly />
          </label>
          <label>
            재고수량
            <input :value="formatQty(selectedStock?.qty)" type="text" readonly />
          </label>
          <label>
            구분
            <select v-model="form.carryType">
              <option value="CARRY">이월</option>
              <option value="BRING">반입</option>
            </select>
          </label>
          <label>
            기준일자
            <input v-model="form.baseDate" type="date" :max="today()" />
          </label>
          <label>
            비고
            <input v-model.trim="form.remark" type="text" />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeForm">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveCarry">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { carryStock, fetchStockCarries, fetchStocks } from '@/api/stock'

const stocks = ref([])
const carries = ref([])
const selectedSeq = ref(null)
const loading = ref(false)
const carryLoading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const form = reactive({
  carryType: 'CARRY',
  baseDate: '',
  remark: ''
})

const selectedStock = computed(() => stocks.value.find((stock) => stock.stockSeq === selectedSeq.value) || null)

function today() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

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

function formatDateTime(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

function carryLabel(type) {
  return type === 'BRING' ? '반입' : '이월'
}

async function loadStocks() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchStocks()
    stocks.value = Array.isArray(data) ? data : []
    if (selectedSeq.value && !stocks.value.some((stock) => stock.stockSeq === selectedSeq.value)) {
      selectedSeq.value = null
      carries.value = []
    }
  } catch (error) {
    stocks.value = []
    errorMessage.value = error.response?.data?.message || '재고 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function selectStock(stock) {
  selectedSeq.value = stock.stockSeq
  carryLoading.value = true
  try {
    const { data } = await fetchStockCarries(stock.stockSeq)
    carries.value = Array.isArray(data) ? data : []
  } catch (error) {
    carries.value = []
    errorMessage.value = error.response?.data?.message || '이월/반입 조회에 실패했습니다.'
  } finally {
    carryLoading.value = false
  }
}

function openCarry() {
  form.carryType = 'CARRY'
  form.baseDate = today()
  form.remark = ''
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function saveCarry() {
  if (!selectedStock.value) {
    return
  }
  if (!form.baseDate) {
    formError.value = '기준일자를 선택하세요.'
    return
  }
  if (form.baseDate > today()) {
    formError.value = '기준일자는 오늘 이전이어야 합니다.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await carryStock({
      stockSeq: selectedStock.value.stockSeq,
      carryType: form.carryType,
      baseDate: form.baseDate,
      remark: form.remark
    })
    showForm.value = false
    await selectStock(selectedStock.value)
  } catch (error) {
    formError.value = error.response?.data?.message || '이월/반입 처리에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

onMounted(loadStocks)
</script>
