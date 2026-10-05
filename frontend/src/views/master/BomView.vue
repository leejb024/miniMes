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
        <button type="button" class="search-btn" @click="openCreate">등록</button>
        <button type="button" class="ghost-btn" :disabled="!selectedBom || itemLoading" @click="openEdit">수정</button>
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

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal-card">
        <div class="modal-head">
          <h2>{{ isEdit ? 'BOM 수정' : 'BOM 등록' }}</h2>
          <button type="button" class="ghost-btn" @click="closeForm">닫기</button>
        </div>
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="form-grid">
          <label>
            제품
            <select v-model="form.itemId" :disabled="isEdit">
              <option value="">제품을 선택하세요</option>
              <option v-for="product in products" :key="product.itemId" :value="product.itemId">
                {{ product.itemId }} {{ product.itemName }}
              </option>
            </select>
          </label>
          <label>
            BOM 버전
            <input v-model.trim="form.bomVersion" type="text" :readonly="isEdit" />
          </label>
        </div>
        <div class="section-head">
          <h2>원료</h2>
          <button type="button" class="search-btn" @click="addMaterial">원료 추가</button>
        </div>
        <div class="table-wrap">
          <table class="data-table form-table">
            <thead>
              <tr>
                <th>원료</th>
                <th>품명</th>
                <th>수량</th>
                <th>단위</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="form.materials.length === 0">
                <td colspan="5" class="empty">원료를 추가하세요.</td>
              </tr>
              <tr v-for="(line, index) in form.materials" :key="index">
                <td>
                  <select v-model="line.materialId">
                    <option value="">원료를 선택하세요</option>
                    <option
                      v-for="material in materialOptions(line)"
                      :key="material.itemId"
                      :value="material.itemId"
                    >
                      {{ material.itemId }} {{ material.itemName }}
                    </option>
                  </select>
                </td>
                <td>{{ materialName(line.materialId) }}</td>
                <td>
                  <input v-model="line.qty" type="number" min="0" step="0.00001" />
                </td>
                <td>
                  <input v-model.trim="line.unit" type="text" />
                </td>
                <td>
                  <button type="button" class="danger-btn" @click="removeMaterial(index)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p class="hint">제품은 품번이 2로 시작하는 품목이고, 원료는 품번이 1로 시작하는 품목입니다.</p>
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
import { createBom, fetchBomMaterials, fetchBoms, updateBom } from '@/api/bom'
import { fetchItems } from '@/api/item'

const boms = ref([])
const materials = ref([])
const items = ref([])
const selectedBom = ref(null)
const selectedKey = ref('')
const loading = ref(false)
const itemLoading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const formError = ref('')
const itemId = ref('')
const itemName = ref('')
const showForm = ref(false)
const isEdit = ref(false)
const form = reactive({
  itemId: '',
  bomVersion: '00',
  materials: []
})

const products = computed(() => items.value.filter((item) => isProductCode(item.itemId) && item.itemType === '2' && isUsable(item)))
const rawMaterials = computed(() => items.value.filter((item) => isRawMaterialCode(item.itemId) && isUsable(item)))

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

function isRawMaterialCode(value) {
  return String(value || '').startsWith('1')
}

function isUsable(item) {
  return !item.useYn || String(item.useYn).toUpperCase() === 'Y'
}

function emptyLine() {
  return {
    materialId: '',
    qty: '',
    unit: 'Kg'
  }
}

function materialOptions(line) {
  const used = new Set(
    form.materials
      .map((row) => row.materialId)
      .filter((id) => id && id !== line.materialId)
  )
  return rawMaterials.value.filter((item) => !used.has(item.itemId))
}

function materialName(materialId) {
  const item = rawMaterials.value.find((row) => row.itemId === materialId)
  return item ? item.itemName : ''
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
    const rows = Array.isArray(data) ? data : []
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
    const rows = Array.isArray(data) ? data : []
    materials.value = rows.filter((item) => isRawMaterialCode(item.materialId))
  } catch (error) {
    materials.value = []
    errorMessage.value = error.response?.data?.message || '자재 조회에 실패했습니다.'
  } finally {
    itemLoading.value = false
  }
}

function resetForm() {
  form.itemId = ''
  form.bomVersion = '00'
  form.materials = [emptyLine()]
  formError.value = ''
}

function openCreate() {
  isEdit.value = false
  resetForm()
  showForm.value = true
}

function openEdit() {
  if (!selectedBom.value) {
    return
  }
  isEdit.value = true
  form.itemId = selectedBom.value.itemId || ''
  form.bomVersion = selectedBom.value.bomVersion || ''
  form.materials = materials.value.length
    ? materials.value.map((row) => ({
      materialId: row.materialId || '',
      qty: row.qty ?? '',
      unit: row.unit || 'Kg'
    }))
    : [emptyLine()]
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  formError.value = ''
}

function addMaterial() {
  form.materials.push(emptyLine())
}

function removeMaterial(index) {
  form.materials.splice(index, 1)
}

async function saveForm() {
  if (!form.itemId) {
    formError.value = '제품을 선택하세요.'
    return
  }
  if (!form.bomVersion) {
    formError.value = 'BOM 버전을 입력하세요.'
    return
  }
  if (form.materials.length === 0) {
    formError.value = '원료를 한 건 이상 등록하세요.'
    return
  }
  const materialsPayload = []
  for (const line of form.materials) {
    if (!line.materialId) {
      formError.value = '원료를 선택하세요.'
      return
    }
    const qty = Number(line.qty)
    if (!Number.isFinite(qty) || qty <= 0) {
      formError.value = '수량은 0보다 커야 합니다.'
      return
    }
    materialsPayload.push({
      materialId: line.materialId,
      qty,
      unit: line.unit || null
    })
  }
  saving.value = true
  formError.value = ''
  const payload = {
    itemId: form.itemId,
    bomVersion: form.bomVersion,
    materials: materialsPayload
  }
  const key = [form.itemId, form.bomVersion].join('|')
  try {
    if (isEdit.value) {
      await updateBom(payload)
    } else {
      await createBom(payload)
    }
    showForm.value = false
    await loadBoms(key)
  } catch (error) {
    formError.value = error.response?.data?.message || 'BOM 저장에 실패했습니다.'
  } finally {
    saving.value = false
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
