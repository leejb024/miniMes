<template>
  <div class="page-card">
    <div class="page-header">
      <h1>BOM 관리</h1>
      <div class="filter-bar">
        <label>
          품번
          <input v-model.trim="itemId" type="text" @keyup.enter="loadBoms" />
        </label>
        <label>
          품명
          <input v-model.trim="itemName" type="text" @keyup.enter="loadBoms" />
        </label>
        <button type="button" class="search-btn" :disabled="loading" @click="loadBoms()">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="split-grid">
      <section class="grid-section">
        <h2>BOM 리스트</h2>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>BOM_ID</th>
                <th>BOM_VERSION</th>
                <th>ITEM_ID</th>
                <th>ITEM_NAME</th>
                <th>USE_YN</th>
                <th>CRE_ID</th>
                <th>CRE_DT</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="7" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="boms.length === 0">
                <td colspan="7" class="empty">조회된 BOM이 없습니다.</td>
              </tr>
              <tr
                v-for="bom in boms"
                v-else
                :key="headerKey(bom)"
                :class="{ selected: selectedKey === headerKey(bom), clickable: true }"
                @click="selectBom(bom)"
              >
                <td>{{ bom.bomId }}</td>
                <td>{{ bom.bomVersion }}</td>
                <td>{{ bom.itemId }}</td>
                <td>{{ bom.itemName }}</td>
                <td>{{ bom.useYn }}</td>
                <td>{{ bom.creId }}</td>
                <td>{{ formatDate(bom.creDt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="grid-section">
        <h2>자재 리스트{{ selectedBom ? ` (${selectedBom.itemId} / ${selectedBom.bomVersion})` : '' }}</h2>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>MATERIAL_ID</th>
                <th>ITEM_ID</th>
                <th>ITEM_NAME</th>
                <th>QTY</th>
                <th>UNIT</th>
                <th>BOM_SEQ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="itemLoading">
                <td colspan="6" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="!selectedBom">
                <td colspan="6" class="empty">상단 BOM을 선택하세요.</td>
              </tr>
              <tr v-else-if="materials.length === 0">
                <td colspan="6" class="empty">조회된 자재가 없습니다.</td>
              </tr>
              <tr v-for="item in materials" v-else :key="item.bomSeq">
                <td>{{ item.materialId }}</td>
                <td>{{ item.itemId }}</td>
                <td>{{ item.itemName }}</td>
                <td>{{ item.qty }}</td>
                <td>{{ item.unit }}</td>
                <td>{{ item.bomSeq }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchBomMaterials, fetchBoms } from '@/api/bom'
import { fetchItems } from '@/api/item'

const boms = ref([])
const materials = ref([])
const items = ref([])
const selectedBom = ref(null)
const selectedKey = ref('')
const loading = ref(false)
const itemLoading = ref(false)
const errorMessage = ref('')
const itemId = ref('')
const itemName = ref('')

function headerKey(bom) {
  return [bom.itemId, bom.bomVersion].join('|')
}

function formatDate(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

function isProductCode(value) {
  return String(value || '').startsWith('2')
}

function matchesKeyword(value, keyword) {
  const text = keyword.trim().toLowerCase()
  if (!text) {
    return true
  }
  return String(value || '').toLowerCase().includes(text)
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

async function loadBoms(preserveKey = '') {
  const key = typeof preserveKey === 'string' ? preserveKey : ''
  loading.value = true
  errorMessage.value = ''
  selectedBom.value = null
  selectedKey.value = ''
  materials.value = []
  try {
    const { data } = await fetchBoms({
      itemId: itemId.value,
      itemName: itemName.value,
      productsOnly: true
    })
    const rows = Array.isArray(data) ? data : [];
 
    boms.value = rows.filter((bom) =>
      isProductCode(bom.itemId)
      && matchesKeyword(bom.itemId, itemId.value)
      && matchesKeyword(bom.itemName, itemName.value)
    )
  
  } catch (error) {
    boms.value = []
    errorMessage.value = error.response?.data?.message || 'BOM 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }

  if (!key) {
    return
  }
  
  const saved = boms.value.find((bom) => headerKey(bom) === key)
  if (saved) {
    await selectBom(saved)
  }
}

async function selectBom(bom) {
  selectedBom.value = bom
  selectedKey.value = headerKey(bom)
  itemLoading.value = true
  errorMessage.value = ''
  try {
    const { data } = await fetchBomMaterials(bom.itemId, bom.bomVersion)
    materials.value = Array.isArray(data) ? data : []
  } catch (error) {
    materials.value = []
    errorMessage.value = error.response?.data?.message || '자재 조회에 실패했습니다.'
  } finally {
    itemLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadItems(), loadBoms()])
})
</script>
<style scoped>
.filter-bar input[type='text'] {
  width: 160px;
}
</style>