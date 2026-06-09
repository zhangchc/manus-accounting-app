import request from '@/utils/request'

export function getAppCategoryList(params) {
  return request.get('/manage/app-category/page', { params }).then(r => r.data)
}
