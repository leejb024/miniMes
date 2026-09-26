<template>
  <div class="page-card">
    <div class="page-header">
      <h1>자재인계</h1>
      <div class="section-actions toolbar-row">
        <button type="button" class="search-btn" @click="openCreate">불출요청</button>
        <button type="button" class="search-btn" :disabled="!isStatus('REQUEST') || acting" @click="onMoveRequest">이동요청</button>
        <button type="button" class="search-btn" :disabled="!isStatus('MOVE_REQUEST')" @click="openPick">피킹오더</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadOrders">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p class="hint">불출 요청, 이동 요청, 승인된 피킹 오더까지 끝나면 자재 인수에서 조회됩니다.</p>

    <div class="master-detail">
      <section class="grid-section">
        <div class="section-head">
          <h2>불출 요청</h2>
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
                <td colspan="8" class="empty">불출 요청이 없습니다.</td>
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
      </section>

      <section class="grid-section">
        <div class="section-head">
          <h2>처리 내역</h2>
          <span class="result-count">검색결과 : {{ selectedOrder?.moves?.length || 0 }}건</span>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>No.</th>
                <th>구분</th>
                <th>수량</th>
                <th>비고</th>
                <th>등록일시</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!selectedOrder">
                <td colspan="5" class="empty">요청을 선택하세요.</td>
              </tr>
              <tr v-else-if="!selectedOrder.moves?.length">
                <td colspan="5" class="empty">처리 내역이 없습니다.</td>
              </tr>
              <tr v-for="(move, index) in selectedOrder.moves" v-else :key="move.moveSeq">
                <td>{{ index + 1 }}</td>
                <td>{{ moveLabel(move.moveType) }}</td>
                <td>{{ formatQty(move.qty) }}</td>
                <td>{{ move.remark }}</td>
                <td>{{ formatDateTime(move.creDt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
      <div class="modal-card">
        <div class="modal-head">
          <h2>자재 불출 요청</h2>
          <button type="button" class="ghost-btn" @click="showCreate = false">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            출고 재고
            <select v-model="form.stockSeq">
              <option value="">재고를 선택하세요</option>
              <option v-for="stock in stocks" :key="stock.stockSeq" :value="String(stock.stockSeq)">
                {{ stock.itemName }} / {{ stock.lotNo }} / {{ formatQty(stock.qty) }} {{ stock.unit }}
              </option>
            </select>
          </label>
          <label>
            입고 창고
            <select v-model="form.toWarehouseId">
              <option value="">창고를 선택하세요</option>
              <option v-for="warehouse in warehouses" :key="warehouse.warehouseId" :value="warehouse.warehouseId">
                {{ warehouse.warehouseName }}
              </option>
            </select>
          </label>
          <label>
            요청 수량
            <input v-model="form.qty" type="number" min="0" step="0.0001" />
          </label>
          <label>
            비고
            <input v-model.trim="form.remark" type="text" />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="acting" @click="showCreate = false">취소</button>
          <button type="button" class="search-btn" :disabled="acting" @click="saveCreate">확인</button>
        </div>
      </div>
    </div>

    <div v-if="showPick" class="modal-mask" @click.self="showPick = false">
      <div class="modal-card">
        <div class="modal-head">
          <h2>피킹 오더</h2>
          <button type="button" class="ghost-btn" @click="showPick = false">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            승인
            <select v-model="form.approvedYn">
              <option value="Y">승인</option>
              <option value="N">미승인</option>
            </select>
          </label>
          <label>
            피킹 오더
            <input v-model.trim="form.pickNo" type="text" />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="acting" @click="showPick = false">취소</button>
          <button type="button" class="search-btn" :disabled="acting" @click="savePick">확인</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createMaterialOrder,
  fetchMaterialOrders,
  registerPickOrder,
  requestMaterialMove
} from '@/api/material'
import { fetchStocks } from '@/api/stock'
import { fetchWarehouses } from '@/api/warehouse'

const orders = ref([])
const stocks = ref([])
const warehouses = ref([])
const selectedNo = ref('')
const loading = ref(false)
const acting = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showCreate = ref(false)
const showPick = ref(false)
const form = reactive(emptyForm())

const selectedOrder = computed(() => orders.value.find((order) => order.orderNo === selectedNo.value) || null)

function emptyForm() {
  return { stockSeq: '', toWarehouseId: '', qty: '', remark: '', approvedYn: 'Y', pickNo: '' }
}

function statusLabel(status) {
  return {
    REQUEST: '요청',
    MOVE_REQUEST: '이동요청',
    MOVING: '이동중',
    MOVE_INFO: '이동정보',
    IN_MOVE: '인계',
    DISPATCHED: '불출',
    MOVED: '이동완료',
    WEIGHED: '계량',
    INPUT: '투입',
    COMBINED: '조합',
    LOT_CHANGED: 'LOT변경'
  }[status] || status
}

function moveLabel(type) {
  return {
    MATERIAL_ORDER: '불출요청',
    MOVE_REQUEST: '이동요청',
    PICK_ORDER: '피킹오더',
    MOVE_INFO: '이동정보',
    IN_MOVE_INFO: '입고이동'
  }[type] || type
}

function formatQty(value) {
  if (value === null || value === undefined || value === '') {
    return ''
  }
  const number = Number(value)
  return Number.isNaN(number) ? value : number.toLocaleString('ko-KR')
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

function isStatus(status) {
  return selectedOrder.value?.status === status
}

async function loadOrders() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchMaterialOrders()
    orders.value = Array.isArray(data) ? data : []
    if (selectedNo.value && !orders.value.some((order) => order.orderNo === selectedNo.value)) {
      selectedNo.value = ''
    }
  } catch (error) {
    orders.value = []
    errorMessage.value = error.response?.data?.message || '불출 요청 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function openCreate() {
  Object.assign(form, emptyForm())
  formError.value = ''
  showCreate.value = true
  try {
    const [stockResult, warehouseResult] = await Promise.all([fetchStocks(), fetchWarehouses()])
    stocks.value = Array.isArray(stockResult.data) ? stockResult.data : []
    warehouses.value = (Array.isArray(warehouseResult.data) ? warehouseResult.data : [])
      .filter((warehouse) => !warehouse.useYn || String(warehouse.useYn).toUpperCase() === 'Y')
  } catch (error) {
    formError.value = error.response?.data?.message || '기준정보 조회에 실패했습니다.'
  }
}

async function saveCreate() {
  if (!form.stockSeq || !form.toWarehouseId || !form.qty || Number(form.qty) <= 0) {
    formError.value = '재고, 입고 창고, 요청 수량을 입력하세요.'
    return
  }
  acting.value = true
  formError.value = ''
  try {
    const { data } = await createMaterialOrder({
      stockSeq: Number(form.stockSeq),
      qty: Number(form.qty),
      toWarehouseId: form.toWarehouseId,
      remark: form.remark
    })
    showCreate.value = false
    selectedNo.value = data?.orderNo || selectedNo.value
    await loadOrders()
  } catch (error) {
    formError.value = error.response?.data?.message || '불출 요청에 실패했습니다.'
  } finally {
    acting.value = false
  }
}

async function onMoveRequest() {
  if (!selectedOrder.value) {
    return
  }
  acting.value = true
  errorMessage.value = ''
  try {
    await requestMaterialMove(selectedOrder.value.orderNo)
    await loadOrders()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '이동 요청에 실패했습니다.'
  } finally {
    acting.value = false
  }
}

function openPick() {
  form.approvedYn = 'Y'
  form.pickNo = ''
  formError.value = ''
  showPick.value = true
}

async function savePick() {
  acting.value = true
  formError.value = ''
  try {
    await registerPickOrder({
      orderNo: selectedOrder.value.orderNo,
      approvedYn: form.approvedYn,
      pickNo: form.pickNo
    })
    showPick.value = false
    await loadOrders()
  } catch (error) {
    formError.value = error.response?.data?.message || '피킹 오더 등록에 실패했습니다.'
  } finally {
    acting.value = false
  }
}

onMounted(loadOrders)
</script>
