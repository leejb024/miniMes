import http from './http'

export function fetchBoms() {
  return http.get('/boms')
}

export function fetchBomMaterials(itemId, bomVersion) {
  return http.get('/boms/materials', {
    params: {
      itemId,
      bomVersion
    }
  })
}
