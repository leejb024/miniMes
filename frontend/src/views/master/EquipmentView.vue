<template>
  <div class="page-card">
    <div class="page-header">
      <h1>설비관리</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" @click="openCreate">등록</button>
        <button type="button" class="ghost-btn" :disabled="!selectedEquipId" @click="openEdit">수정</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadEquipments()">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>No.</th>
            <th>공장 *</th>
            <th>설비 ID *</th>
            <th>설비명</th>
            <th>공정</th>
            <th>창고/위치 명</th>
            <th>설비 유형</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="equipments.length === 0">
            <td colspan="7" class="empty">조회된 설비가 없습니다.</td>
          </tr>
          <tr
            v-for="(equipment, index) in equipments"
            v-else
            :key="equipment.equipId"
            :class="{ selected: selectedEquipId === equipment.equipId, clickable: true }"
            @click="selectedEquipId = equipment.equipId"
          >
            <td>{{ index + 1 }}</td>
            <td>{{ equipment.plantId }}</td>
            <td>{{ equipment.equipId }}</td>
            <td>{{ equipment.equipName }}</td>
            <td>{{ processLabel(equipment) }}</td>
            <td>{{ equipment.warehouseName }}</td>
            <td>{{ equipment.equipType }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>{{ isEdit ? '설비 수정' : '설비 등록' }}</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            공장 *
            <input v-model.trim="form.plantId" type="text" />
          </label>
          <label>
            설비 ID *
            <input v-model.trim="form.equipId" type="text" :readonly="isEdit" />
          </label>
          <label>
            설비명 *
            <input v-model.trim="form.equipName" type="text" />
          </label>
          <label>
            공정 *
            <select v-model="form.processId">
              <option value="">선택</option>
              <option v-for="process in processes" :key="process.processId" :value="process.processId">
                {{ process.processId }} {{ process.processName }}
              </option>
            </select>
          </label>
          <label>
            창고/위치
            <select v-model="form.warehouseId">
              <option value="">선택 안 함</option>
              <option v-for="warehouse in warehouses" :key="warehouse.warehouseId" :value="warehouse.warehouseId">
                {{ warehouse.warehouseName }}
              </option>
            </select>
          </label>
          <label>
            설비 유형
            <input v-model.trim="form.equipType" type="text" />
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
import { onMounted, reactive, ref } from 'vue'
import { createEquipment, fetchEquipments, updateEquipment } from '@/api/equipment'
import { fetchProcesses } from '@/api/process'
import { fetchWarehouses } from '@/api/warehouse'

const equipments = ref([])
const processes = ref([])
const warehouses = ref([])
const selectedEquipId = ref('')
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const isEdit = ref(false)
const form = reactive(emptyForm())

function emptyForm() {
  return {
    plantId: '1',
    equipId: '',
    equipName: '',
    processId: '',
    warehouseId: '',
    equipType: ''
  }
}

function processLabel(equipment) {
  if (equipment.processName) {
    return `${equipment.processId} ${equipment.processName}`
  }
  return equipment.processId || ''
}

async function loadLookups() {
  const [processResult, warehouseResult] = await Promise.all([fetchProcesses(), fetchWarehouses()])
  processes.value = Array.isArray(processResult.data) ? processResult.data : []
  warehouses.value = Array.isArray(warehouseResult.data) ? warehouseResult.data : []
}

async function loadEquipments(preserveId = '') {
  const equipId = typeof preserveId === 'string' ? preserveId : ''
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchEquipments()
    equipments.value = Array.isArray(data) ? data : []
    if (equipId && equipments.value.some((row) => row.equipId === equipId)) {
      selectedEquipId.value = equipId
    } else if (!equipments.value.some((row) => row.equipId === selectedEquipId.value)) {
      selectedEquipId.value = ''
    }
  } catch (error) {
    equipments.value = []
    selectedEquipId.value = ''
    errorMessage.value = error.response?.data?.message || '설비 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, emptyForm())
  formError.value = ''
  showForm.value = true
}

function openEdit() {
  const equipment = equipments.value.find((row) => row.equipId === selectedEquipId.value)
  if (!equipment) {
    return
  }
  isEdit.value = true
  form.plantId = equipment.plantId || '1'
  form.equipId = equipment.equipId || ''
  form.equipName = equipment.equipName || ''
  form.processId = equipment.processId || ''
  form.warehouseId = equipment.warehouseId || ''
  form.equipType = equipment.equipType || ''
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  formError.value = ''
}

async function saveForm() {
  if (!form.plantId) {
    formError.value = '공장을 입력하세요.'
    return
  }
  if (!form.equipId) {
    formError.value = '설비 ID를 입력하세요.'
    return
  }
  if (!form.equipName) {
    formError.value = '설비명을 입력하세요.'
    return
  }
  if (!form.processId) {
    formError.value = '공정을 선택하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  const payload = {
    plantId: form.plantId,
    equipId: form.equipId,
    equipName: form.equipName,
    processId: form.processId,
    warehouseId: form.warehouseId || null,
    equipType: form.equipType || null
  }
  try {
    if (isEdit.value) {
      await updateEquipment(payload)
    } else {
      await createEquipment(payload)
    }
    showForm.value = false
    await loadEquipments(form.equipId)
  } catch (error) {
    formError.value = error.response?.data?.message || '설비 저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    await loadLookups()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '공정 또는 창고 조회에 실패했습니다.'
  }
  await loadEquipments()
})
</script>
