<template>
  <div class="page-card">
    <div class="page-header">
      <h1>품목관리</h1>
      <div class="section-actions">
        <button type="button" class="search-btn" @click="openCreate">등록</button>
        <button type="button" class="danger-btn" :disabled="!selectedItemId || deleting" @click="onDelete">삭제</button>
        <button type="button" class="search-btn" :disabled="loading" @click="loadItems">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ITEM_ID</th>
            <th>ITEM_NAME</th>
            <th>ITEM_TYPE</th>
            <th>USE_YN</th>
            <th>CRE_ID</th>
            <th>CRE_DT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="empty">조회 중...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="6" class="empty">조회된 품목이 없습니다.</td>
          </tr>
          <tr
            v-for="item in items"
            v-else
            :key="item.itemId"
            :class="{ selected: selectedItemId === item.itemId, clickable: true }"
            @click="selectedItemId = item.itemId"
          >
            <td>{{ item.itemId }}</td>
            <td>{{ item.itemName }}</td>
            <td>{{ item.itemType }}</td>
            <td>{{ item.useYn }}</td>
            <td>{{ item.creId }}</td>
            <td>{{ formatDate(item.creDt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>품목 등록</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            구분
            <select v-model="form.kind" @change="onChangeKind">
              <option value="">선택하세요</option>
              <option value="RAW">원재료</option>
            </select>
          </label>
          <label>
            ITEM_ID
            <input :value="form.itemId" type="text" readonly placeholder="원재료 선택 시 자동 채번" />
          </label>
          <label>
            ITEM_NAME
            <input v-model.trim="form.itemName" type="text" :disabled="form.kind !== 'RAW'" />
          </label>
          <label>
            ITEM_TYPE
            <input value="6" type="text" readonly />
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="ghost-btn" :disabled="saving" @click="closeForm">취소</button>
          <button type="button" class="search-btn" :disabled="saving" @click="saveItem">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createRawItem, deleteItem, fetchItems, fetchNextRawItemId } from '@/api/item'

const items = ref([])
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const selectedItemId = ref('')
const errorMessage = ref('')
const formError = ref('')
const showForm = ref(false)
const form = reactive({
  kind: '',
  itemId: '',
  itemName: ''
})

function formatDate(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadItems() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchItems()
    items.value = Array.isArray(data) ? data : []
    if (!items.value.some((item) => item.itemId === selectedItemId.value)) {
      selectedItemId.value = ''
    }
  } catch (error) {
    items.value = []
    errorMessage.value = error.response?.data?.message || '품목 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.kind = ''
  form.itemId = ''
  form.itemName = ''
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  formError.value = ''
}

async function onChangeKind() {
  formError.value = ''
  form.itemId = ''
  if (form.kind !== 'RAW') {
    return
  }
  try {
    const { data } = await fetchNextRawItemId()
    form.itemId = data?.itemId || ''
  } catch (error) {
    formError.value = error.response?.data?.message || '품번 채번에 실패했습니다.'
  }
}

async function saveItem() {
  if (form.kind !== 'RAW') {
    formError.value = '원재료를 선택하세요.'
    return
  }
  if (!form.itemName) {
    formError.value = '품목명을 입력하세요.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await createRawItem({ itemName: form.itemName })
    showForm.value = false
    await loadItems()
  } catch (error) {
    formError.value = error.response?.data?.message || '품목 등록에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function onDelete() {
  const item = items.value.find((row) => row.itemId === selectedItemId.value)
  if (!item) {
    errorMessage.value = '삭제할 품목을 선택하세요.'
    return
  }
  if (!window.confirm(`품목 ${item.itemId} ${item.itemName}을 삭제하시겠습니까?`)) {
    return
  }
  deleting.value = true
  errorMessage.value = ''
  try {
    await deleteItem(item.itemId)
    selectedItemId.value = ''
    await loadItems()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '품목 삭제에 실패했습니다.'
  } finally {
    deleting.value = false
  }
}

onMounted(loadItems)
</script>
