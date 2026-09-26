<template>
  <div class="page-card">
    <div class="page-header">
      <h1>발주관리</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadOrders">조회</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="master-detail">
      <section class="grid-section">
        <div class="section-head">
          <h2>구매 발주 리스트</h2>
          <div class="section-actions">
            <span class="result-count">검색결과 : {{ orders.length }}건</span>
            <button type="button" class="search-btn" @click="openCreate">추가</button>
            <button type="button" class="ghost-btn" :disabled="!selectedOrder" @click="openEdit">수정</button>
            <button type="button" class="danger-btn" :disabled="!selectedOrder || deleting" @click="onDelete">삭제</button>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>No.</th>
                <th>구분</th>
                <th>발주일</th>
                <th>발주번호</th>
                <th>구매거래처</th>
                <th>발주담당자</th>
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
                v-for="(order, index) in orders"
                v-else
                :key="order.poNo"
                :class="{ selected: selectedPoNo === order.poNo, clickable: true }"
                @click="selectOrder(order)"
              >
                <td>{{ index + 1 }}</td>
                <td>{{ order.poType }}</td>
                <td>{{ order.poDate }}</td>
                <td>{{ order.poNo }}</td>
                <td>{{ order.vendorId || order.vendorName }}</td>
                <td>{{ order.managerName }}</td>
                <td>{{ order.dueDate }}</td>
                <td>{{ order.remark }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="grid-section">
        <div class="section-head">
          <h2>발주 자재</h2>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>No.</th>
                <th>자재 ID</th>
                <th>자재 명</th>
                <th>발주 수량</th>
                <th>구매단위</th>
                <th>납품 예정일</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="6" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="!selectedOrder">
                <td colspan="6" class="empty">좌측 발주를 선택하세요.</td>
              </tr>
              <tr v-else-if="selectedItems.length === 0">
                <td colspan="6" class="empty">조회된 발주 자재가 없습니다.</td>
              </tr>
              <tr v-for="(item, index) in selectedItems" v-else :key="item.poItemSeq || index">
                <td>{{ index + 1 }}</td>
                <td>{{ item.itemId }}</td>
                <td>{{ item.itemName }}</td>
                <td>{{ formatQty(item.qty) }}</td>
                <td>{{ item.unit }}</td>
                <td>{{ item.dueDate }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>{{ isEdit ? '발주 수정' : '발주 추가' }}</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>

        <p v-if="formError" class="error">{{ formError }}</p>

        <div class="form-grid">
          <label>
            구분
            <input v-model.trim="form.poType" type="text" placeholder="구매입고" />
          </label>
          <label>
            발주번호
            <input
              v-model.trim="form.poNo"
              type="text"
              :readonly="isEdit"
              :placeholder="isEdit ? '' : '저장 시 자동 채번'"
            />
          </label>
          <label>
            발주일
            <input v-model="form.poDate" type="date" />
          </label>
          <label>
            납품 예정일
            <input v-model="form.dueDate" type="date" />
          </label>
          <label>
            구매거래처
            <select v-model="form.vendorId">
              <option value="">거래처를 선택하세요</option>
              <option v-for="vendor in vendorOptions" :key="vendor.vendorId" :value="vendor.vendorId">
                {{ vendor.vendorId }}
              </option>
            </select>
          </label>
          <label>
            발주담당자
            <input v-model.trim="form.managerName" type="text" />
          </label>
          <label>
            비고
            <input v-model.trim="form.remark" type="text" />
          </label>
        </div>

        <div class="section-head">
          <h2>발주 자재</h2>
          <button type="button" class="search-btn" @click="addItemRow">자재 추가</button>
        </div>
        <div class="table-wrap">
          <table class="data-table form-table">
            <thead>
              <tr>
                <th>자재 ID</th>
                <th>자재 명</th>
                <th>발주 수량</th>
                <th>구매단위</th>
                <th>납품 예정일</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="form.items.length === 0">
                <td colspan="6" class="empty">자재를 추가하세요.</td>
              </tr>
              <tr v-for="(item, index) in form.items" :key="index">
                <td>
                  <select v-model="item.itemId" @change="onSelectItem(item)">
                    <option value="">자재를 선택하세요</option>
                    <option
                      v-for="option in itemOptionsFor(item)"
                      :key="option.itemId"
                      :value="option.itemId"
                    >
                      {{ option.itemId }} {{ option.itemName }}
                    </option>
                  </select>
                </td>
                <td>{{ displayItemName(item) }}</td>
                <td>
                  <input v-model="item.qty" type="number" min="0" step="0.0001" />
                </td>
                <td>
                  <input v-model.trim="item.unit" type="text" />
                </td>
                <td>
                  <input v-model="item.dueDate" type="date" />
                </td>
                <td>
                  <button type="button" class="danger-btn" @click="removeItemRow(index)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeForm">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveForm">저장</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchItems } from '@/api/item'
import { fetchVendors } from '@/api/vendor'
import {
  createPurchaseOrder,
  deletePurchaseOrder,
  fetchPurchaseOrders,
  updatePurchaseOrder
} from '@/api/purchaseOrder'

const orders = ref([])
const vendors = ref([])
const items = ref([])
const selectedPoNo = ref('')
const loading = ref(false)
const deleting = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const isEdit = ref(false)

const form = reactive(emptyForm())

const selectedOrder = computed(() => orders.value.find((order) => order.poNo === selectedPoNo.value) || null)
const selectedItems = computed(() => selectedOrder.value?.items || [])
const usableVendors = computed(() =>
  vendors.value.filter((vendor) => !vendor.useYn || vendor.useYn.toUpperCase() === 'Y')
)
const vendorOptions = computed(() => {
  const options = [...usableVendors.value]
  if (form.vendorId && !options.some((vendor) => vendor.vendorId === form.vendorId)) {
    options.unshift({ vendorId: form.vendorId })
  }
  return options
})
const purchaseItems = computed(() =>
  items.value.filter((item) => isPurchaseItem(item.itemId) && isUsable(item.useYn))
)

function today() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function emptyItem() {
  return {
    itemId: '',
    itemName: '',
    qty: '',
    unit: 'Kg',
    dueDate: today()
  }
}

function emptyForm() {
  return {
    poNo: '',
    poType: '구매입고',
    poDate: today(),
    vendorId: '',
    managerName: '관리자',
    dueDate: today(),
    remark: '',
    items: [emptyItem()]
  }
}

function assignForm(source) {
  form.poNo = source.poNo || ''
  form.poType = source.poType || '구매입고'
  form.poDate = source.poDate || today()
  form.vendorId = resolveVendorId(source.vendorId || source.vendorName)
  form.managerName = source.managerName || '관리자'
  form.dueDate = source.dueDate || source.poDate || today()
  form.remark = source.remark || ''
  form.items = (source.items || []).map((item) => ({
    itemId: item.itemId || '',
    itemName: item.itemName || '',
    qty: item.qty ?? '',
    unit: item.unit || 'Kg',
    dueDate: item.dueDate || source.dueDate || today()
  }))
  if (form.items.length === 0) {
    form.items.push(emptyItem())
  }
}

function isUsable(useYn) {
  return !useYn || String(useYn).toUpperCase() === 'Y'
}

function isPurchaseItem(itemId) {
  return Boolean(itemId) && String(itemId).startsWith('1')
}

function itemOptionsFor(row) {
  const options = [...purchaseItems.value]
  if (row.itemId && !options.some((item) => item.itemId === row.itemId)) {
    options.unshift({
      itemId: row.itemId,
      itemName: row.itemName
    })
  }
  return options
}

function displayItemName(row) {
  return purchaseItems.value.find((item) => item.itemId === row.itemId)?.itemName || row.itemName || ''
}

function onSelectItem(row) {
  const selected = purchaseItems.value.find((item) => item.itemId === row.itemId)
  row.itemName = selected?.itemName || ''
}

function resolveVendorId(value) {
  if (!value) {
    return ''
  }
  if (usableVendors.value.some((vendor) => vendor.vendorId === value)) {
    return value
  }
  const suffix = value.includes('_') ? value.slice(value.lastIndexOf('_') + 1) : ''
  if (suffix && usableVendors.value.some((vendor) => vendor.vendorId === suffix)) {
    return suffix
  }
  return value
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

function selectOrder(order) {
  selectedPoNo.value = order.poNo
}

function openCreate() {
  isEdit.value = false
  formError.value = ''
  assignForm(emptyForm())
  showForm.value = true
}

function openEdit() {
  if (!selectedOrder.value) {
    errorMessage.value = '수정할 발주를 선택하세요.'
    return
  }
  isEdit.value = true
  formError.value = ''
  assignForm(selectedOrder.value)
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  formError.value = ''
}

function addItemRow() {
  form.items.push({
    ...emptyItem(),
    dueDate: form.dueDate || today()
  })
}

function removeItemRow(index) {
  form.items.splice(index, 1)
}

function toPayload() {
  return {
    poNo: form.poNo || null,
    poType: form.poType,
    poDate: form.poDate || null,
    vendorId: form.vendorId || null,
    managerName: form.managerName || null,
    dueDate: form.dueDate || null,
    remark: form.remark || null,
    items: form.items.map((item) => ({
      itemId: item.itemId || null,
      itemName: item.itemName || null,
      qty: item.qty === '' || item.qty === null || item.qty === undefined ? null : Number(item.qty),
      unit: item.unit || 'Kg',
      dueDate: item.dueDate || form.dueDate || null
    }))
  }
}

async function saveForm() {
  saving.value = true
  formError.value = ''
  try {
    const payload = toPayload()
    const { data } = isEdit.value
      ? await updatePurchaseOrder(selectedPoNo.value, payload)
      : await createPurchaseOrder(payload)
    showForm.value = false
    await loadOrders(data?.poNo)
  } catch (error) {
    formError.value = error.response?.data?.message || '발주 저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function onDelete() {
  if (!selectedOrder.value) {
    errorMessage.value = '삭제할 발주를 선택하세요.'
    return
  }
  if (!window.confirm(`발주 ${selectedOrder.value.poNo}를 삭제하시겠습니까?`)) {
    return
  }

  deleting.value = true
  errorMessage.value = ''
  try {
    await deletePurchaseOrder(selectedOrder.value.poNo)
    await loadOrders()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '발주 삭제에 실패했습니다.'
  } finally {
    deleting.value = false
  }
}

async function loadItems() {
  try {
    const { data } = await fetchItems()
    items.value = Array.isArray(data) ? data : []
  } catch (error) {
    items.value = []
    errorMessage.value = error.response?.data?.message || '품목 조회에 실패했습니다.'
  }
}

async function loadVendors() {
  try {
    const { data } = await fetchVendors()
    vendors.value = Array.isArray(data) ? data : []
  } catch (error) {
    vendors.value = []
    errorMessage.value = error.response?.data?.message || '거래처 조회에 실패했습니다.'
  }
}

async function loadOrders(preferPoNo) {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchPurchaseOrders()
    orders.value = Array.isArray(data) ? data : []
    const keepPoNo = preferPoNo || selectedPoNo.value
    selectedPoNo.value = orders.value.some((order) => order.poNo === keepPoNo)
      ? keepPoNo
      : (orders.value[0]?.poNo || '')
  } catch (error) {
    orders.value = []
    selectedPoNo.value = ''
    errorMessage.value = error.response?.data?.message || '발주 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadVendors(), loadItems()])
  await loadOrders()
})
</script>
