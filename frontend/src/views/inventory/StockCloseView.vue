<template>
  <div class="page-card">
    <div class="page-header">
      <h1>재고 마감관리</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" @click="openClose">마감</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadCloses">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p class="hint">마감된 월 또는 기준일자 이전의 입고는 수정, 삭제, 반품할 수 없습니다.</p>

    <div class="section-head">
      <h2>마감 리스트</h2>
      <span class="result-count">검색결과 : {{ closes.length }}건</span>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>No.</th>
            <th>구분</th>
            <th>마감월</th>
            <th>기준일자</th>
            <th>비고</th>
            <th>등록자</th>
            <th>등록일시</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="closes.length === 0">
            <td colspan="7" class="empty">마감 내역이 없습니다.</td>
          </tr>
          <tr v-for="(row, index) in closes" v-else :key="row.closeSeq">
            <td>{{ index + 1 }}</td>
            <td>{{ closeLabel(row.closeType) }}</td>
            <td>{{ row.closeMonth }}</td>
            <td>{{ row.baseDate }}</td>
            <td>{{ row.remark }}</td>
            <td>{{ row.creId }}</td>
            <td>{{ formatDateTime(row.creDt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>재고 마감</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            구분
            <select v-model="form.closeType">
              <option value="MONTH">월마감</option>
              <option value="DATE">기준일자</option>
            </select>
          </label>
          <label v-if="form.closeType === 'MONTH'">
            마감월
            <input v-model="form.closeMonth" type="month" :max="thisMonth()" />
          </label>
          <label v-else>
            기준일자
            <input v-model="form.baseDate" type="date" :max="today()" />
          </label>
          <label>
            비고
            <input v-model.trim="form.remark" type="text" />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeForm">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveClose">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { closeStock, fetchStockCloses } from '@/api/stock'

const closes = ref([])
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const form = reactive({
  closeType: 'MONTH',
  closeMonth: '',
  baseDate: '',
  remark: ''
})

function today() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function thisMonth() {
  return today().slice(0, 7)
}

function formatDateTime(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

function closeLabel(type) {
  return type === 'DATE' ? '기준일자' : '월마감'
}

async function loadCloses() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchStockCloses()
    closes.value = Array.isArray(data) ? data : []
  } catch (error) {
    closes.value = []
    errorMessage.value = error.response?.data?.message || '마감 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

function openClose() {
  form.closeType = 'MONTH'
  form.closeMonth = thisMonth()
  form.baseDate = today()
  form.remark = ''
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

async function saveClose() {
  if (form.closeType === 'MONTH' && !form.closeMonth) {
    formError.value = '마감월을 선택하세요.'
    return
  }
  if (form.closeType === 'DATE' && !form.baseDate) {
    formError.value = '기준일자를 선택하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await closeStock({
      closeType: form.closeType,
      closeMonth: form.closeType === 'MONTH' ? form.closeMonth : null,
      baseDate: form.closeType === 'DATE' ? form.baseDate : null,
      remark: form.remark
    })
    showForm.value = false
    await loadCloses()
  } catch (error) {
    formError.value = error.response?.data?.message || '재고 마감에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

onMounted(loadCloses)
</script>
