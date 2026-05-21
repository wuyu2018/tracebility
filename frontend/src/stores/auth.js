import { reactive } from 'vue'
import { login as apiLogin, storeCaptcha } from '@/api/admin'

const auth = reactive({
  token: localStorage.getItem('admin_token') || '',
  user: JSON.parse(localStorage.getItem('admin_user') || 'null'),
  isLoggedIn: !!localStorage.getItem('admin_token')
})

export function useAuth() {
  async function doLogin(username, password, captcha) {
    await storeCaptcha(username, captcha)
    const res = await apiLogin(username, password, captcha)
    const { token, role, username: uname, agentType, companyId, companyName } = res.data || res
    auth.token = token
    auth.user = { username: uname, role, agentType, companyId, companyName }
    auth.isLoggedIn = true
    localStorage.setItem('admin_token', token)
    localStorage.setItem('admin_user', JSON.stringify(auth.user))
    return auth.user
  }

  function logout() {
    auth.token = ''
    auth.user = null
    auth.isLoggedIn = false
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  function isSuperAdmin() {
    return auth.user?.role === 'SUPER_ADMIN'
  }

  return { auth, doLogin, logout, isSuperAdmin }
}
