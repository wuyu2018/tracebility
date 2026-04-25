// Token 管理工具函数

const TOKEN_KEY = 'jwt_token'
const TOKEN_TYPE_KEY = 'token_type'
const USERNAME_KEY = 'admin_username'
const LOGIN_TIME_KEY = 'login_time'

/**
 * 保存 Token 到本地存储
 */
export function setToken(token, tokenType = 'Bearer') {
  console.log('[Auth] setToken called:', { token, tokenType })
  if (!token) {
    console.error('[Auth] ERROR: token is null or undefined!')
    return
  }
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(TOKEN_TYPE_KEY, tokenType)
  localStorage.setItem(LOGIN_TIME_KEY, Date.now().toString())
  console.log('[Auth] Token saved, current localStorage:', {
    jwt_token: localStorage.getItem(TOKEN_KEY),
    token_type: localStorage.getItem(TOKEN_TYPE_KEY)
  })
}

/**
 * 获取 Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 获取 Token 类型
 */
export function getTokenType() {
  return localStorage.getItem(TOKEN_TYPE_KEY) || 'Bearer'
}

/**
 * 获取登录时间
 */
export function getLoginTime() {
  const time = localStorage.getItem(LOGIN_TIME_KEY)
  return time ? parseInt(time, 10) : 0
}

/**
 * 检查 Token 是否过期（默认 24 小时）
 */
export function isTokenExpired(expirationMs = 86400000) {
  const loginTime = getLoginTime()
  if (!loginTime) return true
  
  const now = Date.now()
  return (now - loginTime) > expirationMs
}

/**
 * 移除 Token（登出）
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TOKEN_TYPE_KEY)
  localStorage.removeItem(USERNAME_KEY)
  localStorage.removeItem(LOGIN_TIME_KEY)
}

/**
 * 保存用户名
 */
export function setUsername(username) {
  localStorage.setItem(USERNAME_KEY, username)
}

/**
 * 获取用户名
 */
export function getUsername() {
  return localStorage.getItem(USERNAME_KEY)
}

/**
 * 检查用户是否已登录
 */
export function isAuthenticated() {
  const token = getToken()
  return !!token && !isTokenExpired()
}

/**
 * 获取完整的 Authorization Header 值
 */
export function getAuthHeader() {
  const token = getToken()
  const tokenType = getTokenType()
  if (!token) return ''
  return `${tokenType} ${token}`
}
