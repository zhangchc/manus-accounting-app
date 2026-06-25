import axios from 'axios'

const request = axios.create({
  baseURL: '',
  timeout: 10000,
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  res => {
    const body = res.data
    if (body && body.code && body.code !== 200) {
      if (body.code === 401) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_user')
        window.location.href = '/'
      }
      return Promise.reject({ response: { data: body } })
    }
    return body
  },
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      window.location.href = '/'
    }
    return Promise.reject(err)
  }
)

export default request
