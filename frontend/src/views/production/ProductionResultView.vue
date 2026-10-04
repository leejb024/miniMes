<template>
  <div class="page-card">
    <div class="page-header">
      <h1>생산실적</h1>
      <div class="filter-bar">
        <label>
          작업일자
          <input v-model="planDateFrom" type="date" />
        </label>
        <span class="filter-sep">~</span>
        <input v-model="planDateTo" type="date" />
        <button type="button" class="search-btn" :disabled="loading" @click="loadWorkOrders()">조회</button>
        <button
          type="button"
          class="search-btn"
          :disabled="completing || !selectedWorkOrder || isWorkOrderClosed"
          @click="completeSelected"
        >
          작업완료
        </button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="split-grid result-layout">
      <section class="grid-section">
        <h2>작업 지시 리스트</h2>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th class="check-col"></th>
                <th>No.</th>
                <th>작업지시번호</th>
                <th>작업 일자</th>
                <th>공정</th>
                <th>계획수량</th>
                <th>수량</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="8" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="workOrders.length === 0">
                <td colspan="8" class="empty">조회된 작업지시가 없습니다.</td>
              </tr>
              <tr
                v-for="(row, index) in workOrders"
                v-else
                :key="rowKey(row)"
                :class="{ selected: selectedKey === rowKey(row), clickable: true }"
                @click="selectWorkOrder(row)"
              >
                <td class="check-col">
                  <input type="checkbox" :checked="selectedKey === rowKey(row)" @click.stop="selectWorkOrder(row)" />
                </td>
                <td>{{ index + 1 }}</td>
                <td>{{ row.workOrderId }}</td>
                <td>{{ row.planDate }}</td>
                <td>{{ displayedProcess(row) }}</td>
                <td>{{ formatQty(row.planQty) }}</td>
                <td>{{ formatQty(displayedResultQty(row)) }}</td>
                <td>{{ row.resultStatus }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="grid-section">
        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            type="button"
            class="tab"
            :class="{ active: activeTab === tab.id }"
            @click="activeTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>

        <div v-if="activeTab === 'input'">
          <div class="section-head">
            <h2>배합</h2>
            <div class="section-actions toolbar-row">
              <button type="button" class="search-btn" :disabled="!selectedWorkOrder || isWorkOrderClosed" @click="openMaterialInput">자재투입</button>
              <button type="button" class="search-btn" :disabled="!selectedWorkOrder || isWorkOrderClosed || acting" @click="completeMixing">배합완료</button>
            </div>
          </div>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>No.</th>
                  <th>품번</th>
                  <th>품명</th>
                  <th>소요량</th>
                  <th>현재고</th>
                  <th>투입수량</th>
                  <th>출고창고</th>
                  <th>재고</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="tabLoading"><td colspan="8" class="empty">조회 중...</td></tr>
                <tr v-else-if="!selectedWorkOrder"><td colspan="8" class="empty">상단 작업지시를 선택하세요.</td></tr>
                <tr v-else-if="mixLines.length === 0"><td colspan="8" class="empty">BOM 자재가 없습니다.</td></tr>
                <tr v-for="(item, index) in mixLines" v-else :key="item.materialId">
                  <td>{{ index + 1 }}</td>
                  <td>{{ item.materialId }}</td>
                  <td>{{ item.itemName }}</td>
                  <td>{{ formatQty(item.requiredQty) }}</td>
                  <td>{{ formatQty(item.onHandQty) }}</td>
                  <td>{{ formatQty(item.inputQty) }}</td>
                  <td>{{ item.warehouseName }}</td>
                  <td>{{ formatQty(item.stockQty) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else-if="activeTab === 'result'">
          <div class="section-head">
            <h2>포장</h2>
            <div class="section-actions">
              <span class="result-count">검색결과 : {{ goodRows.length }}건</span>
              <button type="button" class="search-btn" :disabled="!canPack || savingGood" @click="openPackResult">포장실적</button>
            </div>
          </div>
          <div class="table-wrap">
            <table class="data-table form-table">
              <thead>
                <tr>
                  <th>구분</th>
                  <th>품번</th>
                  <th>품명</th>
                  <th>LOT NO</th>
                  <th>시작일시</th>
                  <th>종료일시</th>
                  <th>실적 수량</th>
                  <th>단위</th>
                  <th>상태</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="tabLoading"><td colspan="10" class="empty">조회 중...</td></tr>
                <tr v-else-if="!selectedWorkOrder"><td colspan="10" class="empty">상단 작업지시를 선택하세요.</td></tr>
                <tr
                  v-for="item in goodRows"
                  v-else
                  :key="item.prodResultSeq"
                >
                  <td>{{ resultTypeLabel(item.resultType) }}</td>
                  <td>{{ packItemId }}</td>
                  <td>{{ packItemName }}</td>
                  <td>{{ item.lotId }}</td>
                  <td>{{ formatDateTime(item.productionStartTime) }}</td>
                  <td>{{ formatDateTime(item.productionEndTime) }}</td>
                  <td>{{ formatQty(item.prodQty) }}</td>
                  <td>{{ item.unit }}</td>
                  <td>{{ formatConfirmStatus(item.isConfirmed) }}</td>
                  <td>
                    <button
                      v-if="item.isConfirmed === 'N'"
                      type="button"
                      class="ghost-btn"
                      :disabled="savingGood || deletingGoodSeq === item.prodResultSeq"
                      @click.stop="deleteGood(item)"
                    >
                      삭제
                    </button>
                  </td>
                </tr>
                <tr v-if="selectedWorkOrder && !tabLoading && goodRows.length === 0">
                  <td colspan="10" class="empty">조회된 포장실적이 없습니다.</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-if="selectedWorkOrder && !mixCompleted && !isWorkOrderClosed" class="hint">배합완료 후 포장실적을 입력할 수 있습니다.</p>
        </div>
      </section>
    </div>

    <div v-if="showResult" class="modal-mask" @click.self="cancelResult">
      <div class="modal-card">
        <div class="modal-head">
          <h2>포장실적</h2>
          <button type="button" class="ghost-btn" @click="cancelResult">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="input-summary">
          <p class="hint">투입 자재</p>
          <p v-if="inputMaterialLines.length === 0" class="hint">투입된 자재가 없습니다.</p>
          <p v-for="line in inputMaterialLines" :key="line.key" class="hint">
            {{ line.label }} / {{ formatQty(line.qty) }} {{ line.unit }}
          </p>
        </div>
        <div class="form-grid">
          <label>
            완제품 LOT
            <input :value="resultForm.lotId" type="text" readonly />
          </label>
          <label>
            포장 수량
            <div class="weigh-qty-row">
              <input v-model="resultForm.prodQty" type="number" min="0" step="0.0001" />
              <span class="weigh-unit">{{ resultForm.unit || selectedWorkOrder?.unit || 'Kg' }}</span>
            </div>
          </label>
          <label>
            시작 일시
            <input v-model="resultForm.startTime" type="datetime-local" />
          </label>
          <label>
            종료 일시
            <input v-model="resultForm.endTime" type="datetime-local" />
          </label>
        </div>
        <p class="hint">완제품 LOT는 자동으로 채번됩니다. 포장수량은 투입 자재와 BOM으로 계산되며, 필요하면 수정할 수 있습니다.</p>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="savingGood" @click="cancelResult">취소</button>
          <button type="button" class="search-btn" :disabled="savingGood" @click="saveResult">저장</button>
        </div>
      </div>
    </div>

    <div v-if="showWeigh" class="modal-mask" @click.self="showWeigh = false">
      <div class="modal-card">
        <div class="modal-head">
          <h2>자재 투입</h2>
          <button type="button" class="ghost-btn" @click="showWeigh = false">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid weigh-form">
          <label>
            재고
            <select v-model="form.stockSeq">
              <option value="">재고를 선택하세요</option>
              <option v-for="stock in inputStocks" :key="stock.stockSeq" :value="String(stock.stockSeq)">
                {{ stock.itemName }} / LOT {{ stock.lotNo }} / {{ stock.warehouseName }} / 현재고 {{ formatQty(stock.qty) }} {{ stock.unit }}
              </option>
            </select>
            <p v-if="selectedWeighStock" class="hint weigh-stock-meta">
              LOT {{ selectedWeighStock.lotNo }} · {{ selectedWeighStock.warehouseName }} · 현재고 {{ formatQty(selectedWeighStock.qty) }} {{ selectedWeighStock.unit }}
            </p>
          </label>
          <label>
            투입 수량
            <div class="weigh-qty-row">
              <input v-model="form.weighQty" type="number" min="0" step="0.0001" :max="selectedWeighStock ? selectedWeighStock.qty : undefined" />
              <span v-if="selectedWeighStock" class="weigh-unit">{{ selectedWeighStock.unit }}</span>
              <span v-if="selectedWeighStock" class="hint weigh-onhand">현재고: {{ formatQty(selectedWeighStock.qty) }} {{ selectedWeighStock.unit }}</span>
            </div>
          </label>
        </div>
        <p v-if="weighWarning" class="error">{{ weighWarning }}</p>
        <p v-else class="hint">투입 수량은 0보다 크고, 선택한 LOT의 현재고를 넘을 수 없습니다.</p>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" @click="showWeigh = false">취소</button>
          <button type="button" class="search-btn" :disabled="acting || !canSaveWeigh" @click="saveWeigh">저장</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchBomMaterials } from '@/api/bom'
import { fetchItems } from '@/api/item'
import {
  deleteProdResult,
  fetchInputStocks,
  fetchProdResults,
  fetchProductionSheet,
  finishWorkOrder,
  inputProductionMaterial,
  saveProductionResult,
  suggestProductionResult
} from '@/api/prodResult'
import { fetchWorkOrders } from '@/api/workOrder'

const route = useRoute()
const tabs = [
  { id: 'input', label: '배합' },
  { id: 'result', label: '포장' }
]

const planDateFrom = ref(oneMonthAgo())
const planDateTo = ref(today())
const workOrders = ref([])
const items = ref([])
const selectedWorkOrder = ref(null)
const selectedKey = ref('')
const activeTab = ref('input')
const loading = ref(false)
const tabLoading = ref(false)
const acting = ref(false)
const errorMessage = ref('')
const formError = ref('')
const goodRows = ref([])
const inputLots = ref([])
const inputStocks = ref([])
const bomMaterials = ref([])
const savingGood = ref(false)
const deletingGoodSeq = ref(null)
const completing = ref(false)
const showWeigh = ref(false)
const showResult = ref(false)
const mixDoneIds = ref(readMixDone())
const packClickedIds = ref(new Set())
const resultForm = reactive({ lotId: '', prodQty: '', unit: '', startTime: '', endTime: '' })
const form = reactive({ stockSeq: '', weighQty: '' })

const isWorkOrderClosed = computed(() => {
  const row = selectedWorkOrder.value
  return Boolean(row) && (row.closeYn === 'Y' || row.resultStatus === '완료')
})
const packItemId = computed(() => selectedWorkOrder.value?.itemId || '')
const packItemName = computed(() => {
  const itemId = packItemId.value
  if (!itemId) return ''
  return items.value.find((item) => item.itemId === itemId)?.itemName || ''
})
const selectedWeighStock = computed(() => inputStocks.value.find((stock) => String(stock.stockSeq) === String(form.stockSeq)) || null)
const mixCompleted = computed(() => {
  const workOrderId = selectedWorkOrder.value?.workOrderId
  if (!workOrderId) {
    return false
  }
  return mixDoneIds.value.has(workOrderId) || goodRows.value.some((row) => row.resultType === 'PACK')
})
const canPack = computed(() => Boolean(selectedWorkOrder.value) && mixCompleted.value && !isWorkOrderClosed.value)
const mixLines = computed(() => {
  const planQty = Number(selectedWorkOrder.value?.planQty || 0)
  return bomMaterials.value.map((material) => {
    const stocks = inputStocks.value.filter((stock) => stock.itemId === material.materialId)
    const inputs = inputLots.value.filter((lot) => lot.itemId === material.materialId && isMaterialInput(lot))
    const issuedNames = [...new Set(inputs.map((lot) => lot.warehouseName).filter(Boolean))]
    const stockWarehouse = stocks.slice().sort((left, right) => Number(right.qty || 0) - Number(left.qty || 0))[0]?.warehouseName || ''
    const warehouseNames = issuedNames.length > 0 ? issuedNames : (stockWarehouse ? [stockWarehouse] : [])
    const warehouseName = warehouseNames.join(', ')
    const warehouseStocks = stocks.filter((stock) => warehouseNames.includes(stock.warehouseName))
    const bomQty = Number(material.qty || 0)
    return {
      materialId: material.materialId,
      itemName: material.itemName || '',
      requiredQty: planQty > 0 ? bomQty * planQty : bomQty,
      onHandQty: stocks.reduce((sum, stock) => sum + Number(stock.qty || 0), 0),
      inputQty: inputs.reduce((sum, lot) => sum + Number(lot.inputQty || 0), 0),
      warehouseName,
      stockQty: warehouseName
        ? warehouseStocks.reduce((sum, stock) => sum + Number(stock.qty || 0), 0)
        : 0
    }
  })
})
const weighWarning = computed(() => {
  if (!form.stockSeq || form.weighQty === '' || form.weighQty === null || form.weighQty === undefined) {
    return ''
  }
  const qty = Number(form.weighQty)
  if (Number.isNaN(qty) || qty <= 0) {
    return '투입 수량은 0보다 커야 합니다.'
  }
  const stock = selectedWeighStock.value
  if (stock && qty > Number(stock.qty)) {
    return `계량 수량이 현재고를 초과합니다. 현재고는 ${formatQty(stock.qty)} ${stock.unit || ''}입니다.`
  }
  return ''
})
const canSaveWeigh = computed(() => Boolean(selectedWeighStock.value) && form.weighQty !== '' && !weighWarning.value)
const inputMaterialLines = computed(() => inputLots.value
  .filter((item) => item.status !== 'RETURNED')
  .map((item) => ({
    key: item.inputLotSeq,
    label: `${item.itemName || ''} / LOT ${item.lotNo || ''}`,
    qty: item.status === 'WEIGHED' ? item.weighQty : item.inputQty,
    unit: item.unit || ''
  })))

function formatIsoDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function today() {
  return formatIsoDate(new Date())
}

function oneMonthAgo() {
  const date = new Date()
  date.setMonth(date.getMonth() - 1)
  return formatIsoDate(date)
}

function rowKey(row) {
  return [row.workOrderId, row.planDate, row.workcenterId].join('|')
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

function formatConfirmStatus(isConfirmed) {
  return isConfirmed === 'N' ? '진행중' : '완료'
}

function formatQty(value) {
  if (value === null || value === undefined || value === '') return ''
  const number = Number(value)
  return Number.isNaN(number) ? value : number.toLocaleString('ko-KR')
}

function displayedResultQty(row) {
  if (selectedKey.value && rowKey(row) === selectedKey.value && !tabLoading.value) {
    if (goodRows.value.length === 0) return null
    return goodRows.value.reduce((total, item) => total + Number(item.prodQty || 0), 0)
  }
  return row.resultQty
}

function hasPackResult(row) {
  const selected = selectedKey.value && rowKey(row) === selectedKey.value && !tabLoading.value
  if (selected) return goodRows.value.some((item) => item.resultType === 'PACK')
  return Number(row.packCount || 0) > 0
}

function isWorkStarted(row) {
  const state = row?.state || ''
  return state === '시작' || state === '믹스시작'
}

function displayedProcess(row) {
  if (hasPackResult(row) || packClickedIds.value.has(row.workOrderId)) return '공정완료'
  if (mixDoneIds.value.has(row.workOrderId)) return '포장'
  if (isWorkStarted(row)) return '배합'
  return ''
}

function resultTypeLabel(resultType) {
  if (resultType === 'WEIGH') return '계량'
  if (resultType === 'PACK') return '포장'
  return '생산'
}

function isMaterialInput(item) {
  return item && (item.status === 'INPUT' || item.status === 'COMBINED') && Number(item.inputQty) > 0
}

const MIX_DONE_KEY = 'miniMes.mixDone'

function readMixDone() {
  try {
    const parsed = JSON.parse(localStorage.getItem(MIX_DONE_KEY) || '[]')
    return new Set(Array.isArray(parsed) ? parsed : [])
  } catch {
    return new Set()
  }
}

function markMixDone(workOrderId) {
  const next = new Set(mixDoneIds.value)
  next.add(workOrderId)
  mixDoneIds.value = next
  localStorage.setItem(MIX_DONE_KEY, JSON.stringify([...next]))
}

function clearTabRows() {
  goodRows.value = []
  inputLots.value = []
  bomMaterials.value = []
  inputStocks.value = []
}

function queryValue(value) {
  return Array.isArray(value) ? (value[0] || '') : (value || '')
}

function applyRouteFilters() {
  const queryPlanDate = queryValue(route.query.planDate)
  if (!queryPlanDate) return
  if (!planDateFrom.value || planDateFrom.value > queryPlanDate) planDateFrom.value = queryPlanDate
  if (!planDateTo.value || planDateTo.value < queryPlanDate) planDateTo.value = queryPlanDate
}

function findMatchingWorkOrder() {
  const workOrderId = queryValue(route.query.workOrderId)
  if (!workOrderId) return null
  const planDate = queryValue(route.query.planDate)
  const workcenterId = queryValue(route.query.workcenterId)
  return workOrders.value.find((row) => row.workOrderId === workOrderId
    && (!planDate || row.planDate === planDate)
    && (!workcenterId || String(row.workcenterId) === String(workcenterId)))
    || workOrders.value.find((row) => row.workOrderId === workOrderId)
    || null
}

async function fetchWorkOrderList() {
  const { data } = await fetchWorkOrders(planDateFrom.value, planDateTo.value)
  return Array.isArray(data) ? data : []
}

async function refreshWorkOrderListKeepingSelection() {
  const key = selectedKey.value
  const list = await fetchWorkOrderList()
  workOrders.value = list
  const matched = list.find((row) => rowKey(row) === key)
  if (matched) {
    selectedWorkOrder.value = matched
    selectedKey.value = rowKey(matched)
  }
}

async function loadWorkOrders(selectFromQuery = false) {
  if (!planDateFrom.value || !planDateTo.value) {
    errorMessage.value = '작업일자 기간을 입력하세요.'
    return
  }
  loading.value = true
  errorMessage.value = ''
  selectedWorkOrder.value = null
  selectedKey.value = ''
  clearTabRows()
  try {
    workOrders.value = await fetchWorkOrderList()
    if (selectFromQuery) {
      const matched = findMatchingWorkOrder()
      if (matched) await selectWorkOrder(matched)
      else if (queryValue(route.query.workOrderId) && workOrders.value.length > 0) {
        errorMessage.value = '선택한 작업지시를 현재 조회 조건에서 찾지 못했습니다.'
      }
    }
  } catch (error) {
    workOrders.value = []
    errorMessage.value = error.response?.data?.message || '작업지시 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function selectWorkOrder(row) {
  selectedWorkOrder.value = row
  selectedKey.value = rowKey(row)
  tabLoading.value = true
  errorMessage.value = ''
  clearTabRows()
  try {
    const version = row.bomVersion || row.itemVersion || ''
    const [goodResult, sheetResult, bomResult, stockResult] = await Promise.all([
      fetchProdResults(row.workOrderId),
      fetchProductionSheet(row.workOrderId),
      fetchBomMaterials(row.itemId, version),
      fetchInputStocks()
    ])
    goodRows.value = Array.isArray(goodResult.data) ? goodResult.data : []
    const data = sheetResult.data || {}
    inputLots.value = Array.isArray(data.inputs) ? data.inputs : []
    bomMaterials.value = Array.isArray(bomResult.data) ? bomResult.data : []
    inputStocks.value = Array.isArray(stockResult.data) ? stockResult.data : []
  } catch (error) {
    goodRows.value = []
    inputLots.value = []
    bomMaterials.value = []
    inputStocks.value = []
    errorMessage.value = error.response?.data?.message || '생산실적 조회에 실패했습니다.'
  } finally {
    tabLoading.value = false
  }
}

function openPackResult() {
  if (!canPack.value) {
    window.alert('배합완료 후 포장할 수 있습니다.')
    return
  }
  const workOrderId = selectedWorkOrder.value?.workOrderId
  if (workOrderId) {
    const next = new Set(packClickedIds.value)
    next.add(workOrderId)
    packClickedIds.value = next
  }
  openResult()
}

function cancelResult() {
  showResult.value = false
  const workOrderId = selectedWorkOrder.value?.workOrderId
  if (!workOrderId || goodRows.value.some((item) => item.resultType === 'PACK')) return
  const next = new Set(packClickedIds.value)
  next.delete(workOrderId)
  packClickedIds.value = next
}

async function openResult() {
  if (!selectedWorkOrder.value || isWorkOrderClosed.value || !mixCompleted.value) return
  const now = toDateTimeLocal(new Date())
  resultForm.lotId = ''
  resultForm.prodQty = ''
  resultForm.unit = selectedWorkOrder.value.unit || 'Kg'
  resultForm.startTime = now
  resultForm.endTime = now
  formError.value = ''
  showResult.value = true
  try {
    const { data } = await suggestProductionResult(selectedWorkOrder.value.workOrderId)
    resultForm.lotId = data?.lotNo || ''
    resultForm.unit = data?.unit || resultForm.unit
    resultForm.prodQty = data?.prodQty == null ? '' : Number(data.prodQty)
  } catch (error) {
    formError.value = error.response?.data?.message || '포장수량 계산에 실패했습니다. 수량을 직접 입력하세요.'
  }
}

function toDateTimeLocal(date) {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function toApiDateTime(value) {
  if (!value) return null
  return value.length === 16 ? `${value}:00` : value
}

async function saveResult() {
  if (!selectedWorkOrder.value) return
  if (!resultForm.prodQty || Number(resultForm.prodQty) <= 0) {
    formError.value = '포장 수량은 0보다 커야 합니다.'
    return
  }
  if (!resultForm.startTime || !resultForm.endTime) {
    formError.value = '시작 일시와 종료 일시를 입력하세요.'
    return
  }
  if (resultForm.endTime < resultForm.startTime) {
    formError.value = '종료 일시는 시작 일시 이후여야 합니다.'
    return
  }
  savingGood.value = true
  formError.value = ''
  try {
    await saveProductionResult({
      workOrderId: selectedWorkOrder.value.workOrderId,
      lotId: resultForm.lotId,
      prodQty: Number(resultForm.prodQty),
      resultType: 'PACK',
      productionStartTime: toApiDateTime(resultForm.startTime),
      productionEndTime: toApiDateTime(resultForm.endTime)
    })
    showResult.value = false
    await selectWorkOrder(selectedWorkOrder.value)
    await refreshWorkOrderListKeepingSelection()
  } catch (error) {
    formError.value = error.response?.data?.message || '포장실적 저장에 실패했습니다.'
  } finally {
    savingGood.value = false
  }
}

async function deleteGood(item) {
  deletingGoodSeq.value = item.prodResultSeq
  errorMessage.value = ''
  try {
    await deleteProdResult(item.prodResultSeq)
    await selectWorkOrder(selectedWorkOrder.value)
    await refreshWorkOrderListKeepingSelection()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '생산실적 삭제에 실패했습니다.'
  } finally {
    deletingGoodSeq.value = null
  }
}

function packagingFinished() {
  const packedQty = goodRows.value
    .filter((row) => row.resultType === 'PACK')
    .reduce((sum, row) => sum + Number(row.prodQty || 0), 0)
  const planQty = Number(selectedWorkOrder.value?.planQty || 0)
  if (planQty > 0) {
    return packedQty + 0.0000001 >= planQty
  }
  return packedQty > 0
}

async function completeSelected() {
  if (!selectedWorkOrder.value) return
  if (!packagingFinished()) {
    window.alert('포장이 끝나지 않았습니다')
    return
  }
  completing.value = true
  errorMessage.value = ''
  try {
    await finishWorkOrder({ workOrderId: selectedWorkOrder.value.workOrderId })
    await selectWorkOrder(selectedWorkOrder.value)
    await refreshWorkOrderListKeepingSelection()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '작업 완료에 실패했습니다.'
  } finally {
    completing.value = false
  }
}

async function openMaterialInput() {
  form.stockSeq = ''
  form.weighQty = ''
  formError.value = ''
  showWeigh.value = true
  try {
    const { data } = await fetchInputStocks()
    inputStocks.value = Array.isArray(data) ? data : []
  } catch (error) {
    inputStocks.value = []
    formError.value = error.response?.data?.message || '재고 조회에 실패했습니다.'
  }
}

async function saveWeigh() {
  if (!selectedWeighStock.value) {
    formError.value = '재고를 선택하세요.'
    return
  }
  if (weighWarning.value) {
    formError.value = weighWarning.value
    return
  }
  acting.value = true
  formError.value = ''
  try {
    const payload = {
      workOrderId: selectedWorkOrder.value.workOrderId,
      stockSeq: Number(form.stockSeq),
      weighQty: Number(form.weighQty)
    }
    await inputProductionMaterial(payload)
    showWeigh.value = false
    await selectWorkOrder(selectedWorkOrder.value)
  } catch (error) {
    formError.value = error.response?.data?.message || '자재 투입에 실패했습니다.'
  } finally {
    acting.value = false
  }
}

async function completeMixing() {
  if (!selectedWorkOrder.value) return
  acting.value = true
  errorMessage.value = ''
  try {
    const version = selectedWorkOrder.value.bomVersion || selectedWorkOrder.value.itemVersion || ''
    const { data } = await fetchBomMaterials(selectedWorkOrder.value.itemId, version)
    const materials = Array.isArray(data) ? data : []
    if (materials.length === 0) {
      window.alert('BOM에 등록된 자재가 없습니다.')
      return
    }
    const missing = materials.filter((material) => !inputLots.value.some((lot) => lot.itemId === material.materialId && isMaterialInput(lot)))
    if (missing.length > 0) {
      const names = missing.map((material) => material.itemName || material.materialId).join(', ')
      window.alert(`BOM에 맞게 자재가 모두 투입되지 않았습니다.\n${names}`)
      return
    }
    markMixDone(selectedWorkOrder.value.workOrderId)
    window.alert('배합이 완료되었습니다.')
  } catch (error) {
    window.alert(error.response?.data?.message || 'BOM 자재 확인에 실패했습니다.')
  } finally {
    acting.value = false
  }
}

async function loadItems() {
  try {
    const { data } = await fetchItems()
    items.value = Array.isArray(data) ? data : []
  } catch {
    items.value = []
  }
}

onMounted(async () => {
  applyRouteFilters()
  await Promise.all([loadWorkOrders(true), loadItems()])
})

watch(
  () => [queryValue(route.query.workOrderId), queryValue(route.query.planDate), queryValue(route.query.workcenterId)],
  async (next, prev) => {
    if (!next[0] || JSON.stringify(next) === JSON.stringify(prev)) return
    applyRouteFilters()
    await loadWorkOrders(true)
  }
)
</script>

<style scoped>
.weigh-form {
  grid-template-columns: 1fr;
}

.weigh-stock-meta,
.weigh-onhand {
  margin: 0;
  font-weight: 400;
}

.weigh-qty-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.weigh-qty-row input {
  width: 140px;
}

.weigh-unit {
  font-weight: 600;
  color: #303133;
}

.input-summary {
  margin-bottom: 12px;
}
</style>


