import http from './http'

export function fetchWarehouses() {
  return http.get('/warehouses')
}
