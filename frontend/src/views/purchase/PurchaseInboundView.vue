<template>
  <div class="page-card">
    <div class="page-header">
      <h1>입고등록</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" @click="openOrder">발주</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadOrders()">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="master-detail">
      <section class="grid-section">
        <div class="section-head">
          <h2>구매 발주 리스트</h2>
          <span class="result-count">검색결과 : {{ orderLines.length }}건</span>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>No.</th>
                <th>구분</th>
                <th>발주일</th>
                <th>발주번호</th>
                <th>품목명</th>
                <th>발주수량</th>
                <th>납품 예정일</th>
                <th>비고</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="8" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="orders.length === 0">
                <td colspan="8" class="empty">조회된 발주가 없습니다.</td>
              </tr>
              <tr
                v-for="(line, index) in orderLines"
                v-else
                :key="`${line.order.poNo}-${line.item?.poItemSeq || index}`"
                :class="{ selected: selectedPoNo === line.order.poNo, clickable: true }"
                @click="selectOrder(line.order)"
              >
                <td>{{ index + 1 }}</td>
                <td>{{ line.order.poType }}</td>
                <td>{{ line.order.poDate }}</td>
                <td>{{ line.order.poNo }}</td>
                <td>{{ line.item?.itemName }}</td>
                <td>{{ formatQty(line.item?.qty) }}</td>
                <td>{{ line.item?.dueDate || line.order.dueDate }}</td>
                <td>{{ line.order.remark }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="grid-section">
        <p v-if="selectedItems.length" class="scan-summary">
          발주 자재:
          <span v-for="(item, index) in selectedItems" :key="item.poItemSeq || index">
            {{ index > 0 ? ', ' : '' }}{{ item.itemId }} {{ item.itemName }}
          </span>
        </p>
        <div class="section-head">
          <h2>입고 LOT</h2>
          <div class="section-actions">
            <button type="button" class="search-btn" :disabled="!selectedOrder" @click="openScan">스캔</button>
            <button type="button" class="ghost-btn" :disabled="!selectedLot" @click="openQtyEdit">수량수정</button>
            <button type="button" class="danger-btn" :disabled="!selectedLot || deleting" @click="onDeleteQty">수량삭제</button>
          </div>
        </div>
        <p v-if="scanSummary" class="scan-summary">최근 스캔: {{ scanSummary }}</p>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>No.</th>
                <th>LOT</th>
                <th>자재</th>
                <th>입고수량</th>
                <th>반품</th>
                <th>잔량</th>
                <th>창고</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="lotsLoading">
                <td colspan="7" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="!selectedOrder">
                <td colspan="7" class="empty">좌측 발주를 선택하세요.</td>
              </tr>
              <tr v-else-if="lots.length === 0">
                <td colspan="7" class="empty">입고된 LOT가 없습니다.</td>
              </tr>
              <tr
                v-for="(lot, index) in lots"
                v-else
                :key="lot.lotSeq"
                :class="{ selected: selectedLotSeq === lot.lotSeq, clickable: true }"
                @click="selectedLotSeq = lot.lotSeq"
              >
                <td>{{ index + 1 }}</td>
                <td>{{ lot.lotNo }}</td>
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
    </div>

    <div v-if="showOrder" class="modal-mask" @click.self="closeOrder">
      <div class="modal-card">
        <div class="modal-head">
          <h2>발주</h2>
          <button type="button" class="ghost-btn" @click="closeOrder">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            거래처
            <select v-model="orderForm.vendorId">
              <option value="">거래처를 선택하세요</option>
              <option v-for="vendor in vendors" :key="vendor.vendorId" :value="vendor.vendorId">
                {{ vendor.vendorId }}
              </option>
            </select>
          </label>
          <label>
            납품예정일
            <input v-model="orderForm.dueDate" type="date" />
          </label>
        </div>
        <div class="section-head">
          <h2>품목</h2>
          <button type="button" class="search-btn" @click="addOrderItem">품목 추가</button>
        </div>
        <div class="table-wrap">
          <table class="data-table form-table">
            <thead>
              <tr>
                <th>품목</th>
                <th>품명</th>
                <th>발주 수량</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="orderForm.items.length === 0">
                <td colspan="4" class="empty">품목을 추가하세요.</td>
              </tr>
              <tr v-for="(row, index) in orderForm.items" :key="index">
                <td>
                  <select v-model="row.itemId">
                    <option value="">품목을 선택하세요</option>
                    <option v-for="item in itemOptionsFor(row)" :key="item.itemId" :value="item.itemId">
                      {{ item.itemId }} {{ item.itemName }}
                    </option>
                  </select>
                </td>
                <td>{{ itemNameOf(row.itemId) }}</td>
                <td>
                  <input v-model="row.qty" type="number" min="0" step="0.0001" />
                </td>
                <td>
                  <button type="button" class="danger-btn" @click="removeOrderItem(index)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeOrder">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveOrder">확인</button>
        </div>
      </div>
    </div>

    <div v-if="showScan" class="modal-mask" @click.self="closeScan">
      <div class="modal-card">
        <div class="modal-head">
          <h2>바코드 스캔</h2>
          <button type="button" class="ghost-btn" @click="closeScan">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            바코드
            <input
              ref="barcodeInput"
              v-model.trim="barcode"
              type="text"
              placeholder="발주번호, 자재ID, 발주번호|자재ID"
              @keyup.enter="saveScan"
            />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeScan">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveScan">스캔 저장</button>
        </div>
      </div>
    </div>

    <div v-if="showLotForm" class="modal-mask" @click.self="closeLotForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>일반 LOT 생성</h2>
          <button type="button" class="ghost-btn" @click="closeLotForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            자재
            <input :value="scan ? `${scan.itemId} ${scan.itemName}` : ''" type="text" readonly />
          </label>
          <label>
            LOT 번호
            <input v-model.trim="lotForm.lotNo" type="text" placeholder="저장 시 자동 채번" />
          </label>
          <label>
            입고 수량
            <input v-model="lotForm.qty" type="number" min="0" step="0.0001" />
          </label>
          <label>
            창고
            <select v-model="lotForm.warehouseId">
              <option value="">창고를 선택하세요</option>
              <option v-for="warehouse in warehouses" :key="warehouse.warehouseId" :value="warehouse.warehouseId">
                {{ warehouse.warehouseId }} {{ warehouse.warehouseName }}
              </option>
            </select>
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeLotForm">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveLot">저장</button>
        </div>
      </div>
    </div>

    <div v-if="showQtyForm" class="modal-mask" @click.self="closeQtyForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>입고 수량 수정</h2>
          <button type="button" class="ghost-btn" @click="closeQtyForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            LOT
            <input :value="selectedLot?.lotNo || ''" type="text" readonly />
          </label>
          <label>
            입고 수량
            <input v-model="qtyForm" type="number" min="0" step="0.0001" />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeQtyForm">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveQty">저장</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { fetchItems } from '@/api/item'
import { fetchVendors } from '@/api/vendor'
import { fetchWarehouses } from '@/api/warehouse'
import { createPurchaseOrder, fetchPurchaseOrders } from '@/api/purchaseOrder'
import {
  createPurchaseLot,
  deletePurchaseQty,
  fetchPurchaseLots,
  scanPurchaseInfo,
  updatePurchaseQty
} from '@/api/purchase'

const orders = ref([])
const lots = ref([])
const warehouses = ref([])
const vendors = ref([])
const purchaseItems = ref([])
const selectedPoNo = ref('')
const selectedLotSeq = ref(null)
const scan = ref(null)
const barcode = ref('')
const barcodeInput = ref(null)
const loading = ref(false)
const lotsLoading = ref(false)
const deleting = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showOrder = ref(false)
const showScan = ref(false)
const showLotForm = ref(false)
const showQtyForm = ref(false)
const qtyForm = ref('')
const lotForm = reactive({
  lotNo: '',
  qty: '',
  warehouseId: ''
})
const orderForm = reactive({
  vendorId: '',
  dueDate: '',
  items: []
})

const orderLines = computed(() =>
  orders.value.flatMap((order) => {
    const items = order.items?.length ? order.items : [null]
    return items.map((item) => ({ order, item }))
  })
)
const selectedOrder = computed(() => orders.value.find((order) => order.poNo === selectedPoNo.value) || null)
const selectedItems = computed(() => selectedOrder.value?.items || [])
const selectedLot = computed(() => lots.value.find((lot) => lot.lotSeq === selectedLotSeq.value) || null)
const scanSummary = computed(() => {
  if (!scan.value) {
    return ''
  }
  return `${scan.value.barcode} / ${scan.value.itemId} ${scan.value.itemName}`
})

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

function today() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function itemOptionsFor(row) {
  const selectedIds = orderForm.items
    .filter((item) => item !== row && item.itemId)
    .map((item) => item.itemId)
  return purchaseItems.value.filter((item) => item.itemId === row.itemId || !selectedIds.includes(item.itemId))
}

function itemNameOf(itemId) {
  return purchaseItems.value.find((item) => item.itemId === itemId)?.itemName || ''
}

function addOrderItem() {
  orderForm.items.push({ itemId: '', qty: '' })
}

function removeOrderItem(index) {
  orderForm.items.splice(index, 1)
}

async function openOrder() {
  formError.value = ''
  orderForm.vendorId = vendors.value.length === 1 ? vendors.value[0].vendorId : ''
  orderForm.dueDate = today()
  orderForm.items = [{ itemId: '', qty: '' }]
  showOrder.value = true
  if (vendors.value.length === 0 || purchaseItems.value.length === 0) {
    await loadOrderMasters()
    if (vendors.value.length === 1) {
      orderForm.vendorId = vendors.value[0].vendorId
    }
  }
}

function closeOrder() {
  showOrder.value = false
  formError.value = ''
}

async function saveOrder() {
  if (!orderForm.vendorId) {
    formError.value = '거래처를 선택하세요.'
    return
  }
  if (!orderForm.dueDate) {
    formError.value = '납품예정일을 선택하세요.'
    return
  }
  if (orderForm.items.length === 0 || orderForm.items.some((row) => !row.itemId)) {
    formError.value = '품목을 선택하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    const { data } = await createPurchaseOrder({
      poType: '구매입고',
      poDate: today(),
      vendorId: orderForm.vendorId,
      managerName: '관리자',
      dueDate: orderForm.dueDate,
      items: orderForm.items.map((row) => ({
        itemId: row.itemId,
        qty: row.qty === '' ? null : Number(row.qty),
        unit: 'Kg',
        dueDate: orderForm.dueDate
      }))
    })
    showOrder.value = false
    await loadOrders(data?.poNo)
  } catch (error) {
    formError.value = error.response?.data?.message || '발주 등록에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

function selectOrder(order) {
  selectedPoNo.value = order.poNo
  selectedLotSeq.value = null
  scan.value = null
  loadLots()
}

function openScan() {
  if (!selectedOrder.value) {
    errorMessage.value = '발주를 선택하세요.'
    return
  }
  formError.value = ''
  barcode.value = ''
  showScan.value = true
  nextTick(() => barcodeInput.value?.focus())
}

function closeScan() {
  showScan.value = false
  formError.value = ''
}

function closeLotForm() {
  showLotForm.value = false
  formError.value = ''
}

function openQtyEdit() {
  if (!selectedLot.value) {
    errorMessage.value = '수정할 입고 LOT를 선택하세요.'
    return
  }
  formError.value = ''
  qtyForm.value = selectedLot.value.qty ?? ''
  showQtyForm.value = true
}

function closeQtyForm() {
  showQtyForm.value = false
  formError.value = ''
}

async function saveScan() {
  if (!barcode.value) {
    formError.value = '바코드를 입력하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    const { data } = await scanPurchaseInfo({
      barcode: barcode.value,
      poNo: selectedPoNo.value
    })
    scan.value = data
    showScan.value = false
    lotForm.lotNo = ''
    lotForm.qty = ''
    lotForm.warehouseId = warehouses.value[0]?.warehouseId || ''
    showLotForm.value = true
  } catch (error) {
    formError.value = error.response?.data?.message || '바코드 스캔 저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

function dueDateOfScan() {
  const order = selectedOrder.value
  const item = order?.items?.find((row) => row.poItemSeq === scan.value?.poItemSeq)
    || order?.items?.find((row) => row.itemId === scan.value?.itemId)
  return item?.dueDate || order?.dueDate || ''
}

async function saveLot() {
  if (!scan.value) {
    formError.value = '스캔 정보가 없습니다.'
    return
  }
  const dueDate = dueDateOfScan()
  if (!dueDate || dueDate > today()) {
    formError.value = '납품예정일이 오늘 이전인 발주만 입고할 수 있습니다.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await createPurchaseLot({
      scanSeq: scan.value.scanSeq,
      qty: lotForm.qty === '' ? null : Number(lotForm.qty),
      lotNo: lotForm.lotNo || null,
      warehouseId: lotForm.warehouseId || null
    })
    showLotForm.value = false
    scan.value = null
    await loadLots()
  } catch (error) {
    formError.value = error.response?.data?.message || 'LOT 생성에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function saveQty() {
  if (!selectedLot.value) {
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await updatePurchaseQty({
      lotSeq: selectedLot.value.lotSeq,
      qty: qtyForm.value === '' ? null : Number(qtyForm.value)
    })
    showQtyForm.value = false
    await loadLots()
  } catch (error) {
    formError.value = error.response?.data?.message || '입고 수량 수정에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function onDeleteQty() {
  if (!selectedLot.value) {
    errorMessage.value = '삭제할 입고 LOT를 선택하세요.'
    return
  }
  if (!window.confirm(`입고 LOT ${selectedLot.value.lotNo} 수량을 삭제하시겠습니까?`)) {
    return
  }
  deleting.value = true
  errorMessage.value = ''
  try {
    await deletePurchaseQty(selectedLot.value.lotSeq)
    selectedLotSeq.value = null
    await loadLots()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '입고 수량 삭제에 실패했습니다.'
  } finally {
    deleting.value = false
  }
}

async function loadWarehouses() {
  try {
    const { data } = await fetchWarehouses()
    warehouses.value = (Array.isArray(data) ? data : []).filter(
      (warehouse) => !warehouse.useYn || String(warehouse.useYn).toUpperCase() === 'Y'
    )
  } catch (error) {
    warehouses.value = []
    errorMessage.value = error.response?.data?.message || '창고 조회에 실패했습니다.'
  }
}

async function loadOrderMasters() {
  try {
    const [vendorResult, itemResult] = await Promise.all([fetchVendors(), fetchItems()])
    vendors.value = (Array.isArray(vendorResult.data) ? vendorResult.data : []).filter(
      (vendor) => !vendor.useYn || String(vendor.useYn).toUpperCase() === 'Y'
    )
    purchaseItems.value = (Array.isArray(itemResult.data) ? itemResult.data : []).filter(
      (item) => String(item.itemId || '').startsWith('1') && (!item.useYn || String(item.useYn).toUpperCase() === 'Y')
    )
  } catch (error) {
    vendors.value = []
    purchaseItems.value = []
    formError.value = error.response?.data?.message || '거래처 또는 품목 조회에 실패했습니다.'
  }
}

async function loadOrders(preferPoNo) {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchPurchaseOrders()
    orders.value = Array.isArray(data) ? data : []
    const keep = preferPoNo || selectedPoNo.value
    selectedPoNo.value = orders.value.some((order) => order.poNo === keep)
      ? keep
      : (orders.value[0]?.poNo || '')
    await loadLots()
  } catch (error) {
    orders.value = []
    selectedPoNo.value = ''
    lots.value = []
    errorMessage.value = error.response?.data?.message || '발주 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function loadLots() {
  if (!selectedPoNo.value) {
    lots.value = []
    selectedLotSeq.value = null
    return
  }
  lotsLoading.value = true
  try {
    const { data } = await fetchPurchaseLots({ lotType: 'INBOUND', poNo: selectedPoNo.value })
    lots.value = Array.isArray(data) ? data : []
    if (!lots.value.some((lot) => lot.lotSeq === selectedLotSeq.value)) {
      selectedLotSeq.value = lots.value[0]?.lotSeq ?? null
    }
  } catch (error) {
    lots.value = []
    selectedLotSeq.value = null
    errorMessage.value = error.response?.data?.message || '입고 LOT 조회에 실패했습니다.'
  } finally {
    lotsLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadWarehouses(), loadOrderMasters()])
  await loadOrders()
})
</script>

<style scoped>
.scan-summary {
  margin: 0 0 8px;
  font-size: 13px;
  color: #606266;
}
</style>
