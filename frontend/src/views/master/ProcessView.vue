<template>
  <div class="page-card">
    <div class="page-header">
      <h1>공정관리</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" @click="openCreate">등록</button>
        <button type="button" class="ghost-btn" :disabled="!selectedProcessId" @click="openEdit">수정</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadProcesses()">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>PROCESS_ID</th>
            <th>PROCESS_NAME</th>
            <th>USE_YN</th>
            <th>CRE_ID</th>
            <th>CRE_DT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="processes.length === 0">
            <td colspan="5" class="empty">조회된 공정이 없습니다.</td>
          </tr>
          <tr
            v-for="process in processes"
            v-else
            :key="process.processId"
            :class="{ selected: selectedProcessId === process.processId, clickable: true }"
            @click="selectedProcessId = process.processId"
          >
            <td>{{ process.processId }}</td>
            <td>{{ process.processName }}</td>
            <td>{{ process.useYn }}</td>
            <td>{{ process.creId }}</td>
            <td>{{ formatDate(process.creDt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>{{ isEdit ? '공정 수정' : '공정 등록' }}</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            공정코드
            <input v-model.trim="form.processId" type="text" :readonly="isEdit" />
          </label>
          <label>
            공정명
            <input v-model.trim="form.processName" type="text" />
          </label>
          <label>
            사용여부
            <select v-model="form.useYn">
              <option value="Y">Y</option>
              <option value="N">N</option>
            </select>
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
import { createProcess, fetchProcesses, updateProcess } from '@/api/process'

const processes = ref([])
const selectedProcessId = ref('')
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const isEdit = ref(false)
const form = reactive(emptyForm())

function emptyForm() {
  return {
    processId: '',
    processName: '',
    useYn: 'Y'
  }
}

function formatDate(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadProcesses(preserveId = '') {
  const processId = typeof preserveId === 'string' ? preserveId : ''
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchProcesses()
    processes.value = Array.isArray(data) ? data : []
    if (processId && processes.value.some((process) => process.processId === processId)) {
      selectedProcessId.value = processId
    } else if (!processes.value.some((process) => process.processId === selectedProcessId.value)) {
      selectedProcessId.value = ''
    }
  } catch (error) {
    processes.value = []
    selectedProcessId.value = ''
    errorMessage.value = error.response?.data?.message || '공정 조회에 실패했습니다.'
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
  const process = processes.value.find((row) => row.processId === selectedProcessId.value)
  if (!process) {
    return
  }
  isEdit.value = true
  form.processId = process.processId || ''
  form.processName = process.processName || ''
  form.useYn = process.useYn === 'N' ? 'N' : 'Y'
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  formError.value = ''
}

async function saveForm() {
  if (!form.processId) {
    formError.value = '공정코드를 입력하세요.'
    return
  }
  if (!form.processName) {
    formError.value = '공정명을 입력하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  const payload = {
    processId: form.processId,
    processName: form.processName,
    useYn: form.useYn
  }
  try {
    if (isEdit.value) {
      await updateProcess(payload)
    } else {
      await createProcess(payload)
    }
    showForm.value = false
    await loadProcesses(form.processId)
  } catch (error) {
    formError.value = error.response?.data?.message || '공정 저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

onMounted(loadProcesses)
</script>
