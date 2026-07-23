import request from '@/utils/request'

/** 查询T配置列表 */
export function getConfigList() {
  return request.get('/manage/stock/trade/config/list').then(r => r.data)
}

/** 根据股票代码查询T配置 */
export function getConfigByStockCode(stockCode) {
  return request.get('/manage/stock/trade/config/query', { params: { stockCode } }).then(r => r.data)
}

/** 保存T配置（新增 / 修改） */
export function saveConfig(data) {
  return request.post('/manage/stock/trade/config/save', data)
}

/** 查询档位列表 */
export function getOperationList(configId) {
  return request.get('/manage/stock/trade/operation/list', { params: { configId } }).then(r => r.data)
}

/** 新增交易记录 */
export function saveRecord(data) {
  return request.post('/manage/stock/trade/record/save', data)
}

/** 查询交易流水 */
export function getRecordList(configId) {
  return request.get('/manage/stock/trade/record/list', { params: { configId } }).then(r => r.data)
}
