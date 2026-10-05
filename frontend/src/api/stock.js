import http from './http'

export function fetchStocks() {
  return http.get('/stocks')
}

export function fetchStockCarries(stockSeq) {
  return http.get('/inventory/stock/carry', {
    params: stockSeq ? { stockSeq } : {}
  })
}

export function carryStock(payload) {
  return http.post('/inventory/stock/carry', payload)
}
