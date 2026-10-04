<template>
  <div class="page-card">
    <div class="page-header">
      <h1>작업지시</h1>
      <div class="filter-bar">
        <label>
          계획일
          <input v-model="planDateFrom" type="date" />
        </label>
        <span class="filter-sep">~</span>
        <input v-model="planDateTo" type="date" />
        <button type="button" class="search-btn" :disabled="loading" @click="loadWorkOrders">조회</button>
        <button type="button" class="search-btn" @click="openCreate">추가</button>
        <button type="button" class="ghost-btn" :disabled="!selectedRow || isSelectedClosed" @click="openEdit">수정</button>
        <button type="button" class="danger-btn" :disabled="!selectedRow || deleting || !canDeleteSelected" @click="onDelete">삭제</button>
      </div>
    </div>
    <div class="section-actions toolbar-row">
      <button type="button" class="search-btn" :disabled="!canChangeSelected || acting" @click="onStart">작업시작</button>
      <button type="button" class="danger-btn" :disabled="!canChangeSelected || acting" @click="onCancel">작업중단</button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>PLAN_DATE</th>
            <th>WORK_ORDER_ID</th>
            <th>공정</th>
            <th>설비</th>
            <th>ITEM_ID</th>
            <th>STATE</th>
            <th>UNIT</th>
            <th>PLAN_QTY</th>
            <th>ITEM_VERSION</th>
            <th>BOM_VERSION</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="rows.length === 0">
            <td colspan="10" class="empty">조회된 작업지시가 없습니다.</td>
          </tr>
          <tr
            v-for="row in rows"
            v-else
            :key="row.workOrderId + '|' + (row.bomVersion || '')"
            class="clickable"
            :class="{ selected: selectedWorkOrderId === row.workOrderId }"
            @click="selectRow(row)"
            @dblclick="openProductionResult(row)"
          >
            <td>{{ row.planDate }}</td>
            <td>{{ row.workOrderId }}</td>
            <td>{{ processLabel(row) }}</td>
            <td>{{ equipmentLabel(row) }}</td>
            <td>{{ row.itemId }}</td>
            <td>{{ displayState(row) }}</td>
            <td>{{ row.unit }}</td>
            <td>{{ row.planQty }}</td>
            <td>{{ row.itemVersion }}</td>
            <td>{{ row.bomVersion }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-if="actionHint" class="hint work-order-note">{{ actionHint }}</p>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>{{ isEdit ? '작업지시 수정' : '작업지시 추가' }}</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>

        <p v-if="formError" class="error">{{ formError }}</p>

        <div class="form-grid">
          <label>
            계획일
            <input v-model="form.planDate" type="date" />
          </label>
          <label>
            작업지시번호
            <input
              v-model.trim="form.workOrderId"
              type="text"
              :readonly="isEdit"
              :placeholder="isEdit ? '' : '저장 시 자동 채번'"
            />
          </label>
          <label>
            공정
            <select v-model="form.processId" @change="onSelectProcess">
              <option value="">공정을 선택하세요</option>
              <option v-for="process in processOptions" :key="process.processId" :value="process.processId">
                {{ process.processId }} {{ process.processName }}
              </option>
            </select>
          </label>
          <label>
            설비
            <select v-model="form.equipId" :disabled="!form.processId">
              <option value="">설비를 선택하세요</option>
              <option v-for="equipment in equipmentOptions" :key="equipment.equipId" :value="equipment.equipId">
                {{ equipment.equipId }} {{ equipment.equipName }}
              </option>
            </select>
          </label>
          <label>
            품목
            <select v-model="form.itemId" @change="onSelectItem">
              <option value="">품목을 선택하세요</option>
              <option v-for="item in itemOptions" :key="item.itemId" :value="item.itemId">
                {{ item.itemId }} {{ item.itemName }}
              </option>
            </select>
          </label>
          <label>
            상태
            <input v-model.trim="form.state" type="text" />
          </label>
          <label>
            단위
            <input v-model.trim="form.unit" type="text" />
          </label>
          <label>
            계획수량
            <input v-model="form.planQty" type="number" min="0" step="0.0001" />
          </label>
          <label v-if="isEdit">
            품목버전
            <input v-model.trim="form.itemVersion" type="text" list="work-order-item-versions" />
            <datalist id="work-order-item-versions">
              <option v-for="version in itemVersions" :key="version" :value="version" />
            </datalist>
          </label>
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
import { useRouter } from 'vue-router'
import { fetchBoms } from '@/api/bom'
import { fetchEquipments } from '@/api/equipment'
import { fetchItems } from '@/api/item'
import { fetchProcesses } from '@/api/process'
import {
  cancelWorkOrder,
  createWorkOrder,
  deleteWorkOrder,
  fetchWorkOrders,
  startWorkOrder,
  updateWorkOrder
} from '@/api/workOrder'

const router = useRouter()
const planDateFrom = ref(oneMonthAgo())
const planDateTo = ref(today())
const rows = ref([])
const items = ref([])
const processes = ref([])
const equipments = ref([])
const boms = ref([])
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const acting = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const isEdit = ref(false)
const selectedWorkOrderId = ref('')
const form = reactive(emptyForm())

const itemOptions = computed(() => items.value.filter((item) => isUsable(item.useYn) && String(item.itemId || '').startsWith('2')))
const processOptions = computed(() => processes.value.filter((process) => {
  if (!isUsable(process.useYn)) return false
  return (process.processName || '').includes('배합')
}))
const equipmentOptions = computed(() => {
  const mixProcessIds = new Set(processOptions.value.map((process) => process.processId))
  return equipments.value.filter((equipment) => equipment.processId === form.processId && mixProcessIds.has(equipment.processId))
})
const selectedRow = computed(() => rows.value.find((row) => row.workOrderId === selectedWorkOrderId.value) || null)
const isSelectedClosed = computed(() => isClosed(selectedRow.value))
const canChangeSelected = computed(() => Boolean(selectedRow.value) && !isClosed(selectedRow.value) && !isLockedState(selectedRow.value))
const actionHint = computed(() => {
  const row = selectedRow.value
  if (!row || canChangeSelected.value) {
    return ''
  }
  const state = displayState(row)
  if (state === '완료') {
    return '완료된 작업지시는 작업시작, 작업중단 할 수 없습니다'
  }
  if (state === '작업시작') {
    return '시작된 작업지시는 작업시작, 작업중단 할 수 없습니다'
  }
  if (state === '작업중단') {
    return '작업중단된 작업지시는 작업시작, 작업중단 할 수 없습니다'
  }
  return `${state} 상태의 작업지시는 작업시작, 작업중단 할 수 없습니다`
})
const canDeleteSelected = computed(() => {
  const row = selectedRow.value
  return Boolean(row) && !isClosed(row) && row.resultStatus === '미등록'
})
const itemVersions = computed(() => {
  const versions = []
  for (const bom of boms.value) {
    if (bom.itemId !== form.itemId && bom.bomId !== form.itemId) {
      continue
    }
    if (bom.bomVersion && !versions.includes(bom.bomVersion)) {
      versions.push(bom.bomVersion)
    }
  }
  return versions
})

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

function emptyForm() {
  return {
    workOrderId: '',
    planDate: today(),
    processId: '',
    equipId: '',
    itemId: '',
    state: '작업지시',
    unit: 'Kg',
    planQty: '',
    itemVersion: ''
  }
}

function assignForm(source) {
  form.workOrderId = source.workOrderId || ''
  form.planDate = source.planDate || today()
  form.processId = source.processId || ''
  form.equipId = source.equipId || ''
  if (!processOptions.value.some((process) => process.processId === form.processId)) {
    form.processId = ''
    form.equipId = ''
  } else if (!equipmentOptions.value.some((equipment) => equipment.equipId === form.equipId)) {
    form.equipId = ''
  }
  form.itemId = source.itemId || ''
  form.state = source.state || ''
  form.unit = source.unit || 'Kg'
  form.planQty = source.planQty ?? ''
  form.itemVersion = source.itemVersion || ''
}

function processLabel(row) {
  if (!row?.processId && !row?.processName) return ''
  return [row.processId, row.processName].filter(Boolean).join(' ')
}

function equipmentLabel(row) {
  if (!row?.equipId && !row?.equipName) return ''
  return [row.equipId, row.equipName].filter(Boolean).join(' ')
}

function isUsable(useYn) {
  return !useYn || String(useYn).toUpperCase() === 'Y'
}

function openProductionResult(row) {
  router.push({
    path: '/production/management/result',
    query: {
      workOrderId: row.workOrderId || '',
      planDate: row.planDate || '',
      workcenterId: row.workcenterId || ''
    }
  })
}

function displayState(row) {
  if (!row) {
    return ''
  }
  if (isClosed(row)) {
    return '완료'
  }
  const state = row.state || ''
  if (state === '시작' || state === '믹스시작') {
    return '작업시작'
  }
  if (state === '취소') {
    return '작업중단'
  }
  return state || '작업지시'
}

function isClosed(row) {
  if (!row) {
    return false
  }
  return String(row.closeYn || '').toUpperCase() === 'Y' || row.resultStatus === '완료'
}

function isLockedState(row) {
  const state = row?.state || ''
  return state === '취소' || state === '시작' || state === '믹스시작'
}

function selectRow(row) {
  selectedWorkOrderId.value = row.workOrderId
}

function openCreate() {
  isEdit.value = false
  formError.value = ''
  assignForm(emptyForm())
  showForm.value = true
}

function openEdit() {
  if (!selectedRow.value) {
    errorMessage.value = '수정할 작업지시를 선택하세요.'
    return
  }
  if (isClosed(selectedRow.value)) {
    errorMessage.value = '마감된 작업지시는 수정할 수 없습니다.'
    return
  }
  isEdit.value = true
  formError.value = ''
  assignForm(selectedRow.value)
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  formError.value = ''
}

function onSelectItem() {
  if (form.itemVersion && itemVersions.value.includes(form.itemVersion)) {
    return
  }
  form.itemVersion = itemVersions.value[0] || ''
}

function onSelectProcess() {
  if (!equipmentOptions.value.some((equipment) => equipment.equipId === form.equipId)) {
    form.equipId = ''
  }
}

function toPayload() {
  return {
    workOrderId: form.workOrderId || null,
    planDate: form.planDate || null,
    processId: form.processId || null,
    equipId: form.equipId || null,
    itemId: form.itemId || null,
    state: form.state || null,
    unit: form.unit || 'Kg',
    planQty: form.planQty === '' || form.planQty === null || form.planQty === undefined ? null : Number(form.planQty),
    itemVersion: form.itemVersion || null
  }
}

function includePlanDate(planDate) {
  if (!planDate) {
    return
  }
  if (!planDateFrom.value || planDate < planDateFrom.value) {
    planDateFrom.value = planDate
  }
  if (!planDateTo.value || planDate > planDateTo.value) {
    planDateTo.value = planDate
  }
}

async function saveForm() {
  if (!form.processId) {
    formError.value = '공정을 선택하세요.'
    return
  }
  if (!form.equipId) {
    formError.value = '설비를 선택하세요.'
    return
  }
  if (!form.itemId || !String(form.itemId).startsWith('2')) {
    formError.value = '제품(2번 코드)을 선택하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    const payload = toPayload()
    const { data } = isEdit.value
      ? await updateWorkOrder(selectedWorkOrderId.value, payload)
      : await createWorkOrder(payload)
    const savedId = data?.workOrderId || form.workOrderId
    showForm.value = false
    includePlanDate(data?.planDate || form.planDate)
    selectedWorkOrderId.value = savedId || selectedWorkOrderId.value
    await loadWorkOrders()
  } catch (error) {
    formError.value = error.response?.data?.message || '작업지시 저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function onCancel() {
  if (!selectedRow.value) {
    return
  }
  if (!window.confirm(`작업지시 ${selectedRow.value.workOrderId}를 작업중단하시겠습니까?`)) {
    return
  }
  acting.value = true
  errorMessage.value = ''
  try {
    await cancelWorkOrder(selectedRow.value.workOrderId)
    await loadWorkOrders()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '작업중단에 실패했습니다.'
  } finally {
    acting.value = false
  }
}

async function onStart() {
  if (!selectedRow.value) {
    return
  }
  if (!window.confirm(`작업지시 ${selectedRow.value.workOrderId}를 작업시작하시겠습니까?`)) {
    return
  }
  acting.value = true
  errorMessage.value = ''
  try {
    await startWorkOrder(selectedRow.value.workOrderId)
    await loadWorkOrders()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '작업시작에 실패했습니다.'
  } finally {
    acting.value = false
  }
}

async function onDelete() {
  if (!selectedRow.value) {
    errorMessage.value = '삭제할 작업지시를 선택하세요.'
    return
  }
  if (!window.confirm(`작업지시 ${selectedRow.value.workOrderId}를 삭제하시겠습니까?`)) {
    return
  }

  deleting.value = true
  errorMessage.value = ''
  try {
    await deleteWorkOrder(selectedRow.value.workOrderId)
    selectedWorkOrderId.value = ''
    await loadWorkOrders()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '작업지시 삭제에 실패했습니다.'
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

async function loadProcesses() {
  try {
    const { data } = await fetchProcesses()
    processes.value = Array.isArray(data) ? data : []
  } catch (error) {
    processes.value = []
    errorMessage.value = error.response?.data?.message || '공정 조회에 실패했습니다.'
  }
}

async function loadEquipments() {
  try {
    const { data } = await fetchEquipments()
    equipments.value = Array.isArray(data) ? data : []
  } catch (error) {
    equipments.value = []
    errorMessage.value = error.response?.data?.message || '설비 조회에 실패했습니다.'
  }
}

async function loadBoms() {
  try {
    const { data } = await fetchBoms()
    boms.value = Array.isArray(data) ? data : []
  } catch (error) {
    boms.value = []
  }
}

async function loadWorkOrders() {
  if (!planDateFrom.value || !planDateTo.value) {
    errorMessage.value = '계획일 기간을 입력하세요.'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchWorkOrders(planDateFrom.value, planDateTo.value)
    rows.value = Array.isArray(data) ? data : []
    if (selectedWorkOrderId.value && !rows.value.some((row) => row.workOrderId === selectedWorkOrderId.value)) {
      selectedWorkOrderId.value = ''
    }
  } catch (error) {
    rows.value = []
    errorMessage.value = error.response?.data?.message || '작업지시 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadItems(), loadBoms(), loadProcesses(), loadEquipments()])
  await loadWorkOrders()
})
</script>

<style scoped>
.work-order-note {
  margin: 8px 0 0;
  text-align: right;
}
</style>
