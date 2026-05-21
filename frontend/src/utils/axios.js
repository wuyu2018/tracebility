import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    const tokenType = localStorage.getItem('tokenType') || 'Bearer'
    
    if (token) {
      config.headers.Authorization = `${tokenType} ${token}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          localStorage.removeItem('token')
          localStorage.removeItem('tokenType')
          localStorage.removeItem('username')
          localStorage.removeItem('role')
          localStorage.removeItem('agentType')
          
          if (window.location.pathname !== '/Admin') {
            window.location.href = '/Admin'
          }
          break
          
        case 403:
          console.error('无权限访问')
          break
          
        case 404:
          console.error('资源不存在')
          break
          
        case 500:
          console.error('服务器错误')
          break
          
        default:
          break
      }
    }
    
    return Promise.reject(error)
  }
)

export default api
