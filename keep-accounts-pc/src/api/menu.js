import request from '@/utils/request'

export function getMenuTree() {
  return request.get('/manage/menu/list').then(r => r.data)
}

export function createMenu(data) {
  return request.post('/manage/menu', data)
}

export function updateMenu(data) {
  return request.put('/manage/menu', data)
}

export function deleteMenu(id) {
  return request.delete(`/manage/menu/${id}`)
}
