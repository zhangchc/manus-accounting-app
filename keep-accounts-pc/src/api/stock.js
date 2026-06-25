import request from '@/utils/request'

export function getPosition() {
  return request.get('/manage/stock/position').then(r => r.data)
}

export function savePosition(data) {
  return request.post('/manage/stock/position', data).then(r => r.data)
}

export function getStockPrice(code) {
  return request.get('/manage/stock/price', { params: { code } }).then(r => r.data)
}

// ==================== 做T管理 ====================

export function getTradeStrategy() {
  return request.get('/manage/stock/trade/strategy').then(r => r.data)
}

export function saveTradeStrategy(data) {
  return request.post('/manage/stock/trade/strategy', data).then(r => r.data)
}

export function tradePrecheck(type, currentPrice) {
  return request.get('/manage/stock/trade/precheck', { params: { type, currentPrice } }).then(r => r.data)
}

export function tradeSell(data) {
  return request.post('/manage/stock/trade/sell', data).then(r => r.data)
}

export function tradeBuy(data) {
  return request.post('/manage/stock/trade/buy', data).then(r => r.data)
}

export function getTradeRecords(params) {
  return request.get('/manage/stock/trade/records', { params }).then(r => r.data)
}

export function getTradeSummary() {
  return request.get('/manage/stock/trade/summary').then(r => r.data)
}

export function resetTrade() {
  return request.post('/manage/stock/trade/reset').then(r => r.data)
}

export function getCostHistory() {
  return request.get('/manage/stock/cost-history').then(r => r.data)
}
