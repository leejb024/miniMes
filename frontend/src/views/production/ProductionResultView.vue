<template>
  <div class="page-card">
    <div class="page-header">
      <h1>생산실적</h1>
      <div class="filter-bar">
        <label>
          부서
          <input v-model.trim="deptId" type="text" placeholder="DEPT_ID" />
        </label>
        <label>
          작업일자
          <input v-model="planDateFrom" type="date" />
        </label>
        <span class="filter-sep">~</span>
        <input v-model="planDateTo" type="date" />
        <button type="button" class="search-btn" :disabled="loading" @click="loadWorkOrders">조회</button>
      </div>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div class="split-grid result-layout">
      <section class="grid-section">
        <h2>작업 지시 리스트</h2>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th class="check-col"></th>
                <th>No.</th>
                <th>작업 일자</th>
                <th>생산 부서</th>
                <th>워크센터명</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="5" class="empty">조회 중...</td>
              </tr>
              <tr v-else-if="workOrders.length === 0">
                <td colspan="5" class="empty">조회된 작업지시가 없습니다.</td>
              </tr>
              <tr
                v-for="(row, index) in workOrders"
                v-else
                :key="rowKey(row)"
                :class="{ selected: selectedKey === rowKey(row), clickable: true }"
                @click="selectWorkOrder(row)"
              >
                <td class="check-col">
                  <input type="checkbox" :checked="selectedKey === rowKey(row)" @click.stop="selectWorkOrder(row)" />
                </td>
                <td>{{ index + 1 }}</td>
                <td>{{ row.planDate }}</td>
                <td>{{ row.deptName || row.deptId }}</td>
                <td>{{ row.workcenterName || row.workcenterId }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="grid-section">
        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            type="button"
            class="tab"
            :class="{ active: activeTab === tab.id }"
            @click="activeTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>

        <div v-if="activeTab === 'good'" class="master-detail">
          <div>
            <div class="section-head">
              <h2>양품 실적 리스트</h2>
              <span class="result-count">검색결과 : {{ goodRows.length }}건</span>
            </div>
            <div class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th class="check-col"></th>
                    <th>LOT NO</th>
                    <th class="required">시작일시</th>
                    <th class="required">종료일시</th>
                    <th>실적 수량</th>
                    <th>WMS 생산수량</th>
                    <th>WMS 확정수량</th>
                    <th>단위</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="tabLoading">
                    <td colspan="8" class="empty">조회 중...</td>
                  </tr>
                  <tr v-else-if="!selectedWorkOrder">
                    <td colspan="8" class="empty">상단 작업지시를 선택하세요.</td>
                  </tr>
                  <tr v-else-if="goodRows.length === 0">
                    <td colspan="8" class="empty">조회된 양품 실적이 없습니다.</td>
                  </tr>
                  <tr
                    v-for="item in goodRows"
                    v-else
                    :key="item.lotId"
                    :class="{ selected: selectedGoodKey === goodKey(item), clickable: true }"
                    @click="selectGoodRow(item)"
                  >
                    <td class="check-col">
                      <input type="checkbox" :checked="selectedGoodKey === goodKey(item)" @click.stop="selectGoodRow(item)" />
                    </td>
                    <td>{{ item.lotId }}</td>
                    <td>{{ formatDateTime(item.productionStartTime) }}</td>
                    <td>{{ formatDateTime(item.productionEndTime) }}</td>
                    <td>{{ formatQty(item.prodQty) }}</td>
                    <td>{{ formatQty(item.wmsProdQty) }}</td>
                    <td>{{ formatQty(item.wmsConfirmQty) }}</td>
                    <td>{{ item.unit }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div>
            <h2>양품 실적 상세 리스트</h2>
            <div class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>No.</th>
                    <th>시작일시</th>
                    <th>종료일시</th>
                    <th>수량</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="detailLoading">
                    <td colspan="4" class="empty">조회 중...</td>
                  </tr>
                  <tr v-else-if="!selectedGoodRow">
                    <td colspan="4" class="empty">좌측 양품 실적을 선택하세요.</td>
                  </tr>
                  <tr v-else-if="goodDetails.length === 0">
                    <td colspan="4" class="empty">조회된 상세 실적이 없습니다.</td>
                  </tr>
                  <tr v-for="(item, index) in goodDetails" v-else :key="item.prodResultSeq || index">
                    <td>{{ index + 1 }}</td>
                    <td>{{ formatDateTime(item.productionStartTime) }}</td>
                    <td>{{ formatDateTime(item.productionEndTime) }}</td>
                    <td>{{ formatQty(item.prodQty) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div v-else-if="activeTab === 'material'" class="master-detail">
          <div>
            <div class="section-head">
              <h2>자재투입 실적 리스트</h2>
              <span class="result-count">검색결과 : {{ materialRows.length }}건</span>
            </div>
            <div class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th class="check-col"></th>
                    <th>No.</th>
                    <th>자재코드</th>
                    <th>수량</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!selectedWorkOrder">
                    <td colspan="4" class="empty">상단 작업지시를 선택하세요.</td>
                  </tr>
                  <tr v-else-if="materialRows.length === 0">
                    <td colspan="4" class="empty">조회된 자재투입 실적이 없습니다.</td>
                  </tr>
                  <tr
                    v-for="(item, index) in materialRows"
                    v-else
                    :key="item.id || index"
                    :class="{ selected: selectedMaterialKey === materialKey(item, index), clickable: true }"
                    @click="selectMaterialRow(item, index)"
                  >
                    <td class="check-col">
                      <input
                        type="checkbox"
                        :checked="selectedMaterialKey === materialKey(item, index)"
                        @click.stop="selectMaterialRow(item, index)"
                      />
                    </td>
                    <td>{{ index + 1 }}</td>
                    <td>{{ item.materialId }}</td>
                    <td>{{ formatQty(item.qty) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div>
            <h2>자재투입 실적 상세 리스트</h2>
            <div class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>No.</th>
                    <th>시작일시</th>
                    <th>종료일시</th>
                    <th>수량</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!selectedMaterialRow">
                    <td colspan="4" class="empty">좌측 자재투입 실적을 선택하세요.</td>
                  </tr>
                  <tr v-else-if="materialDetails.length === 0">
                    <td colspan="4" class="empty">조회된 상세 실적이 없습니다.</td>
                  </tr>
                  <tr v-for="(item, index) in materialDetails" v-else :key="index">
                    <td>{{ index + 1 }}</td>
                    <td>{{ formatDateTime(item.productionStartTime) }}</td>
                    <td>{{ formatDateTime(item.productionEndTime) }}</td>
                    <td>{{ formatQty(item.qty) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div v-else>
          <h2>공수 리스트</h2>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>No.</th>
                  <th>작업지시번호</th>
                  <th>내용</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!selectedWorkOrder">
                  <td colspan="3" class="empty">상단 작업지시를 선택하세요.</td>
                </tr>
                <tr v-else-if="laborRows.length === 0">
                  <td colspan="3" class="empty">조회된 공수 데이터가 없습니다.</td>
                </tr>
                <tr v-for="(item, index) in laborRows" v-else :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>{{ item.workOrderId }}</td>
                  <td>{{ item.content }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchProdResultDetails, fetchProdResults } from '@/api/prodResult'
import { fetchWorkOrders } from '@/api/workOrder'

const tabs = [
  { id: 'good', label: '양품 실적' },
  { id: 'material', label: '자재투입 실적' },
  { id: 'labor', label: '공수' }
]

const deptId = ref('27')
const planDateFrom = ref('2026-06-01')
const planDateTo = ref('2026-08-16')
const workOrders = ref([])
const selectedWorkOrder = ref(null)
const selectedKey = ref('')
const activeTab = ref('good')
const loading = ref(false)
const tabLoading = ref(false)
const detailLoading = ref(false)
const errorMessage = ref('')
const goodRows = ref([])
const goodDetails = ref([])
const selectedGoodRow = ref(null)
const selectedGoodKey = ref('')
const materialRows = ref([])
const materialDetails = ref([])
const selectedMaterialRow = ref(null)
const selectedMaterialKey = ref('')
const laborRows = ref([])

function rowKey(row) {
  return [row.workOrderId, row.planDate, row.workcenterId].join('|')
}

function goodKey(item) {
  return [item.workOrderId, item.plantId, item.lotId].join('|')
}

function materialKey(item, index) {
  return item.id || String(index)
}

function formatDateTime(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

function formatQty(value) {
  if (value === null || value === undefined || value === '') {
    return ''
  }
  const number = Number(value)
  if (Number.isNaN(number)) {
    return value
  }
  return number.toLocaleString('ko-KR', { minimumFractionDigits: 5, maximumFractionDigits: 5 })
}

function clearTabRows() {
  goodRows.value = []
  goodDetails.value = []
  selectedGoodRow.value = null
  selectedGoodKey.value = ''
  materialRows.value = []
  materialDetails.value = []
  selectedMaterialRow.value = null
  selectedMaterialKey.value = ''
  laborRows.value = []
}

async function loadWorkOrders() {
  if (!deptId.value || !planDateFrom.value || !planDateTo.value) {
    errorMessage.value = '부서와 작업일자 기간을 입력하세요.'
    return
  }

  loading.value = true
  errorMessage.value = ''
  selectedWorkOrder.value = null
  selectedKey.value = ''
  clearTabRows()
  try {
    const { data } = await fetchWorkOrders(deptId.value, planDateFrom.value, planDateTo.value)
    workOrders.value = Array.isArray(data) ? data : []
  } catch (error) {
    workOrders.value = []
    errorMessage.value = error.response?.data?.message || '작업지시 조회에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function selectWorkOrder(row) {
  selectedWorkOrder.value = row
  selectedKey.value = rowKey(row)
  activeTab.value = 'good'
  tabLoading.value = true
  errorMessage.value = ''
  clearTabRows()
  try {
    const { data } = await fetchProdResults(row.workOrderId)
    goodRows.value = Array.isArray(data) ? data : []
  } catch (error) {
    goodRows.value = []
    errorMessage.value = error.response?.data?.message || '양품 실적 조회에 실패했습니다.'
  } finally {
    tabLoading.value = false
  }
}

async function selectGoodRow(item) {
  selectedGoodRow.value = item
  selectedGoodKey.value = goodKey(item)
  detailLoading.value = true
  try {
    const { data } = await fetchProdResultDetails(item.workOrderId, item.lotId)
    goodDetails.value = Array.isArray(data) ? data : []
  } catch (error) {
    goodDetails.value = []
    errorMessage.value = error.response?.data?.message || '양품 실적 상세 조회에 실패했습니다.'
  } finally {
    detailLoading.value = false
  }
}

function selectMaterialRow(item, index) {
  selectedMaterialRow.value = item
  selectedMaterialKey.value = materialKey(item, index)
  materialDetails.value = []
}

onMounted(loadWorkOrders)
</script>
