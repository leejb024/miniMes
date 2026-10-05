import http from './http'

export function fetchProcesses() {
  return http.get('/processes')
}

export function createProcess(payload) {
  return http.post('/processes', payload)
}

export function updateProcess(payload) {
  return http.put('/processes', payload)
}
