import request from '@/utils/request'

export function login(data) {
  return request.post('/manage/auth/login', data).then(r => r.data)
}

export function getUserInfo() {
  return request.get('/manage/auth/userinfo').then(r => r.data)
}
