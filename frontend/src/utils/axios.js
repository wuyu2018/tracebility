import axios from 'axios'
import { getToken, getTokenType, removeToken, setUsername } from '../utils/auth'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 秒超时
})

// 请求拦截器：自动添加 Token
api.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      const tokenType = getTokenType()
      config.headers.Authorization = `${tokenType} ${token}`
    }
    return config
  },
  error => {
    console.error('[API 请求错误]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：处理 Token 过期等错误
api.interceptors.response.use(
  response => response,
  error => {
    // 401 Unauthorized - Token 过期或无效
    if (error.response && error.response.status === 401) {
      console.warn('[Token 验证失败] Token 已过期或无效')
      
      // 清除 Token
      removeToken()
      
      // 如果当前不在登录页，提示后跳转
      const currentPath = window.location.pathname
      if (!currentPath.includes('ToolsStandalone') && !currentPath.includes('/Admin')) {
        ElMessage.error('登录已过期，请重新登录')
        
        // 延迟跳转到登录页
        setTimeout(() => {
          // 记录当前页面，登录后可以跳回来
          const redirectPath = encodeURIComponent(currentPath)
          window.location.href = `/ToolsStandalone?redirect=${redirectPath}`
        }, 1500)
      }
      
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }
    
    // 403 Forbidden - 无权访问
    if (error.response && error.response.status === 403) {
      console.warn('[访问被拒绝]', error.response.data)
      const message = error.response.data?.message || '无权访问该资源'
      ElMessage.error(message)
      return Promise.reject(error)
    }
    
    // 其他错误处理
    if (error.response && error.response.data) {
      const message = error.response.data.message || 
                     error.response.data.error || 
                     '请求失败，请稍后重试'
      console.error('[API 响应错误]', message)
      
      // 只对非静默请求显示错误提示
      if (!error.config?.silent) {
        ElMessage.error(message)
      }
      
      return Promise.reject(error)
    }
    
    // 网络错误
    if (error.request) {
      console.error('[网络错误]', error.message)
      ElMessage.error('网络连接失败，请检查网络后重试')
      return Promise.reject(error)
    }
    
    // 其他错误
    console.error('[未知错误]', error.message)
    return Promise.reject(error)
  }
)

// 导出 API 方法
export default api
