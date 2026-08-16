import http from './http'

export function fetchDepts() {
  return http.get('/depts')
}
