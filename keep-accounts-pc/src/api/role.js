import request from '@/utils/request'

export function getRoleList() {
  return request.get('/manage/role/list').then(r => r.data)
}

export function createRole(data) {
  return request.post('/manage/role', data)
}

export function getRoleMenus(roleId) {
  return request.get(`/manage/role/${roleId}/menus`).then(r => r.data)
}

export function assignRoleMenus(roleId, menuIds) {
  return request.put(`/manage/role/${roleId}/menus`, menuIds)
}
