import http from './http'

export function fetchWorkOrders(planDateFrom, planDateTo) {
  return http.get('/work-orders', {
    params: {
      planDateFrom,
      planDateTo
    }
  })
}

export function createWorkOrder(payload) {
  return http.post('/work-orders', payload)
}

export function updateWorkOrder(workOrderId, payload) {
  return http.put(`/work-orders/${encodeURIComponent(workOrderId)}`, payload)
}

export function deleteWorkOrder(workOrderId) {
  return http.delete(`/work-orders/${encodeURIComponent(workOrderId)}`)
}

export function cancelWorkOrder(workOrderId) {
  return http.post('/production/cancelworkorder', null, {
    params: { workOrderId }
  })
}

export function startWorkOrder(workOrderId) {
  return http.post('/production/start/workorder', null, {
    params: { workOrderId }
  })
}
