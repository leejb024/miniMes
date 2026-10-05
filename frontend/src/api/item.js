import http from './http'

export function fetchItems() {
  return http.get('/items')
}

export function fetchNextItemId(kind) {
  return http.get('/items/next-id', { params: { kind } })
}

export function createItem(payload) {
  return http.post('/items', payload)
}

export function deleteItem(itemId) {
  return http.delete(`/items/${encodeURIComponent(itemId)}`)
}
