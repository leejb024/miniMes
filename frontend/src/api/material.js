import http from './http'

export function fetchMaterialOrders() {
  return http.get('/inventory/materialOrder')
}

export function createMaterialOrder(payload) {
  return http.post('/inventory/materialOrder', payload)
}

export function requestMaterialMove(orderNo) {
  return http.post('/inventory/moveRequest', { orderNo })
}

export function registerPickOrder(payload) {
  return http.post('/inventory/pickorder', payload)
}

export function saveMoveInfo(payload) {
  return http.post('/inventory/moveinfo', payload)
}

export function saveInMoveInfo(payload) {
  return http.post('/inventory/in/moveinfo', payload)
}

export function confirmMaterialReceive(orderNo) {
  return http.post('/inventory/material/receive', { orderNo })
}

export function moveMaterial(orderNo) {
  return http.post('/inventory/material/move', { orderNo })
}

export function weighInputLot(payload) {
  return http.post('/inventory/input/material/lot/weighing', payload)
}

export function registerInputLot(payload) {
  return http.post('/inventory/input/material/lot', payload)
}

export function saveCombineResult(payload) {
  return http.post('/inventory/input/combine/result', payload)
}

export function changeInputLot(payload) {
  return http.post('/inventory/input/material/lot/change', payload)
}
