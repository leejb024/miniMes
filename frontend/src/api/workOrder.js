import http from './http'

export function fetchWorkOrders(deptId, planDateFrom, planDateTo) {
  return http.get('/work-orders', {
    params: {
      deptId,
      planDateFrom,
      planDateTo
    }
  })
}
