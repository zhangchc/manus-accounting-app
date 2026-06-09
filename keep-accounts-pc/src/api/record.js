import request from '@/utils/request'

export function getRecordList(params) {
  return request.get('/manage/record/page', { params }).then(r => r.data)
}

export function getRecordCategories() {
  return request.get('/manage/record/categories').then(r => r.data)
}

export function exportRecords(params) {
  return request.get('/manage/record/export', {
    params,
    responseType: 'blob'
  }).then(blob => {
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'record_export.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  })
}
