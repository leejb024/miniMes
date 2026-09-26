<template>
  <div class="page-card">
    <div class="page-header">
      <h1>반품관리</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadInboundLots">조회</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="master-detail">
      <section class="grid-section">
        <div class="section-head">
          <h2>입고 LOT</h2>
          <div class="section-actions">
            <span class="result-count">검색결과 : {{ inboundLots.length }}건</span>
            <button type="button" class="search-btn" :disabled="!selectedInbound" @click="openReturn">반품등록</button>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>No.</th>
                <th>LOT</th>
                <th>발주번호</th>
                <th>자재</th>
                <th>입고수량</th>
                <th>반품</th>
                <th>잔량</th>
                <th>창고</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="8" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="inboundLots.length === 0">
                <td colspan="8" class="empty">입고된 LOT가 없습니다.</td>
              </tr>
              <tr
                v-for="(lot, index) in inboundLots"
                v-else
                :key="lot.lotSeq"
                :class="{ selected: selectedLotNo === lot.lotNo, clickable: true }"
                @click="selectInbound(lot)"
              >
                <td>{{ index + 1 }}</td>
                <td>{{ lot.lotNo }}</td>
                <td>{{ lot.poNo }}</td>
                <td>{{ lot.itemName }}</td>
                <td>{{ formatQty(lot.qty) }}</td>
                <td>{{ formatQty(lot.returnedQty) }}</td>
                <td>{{ formatQty(lot.remainQty) }}</td>
                <td>{{ lot.warehouseName }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="grid-section">
        <div class="section-head">
          <h2>반품 LOT</h2>
          <button
            type="button"
            class="danger-btn"
            :disabled="!selectedReturn || deleting"
            @click="onCancelReturn"
          >
            삭제/취소
          </button>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>No.</th>
                <th>반품 LOT</th>
                <th>반품수량</th>
                <th>단위</th>
                <th>비고</th>
                <th>등록일시</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="returnsLoading">
                <td colspan="6" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="!selectedInbound">
                <td colspan="6" class="empty">좌측 입고 LOT를 선택하세요.</td>
              </tr>
              <tr v-else-if="returns.length === 0">
                <td colspan="6" class="empty">반품 내역이 없습니다.</td>
              </tr>
              <tr
                v-for="(lot, index) in returns"
                v-else
                :key="lot.lotSeq"
                :class="{ selected: selectedReturnSeq === lot.lotSeq, clickable: true }"
                @click="selectedReturnSeq = lot.lotSeq"
              >
                <td>{{ index + 1 }}</td>
                <td>{{ lot.lotNo }}</td>
                <td>{{ formatQty(lot.qty) }}</td>
                <td>{{ lot.unit }}</td>
                <td>{{ lot.remark }}</td>
                <td>{{ formatDateTime(lot.creDt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>반품 LOT 저장</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            입고 LOT
            <input :value="selectedInbound?.lotNo || ''" type="text" readonly />
          </label>
          <label>
            잔량
            <input :value="formatQty(selectedInbound?.remainQty)" type="text" readonly />
          </label>
          <label>
            반품 수량
            <input v-model="form.qty" type="number" min="0" step="0.0001" />
          </label>
          <label>
            비고
            <input v-model.trim="form.remark" type="text" />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeForm">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveReturn">저장</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteReturnQty, fetchPurchaseLots, returnPurchaseLot } from '@/api/purchase'

const inboundLots = ref([])
const returns = ref([])
const selectedLotNo = ref('')
const selectedReturnSeq = ref(null)
const loading = ref(false)
const returnsLoading = ref(false)
const deleting = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const form = reactive({
  qty: '',
  remark: ''
})

const selectedInbound = computed(() => inboundLots.value.find((lot) => lot.lotNo === selectedLotNo.value) || null)
const selectedReturn = computed(() => returns.value.find((lot) => lot.lotSeq === selectedReturnSeq.value) || null)

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

function selectInbound(lot) {
  selectedLotNo.value = lot.lotNo
  selectedReturnSeq.value = null
  loadReturns()
}

function openReturn() {
  if (!selectedInbound.value) {
    errorMessage.value = '반품할 입고 LOT를 선택하세요.'
    return
  }
  formError.value = ''
  form.qty = ''
  form.remark = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  formError.value = ''
}

async function saveReturn() {
  if (!selectedInbound.value) {
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await returnPurchaseLot({
      lotSeq: selectedInbound.value.lotSeq,
      qty: form.qty === '' ? null : Number(form.qty),
      remark: form.remark || null
    })
    showForm.value = false
    await loadInboundLots()
  } catch (error) {
    formError.value = error.response?.data?.message || '반품 저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function onCancelReturn() {
  if (!selectedReturn.value) {
    errorMessage.value = '취소할 반품을 선택하세요.'
    return
  }
  if (!window.confirm(`반품 LOT ${selectedReturn.value.lotNo}를 취소하시겠습니까?`)) {
    return
  }
  deleting.value = true
  errorMessage.value = ''
  try {
    await deleteReturnQty(selectedReturn.value.lotSeq)
    selectedReturnSeq.value = null
    await loadInboundLots()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '반품 취소에 실패했습니다.'
  } finally {
    deleting.value = false
  }
}

async function loadInboundLots() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchPurchaseLots({ lotType: 'INBOUND' })
    inboundLots.value = Array.isArray(data) ? data : []
    const keep = selectedLotNo.value
    selectedLotNo.value = inboundLots.value.some((lot) => lot.lotNo === keep)
      ? keep
      : (inboundLots.value[0]?.lotNo || '')
    await loadReturns()
  } catch (error) {
    inboundLots.value = []
    selectedLotNo.value = ''
    returns.value = []
    errorMessage.value = error.response?.data?.message || '입고 LOT 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function loadReturns() {
  if (!selectedLotNo.value) {
    returns.value = []
    selectedReturnSeq.value = null
    return
  }
  returnsLoading.value = true
  try {
    const { data } = await fetchPurchaseLots({ lotType: 'RETURN', sourceLotNo: selectedLotNo.value })
    returns.value = Array.isArray(data) ? data : []
    if (!returns.value.some((lot) => lot.lotSeq === selectedReturnSeq.value)) {
      selectedReturnSeq.value = returns.value[0]?.lotSeq ?? null
    }
  } catch (error) {
    returns.value = []
    selectedReturnSeq.value = null
    errorMessage.value = error.response?.data?.message || '반품 조회에 실패했습니다.'
  } finally {
    returnsLoading.value = false
  }
}

onMounted(loadInboundLots)
</script>
