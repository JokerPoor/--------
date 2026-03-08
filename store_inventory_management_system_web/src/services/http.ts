import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api/system',
  timeout: 60000,
  withCredentials: true
})

http.interceptors.response.use(
  r => {
    const payload = r.data
    if (payload && typeof payload.code === 'number') {
      if (payload.code === 40200) return payload
      if (payload.code === 40100) {
        // 未登录错误，排除登录和注册页面
        if (location.pathname !== '/login' && location.pathname !== '/register') {
          location.href = '/login'
        }
      }
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload
  },
  e => {
    ElMessage.error(e.message || '网络错误')
    return Promise.reject(e)
  }
)

export default http
