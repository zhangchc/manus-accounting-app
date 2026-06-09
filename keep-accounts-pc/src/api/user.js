import request from '@/utils/request'

export function getUserList() {
  return request.get('/manage/user/list').then(r => r.data)
}

export function createUser(data) {
  return request.post('/manage/user', data)
}

export function updateUser(data) {
  return request.put('/manage/user', data)
}

export function deleteUser(id) {
  return request.delete(`/manage/user/${id}`)
}

export function getUserRoles(userId) {
  return request.get(`/manage/user/${userId}/roles`).then(r => r.data)
}

export function assignUserRoles(userId, roleIds) {
  return request.put(`/manage/user/${userId}/roles`, roleIds)
}
