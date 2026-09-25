import http from './http'

export function fetchWarehouses() {
  return http.get('/warehouses')
}

export function createWarehouse(payload) {
  return http.post('/warehouses', payload)
}
