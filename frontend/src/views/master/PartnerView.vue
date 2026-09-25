<template>
  <div class="page-card">
    <div class="page-header">
      <h1>거래처관리</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" @click="openCreate">등록</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadVendors">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>VENDOR_ID</th>
            <th>VENDOR_NAME</th>
            <th>USE_YN</th>
            <th>REG_DT</th>
            <th>MOD_DT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="vendors.length === 0">
            <td colspan="5" class="empty">조회된 거래처가 없습니다.</td>
          </tr>
          <tr v-for="vendor in vendors" v-else :key="vendor.vendorId">
            <td>{{ vendor.vendorId }}</td>
            <td>{{ vendor.vendorName }}</td>
            <td>{{ vendor.useYn }}</td>
            <td>{{ formatDate(vendor.regDt) }}</td>
            <td>{{ formatDate(vendor.modDt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>거래처 등록</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            거래처ID
            <input v-model.trim="form.vendorId" type="text" />
          </label>
          <label>
            거래처명
            <input v-model.trim="form.vendorName" type="text" />
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
import { createVendor, fetchVendors } from '@/api/vendor'

const vendors = ref([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const form = reactive(emptyForm())

function emptyForm() {
  return {
    vendorId: '',
    vendorName: '',
    useYn: 'Y'
  }
}

function formatDate(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadVendors() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchVendors()
    vendors.value = Array.isArray(data) ? data : []
  } catch (error) {
    vendors.value = []
    errorMessage.value = error.response?.data?.message || '거래처 조회에 실패했습니다.'
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
  if (!form.vendorId || !form.vendorName) {
    formError.value = '거래처ID와 거래처명을 입력하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await createVendor({
      vendorId: form.vendorId,
      vendorName: form.vendorName,
      useYn: form.useYn
    })
    showForm.value = false
    await loadVendors()
  } catch (error) {
    formError.value = error.response?.data?.message || '거래처 등록에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

onMounted(loadVendors)
</script>
