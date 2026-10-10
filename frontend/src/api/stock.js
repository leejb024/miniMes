import http from './http'

export function fetchStocks() {
  return http.get('/stocks')
}
