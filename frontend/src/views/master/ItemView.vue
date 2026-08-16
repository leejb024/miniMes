<template>
  <div class="page-card">
    <div class="page-header">
      <h1>품목관리</h1>
      <button type="button" class="search-btn" :disabled="loading" @click="loadItems">조회</button>
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
          <tr v-for="item in items" v-else :key="item.itemId">
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchItems } from '@/api/item'

const items = ref([])
const loading = ref(false)
const errorMessage = ref('')

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
  } catch (error) {
    items.value = []
    errorMessage.value = error.response?.data?.message || '품목 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadItems)
</script>
