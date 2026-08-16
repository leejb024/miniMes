import http from './http'

export function fetchProcesses() {
  return http.get('/processes')
}
