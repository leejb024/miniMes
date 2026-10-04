import http from './http'

export function fetchProdResults(workOrderId) {
  return http.get('/prod-results', {
    params: { workOrderId }
  })
}

export function fetchProdMaterialInputs(workOrderId) {
  return http.get('/prod-results/materials', {
    params: { workOrderId }
  })
}

export function createProdResult(payload) {
  return http.post('/prod-results', payload)
}

export function completeProdResults(workOrderId) {
  return http.post('/prod-results/complete', { workOrderId })
}

export function updateProdResult(prodResultSeq, payload) {
  return http.put(`/prod-results/${prodResultSeq}`, payload)
}

export function deleteProdResult(prodResultSeq) {
  return http.delete(`/prod-results/${prodResultSeq}`)
}

export function fetchProductionSheet(workOrderId) {
  return http.get('/production/sheet', { params: { workOrderId } })
}

export function fetchInputStocks() {
  return http.get('/production/input/stocks')
}

export function weighInputMaterial(payload) {
  return http.post('/production/input/material/lot/weighing', payload)
}

export function inputProductionMaterial(payload) {
  return http.post('/production/input/material', payload)
}

export function registerInputMaterialLot(payload) {
  return http.post('/production/input/material/lot', payload)
}

export function saveInputCombineResult(payload) {
  return http.post('/production/input/combine/result', payload)
}

export function updateInputComment(payload) {
  return http.post('/production/update/workOrder/inputcomment', payload)
}

export function updateWorkComment(payload) {
  return http.post('/production/update/workOrder/workcomment', payload)
}

export function saveProductionResult(payload) {
  return http.post('/production/result', payload)
}

export function suggestProductionResult(workOrderId) {
  return http.get('/production/result/suggest', { params: { workOrderId } })
}

export function finishWorkOrder(payload) {
  return http.post('/production/finish/workorder', payload)
}
