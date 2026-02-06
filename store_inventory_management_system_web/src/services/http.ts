import axios from 'axios'

const http = axios.create({
  baseURL: '/api/system',
  timeout: 8000,
  withCredentials: true
})

http.interceptors.response.use(
  r => {
    const payload = r.data
    if (payload && typeof payload.code === 'number') {
      if (payload.code === 40200) return payload
      if (payload.code === 40100) {
        if (location.pathname !== '/login') location.href = '/login'
      }
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload
  },
  e => Promise.reject(e)
)

export default http
