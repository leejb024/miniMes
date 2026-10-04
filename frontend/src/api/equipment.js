import http from './http'

export function fetchEquipments() {
  return http.get('/equipments')
}

export function createEquipment(payload) {
  return http.post('/equipments', payload)
}

export function updateEquipment(payload) {
  return http.put('/equipments', payload)
}
