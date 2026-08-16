import http from './http'

export function fetchWorkcenters() {
  return http.get('/workcenters')
}
