import http from './http'

export function fetchItems() {
  return http.get('/items')
}

export function fetchNextRawItemId() {
  return http.get('/items/next-raw-id')
}

export function createRawItem(payload) {
  return http.post('/items', payload)
}

export function deleteItem(itemId) {
  return http.delete(`/items/${encodeURIComponent(itemId)}`)
}
