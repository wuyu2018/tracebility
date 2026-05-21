export function setToken(token, tokenType = 'Bearer') {
  localStorage.setItem('token', token)
  localStorage.setItem('tokenType', tokenType)
}

export function getToken() {
  return localStorage.getItem('token')
}

export function removeToken() {
  localStorage.removeItem('token')
  localStorage.removeItem('tokenType')
}

export function isAuthenticated() {
  return !!getToken()
}

export function setUsername(username) {
  localStorage.setItem('username', username)
}

export function getUsername() {
  return localStorage.getItem('username')
}

export function setRole(role) {
  localStorage.setItem('role', role)
}

export function getRole() {
  return localStorage.getItem('role') || ''
}

export function setAgentType(agentType) {
  localStorage.setItem('agentType', agentType)
}

export function getAgentType() {
  return localStorage.getItem('agentType')
}

export function getRoleLabel() {
  const agentType = getAgentType()
  const role = getRole()
  
  if (agentType) {
    const typeMap = {
      PRODUCTION: '生产方',
      CIRCULATION: '流通方',
      SALES: '销售方',
    }
    return typeMap[agentType] || '管理员'
  }
  
  return role === 'SUPER_ADMIN' ? '超级管理员' : '管理员'
}

export function clearAuth() {
  removeToken()
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  localStorage.removeItem('agentType')
}
