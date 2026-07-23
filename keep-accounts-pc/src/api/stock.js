import request from '@/utils/request'

/** 持仓概览汇总查询 */
export function getPositionSummary() {
  return request.get('/manage/stock/position/summary').then(r => r.data)
}

/** 新增持仓 */
export function addPosition(data) {
  return request.post('/manage/stock/position/add', data)
}

/** 分页查询持仓列表 */
export function getPositionList(params) {
  return request.get('/manage/stock/position/list', { params }).then(r => r.data)
}

/** 修改 / 删除持仓 */
export function savePosition(data) {
  return request.post('/manage/stock/position/update', data)
}
