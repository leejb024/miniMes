import http from './http'

export function fetchItems() {
  return http.get('/items')
}
