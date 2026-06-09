import request from '@/utils/request'

export function getAppUserList(params) {
  return request.get('/manage/app-user/page', { params }).then(r => r.data)
}

export function getAppUserDetail(id) {
  return request.get(`/manage/app-user/${id}/detail`).then(r => r.data)
}
