import http from './http'

export function fetchWmsInboundPlans() {
  return http.get('/wms/inbound-plans')
}
