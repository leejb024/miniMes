import http from './http'

export function fetchBoms(params) {
  return http.get('/boms', { params })
}

export function fetchBomMaterials(itemId, bomVersion) {
  return http.get('/boms/materials', {
    params: {
      itemId,
      bomVersion
    }
  })
}
