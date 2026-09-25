import http from './http'

export function fetchVendors() {
  return http.get('/vendors')
}

export function createVendor(payload) {
  return http.post('/vendors', payload)
}
