import http from './http'

export function fetchPurchaseOrders() {
  return http.get('/purchase-orders')
}

export function createPurchaseOrder(payload) {
  return http.post('/purchase-orders', payload)
}

export function updatePurchaseOrder(poNo, payload) {
  return http.put(`/purchase-orders/${encodeURIComponent(poNo)}`, payload)
}

export function deletePurchaseOrder(poNo) {
  return http.delete(`/purchase-orders/${encodeURIComponent(poNo)}`)
}
