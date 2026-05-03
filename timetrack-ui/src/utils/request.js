import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearAdmin, getToken, removeToken } from '@/utils/auth'

const service = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const headers = config.headers || {}
  if (headers.isToken !== false && getToken()) {
    headers.Authorization = `Bearer ${getToken()}`
  }
  config.headers = headers
  return config
})

service.interceptors.response.use(
  (response) => {
    const body = response.data
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return body
    }
    const code = body?.code ?? 0
    if (code === 0) {
      return body.data
    }
    const message = body?.message || body?.msg || '请求失败'
    if (code === 4010) {
      removeToken()
      clearAdmin()
      ElMessageBox.alert('登录状态已过期，请重新登录。', '会话过期', { type: 'warning' }).finally(() => {
        window.location.href = '/login'
      })
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(new Error(message))
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '后端接口连接异常'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
