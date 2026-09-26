import http from './http'

export function fetchPurchaseLots(params) {
  return http.get('/purchase/lots', { params })
}

export function scanPurchaseInfo(payload) {
  return http.post('/purchase/scanInfo', payload)
}

export function createPurchaseLot(payload) {
  return http.post('/purchase/createLot', payload)
}

export function updatePurchaseQty(payload) {
  return http.put('/purchase/updateQty', payload)
}

export function deletePurchaseQty(lotSeq) {
  return http.delete('/purchase/deleteQty', { params: { lotSeq } })
}

export function returnPurchaseLot(payload) {
  return http.post('/purchase/returnLot', payload)
}

export function deleteReturnQty(lotSeq) {
  return http.delete('/purchase/delete/ReturnQty', { params: { lotSeq } })
}
