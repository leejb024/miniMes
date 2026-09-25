<template>
  <div class="page-card">
    <div class="page-header">
      <h1>창고관리</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" @click="openCreate">등록</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadWarehouses">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>창고ID</th>
            <th>창고명</th>
            <th>유형</th>
            <th>사용여부</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="4" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="warehouses.length === 0">
            <td colspan="4" class="empty">조회된 창고가 없습니다.</td>
          </tr>
          <tr v-for="warehouse in warehouses" v-else :key="warehouse.warehouseId">
            <td>{{ warehouse.warehouseId }}</td>
            <td>{{ warehouse.warehouseName }}</td>
            <td>{{ warehouse.warehouseType }}</td>
            <td>{{ warehouse.useYn }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>창고 등록</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            창고ID
            <input v-model.trim="form.warehouseId" type="text" />
          </label>
          <label>
            창고명
            <input v-model.trim="form.warehouseName" type="text" />
          </label>
          <label>
            유형
            <input v-model.trim="form.warehouseType" type="text" />
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
          <button type="button" class="search-btn" :disabled="saving" @click="saveForm">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createWarehouse, fetchWarehouses } from '@/api/warehouse'

const warehouses = ref([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const form = reactive(emptyForm())

function emptyForm() {
  return {
    warehouseId: '',
    warehouseName: '',
    warehouseType: '',
    useYn: 'Y'
  }
}

async function loadWarehouses() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchWarehouses()
    warehouses.value = Array.isArray(data) ? data : []
  } catch (error) {
    warehouses.value = []
    errorMessage.value = error.response?.data?.message || '창고 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, emptyForm())
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function saveForm() {
  if (!form.warehouseId || !form.warehouseName) {
    formError.value = '창고ID와 창고명을 입력하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await createWarehouse({
      warehouseId: form.warehouseId,
      warehouseName: form.warehouseName,
      warehouseType: form.warehouseType || null,
      useYn: form.useYn
    })
    showForm.value = false
    await loadWarehouses()
  } catch (error) {
    formError.value = error.response?.data?.message || '창고 등록에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

onMounted(loadWarehouses)
</script>
