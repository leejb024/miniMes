import http from './http'

export function fetchProdResults(workOrderId) {
  return http.get('/prod-results', {
    params: { workOrderId }
  })
}

export function fetchProdResultDetails(workOrderId, lotId) {
  return http.get('/prod-results/details', {
    params: { workOrderId, lotId }
  })
}
