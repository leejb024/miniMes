import http from './http'

export function fetchVendors() {
  return http.get('/vendors')
}
