<template>
  <div class="admin-login-page">
    <div class="aurora aurora-a"></div>
    <div class="aurora aurora-b"></div>

    <div class="login-shell">
      <section class="brand-panel">
        <p class="brand-kicker">Traceability Console</p>
        <h1>食品溯源管理台</h1>
        <p class="brand-copy">稳定、安全、实时的供应链数据协同入口</p>
        <div class="brand-tags">
          <span>全流程追踪</span>
          <span>实时监管</span>
          <span>高可信审计</span>
        </div>
      </section>

      <section class="login-card">
        <div class="login-header">
          <h2>管理员登录</h2>
          <span class="role-badge">Admin</span>
        </div>

        <p class="login-subtitle">输入账号、密码与验证码后进入控制台</p>

        <button type="button" class="create-user-link" @click="showCreateUser = !showCreateUser">
          {{ showCreateUser ? '收起新建用户' : '新建用户' }}
        </button>

        <div v-if="showCreateUser" class="create-user-panel">
          <div class="input-group">
            <label>新用户名</label>
            <input
              type="text"
              class="input-field"
              v-model="newUser.username"
              autocomplete="off"
              placeholder="请输入新用户名"
            />
          </div>
          <div class="input-group">
            <label>新用户密码</label>
            <input
              type="password"
              class="input-field"
              v-model="newUser.password"
              autocomplete="off"
              placeholder="请输入新用户密码"
            />
          </div>
          <div class="input-group">
            <label>当前管理员账号</label>
            <input
              type="text"
              class="input-field"
              v-model="newUser.currentAdminUsername"
              autocomplete="off"
              placeholder="请输入当前管理员账号"
            />
          </div>
          <div class="input-group">
            <label>当前管理员密码</label>
            <input
              type="password"
              class="input-field"
              v-model="newUser.currentPassword"
              autocomplete="off"
              placeholder="请输入当前管理员密码"
            />
          </div>

          <div v-if="createUserMsg" class="success-message create-user-msg">{{ createUserMsg }}</div>
          <div v-if="createUserError" class="error-message create-user-msg">{{ createUserError }}</div>

          <button
            type="button"
            class="login-btn create-user-btn"
            :disabled="creatingUser"
            @click="handleCreateUser"
          >
            <span v-if="creatingUser">创建中...</span>
            <span v-else>确认新建用户</span>
          </button>
        </div>

        <div v-if="loginSuccess" class="success-message">
          登录成功，欢迎 {{ username }}
        </div>

        <template v-else>
          <div class="input-group">
            <label>管理员账号</label>
            <input
              type="text"
              class="input-field"
              v-model="credentials.username"
              autocomplete="off"
              ref="usernameInput"
              @keyup.enter="handleLogin"
              placeholder="请输入账号"
            />
          </div>

          <div class="input-group">
            <label>密码</label>
            <input
              type="password"
              class="input-field"
              v-model="credentials.password"
              autocomplete="off"
              @keyup.enter="handleLogin"
              placeholder="请输入密码"
            />
          </div>

          <div class="input-group">
            <label>验证码</label>
            <div class="captcha-row">
              <input
                type="text"
                class="input-field"
                v-model="credentials.captchaInput"
                maxlength="6"
                autocomplete="off"
                @keyup.enter="handleLogin"
                placeholder="请输入验证码"
              />
              <div class="captcha-box" @click="generateCaptcha" title="点击刷新验证码">
                {{ currentCaptcha }}
              </div>
            </div>
          </div>

          <div v-if="errorMsg" class="error-message">
            {{ errorMsg }}
          </div>

          <button type="button" class="login-btn" :disabled="loading" @click="handleLogin">
            <span v-if="loading">处理中...</span>
            <span v-else>进入控制台</span>
          </button>
        </template>

        <div class="footer-note">
          <strong>受控访问环境</strong>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { login, storeCaptcha, registerAdmin } from '../api'
import { setToken, setUsername, setRole, setAgentType } from '../utils/auth'

const credentials = reactive({
  username: '',
  password: '',
  captchaInput: ''
})

const currentCaptcha = ref('')
const errorMsg = ref('')
const loading = ref(false)
const loginSuccess = ref(false)
const username = ref('')
const showCreateUser = ref(false)
const creatingUser = ref(false)
const createUserMsg = ref('')
const createUserError = ref('')
const newUser = reactive({
  username: '',
  password: '',
  currentPassword: '',
  currentAdminUsername: ''
})

function generateCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'
  let captcha = ''
  const length = 5
  for (let i = 0; i < length; i++) {
    captcha += chars[Math.floor(Math.random() * chars.length)]
  }
  currentCaptcha.value = captcha
  credentials.captchaInput = ''

  if (credentials.username && credentials.username.trim()) {
    storeCaptcha(credentials.username, captcha).catch(err => {
      console.error('验证码存储失败:', err)
    })
  }
}

async function handleCreateUser() {
  if (!newUser.username.trim()) {
    createUserError.value = '请输入新用户名'
    return
  }
  if (!newUser.password.trim()) {
    createUserError.value = '请输入新用户密码'
    return
  }
  if (!newUser.currentAdminUsername.trim()) {
    createUserError.value = '请输入当前管理员账号'
    return
  }
  if (!newUser.currentPassword.trim()) {
    createUserError.value = '请输入当前管理员密码'
    return
  }

  creatingUser.value = true
  createUserError.value = ''
  createUserMsg.value = ''

  try {
    await registerAdmin({
      username: newUser.username.trim(),
      password: newUser.password,
      currentPassword: newUser.currentPassword,
      currentAdminUsername: newUser.currentAdminUsername.trim()
    })
    createUserMsg.value = `用户 ${newUser.username.trim()} 创建成功`
    newUser.username = ''
    newUser.password = ''
    newUser.currentPassword = ''
    newUser.currentAdminUsername = ''
  } catch (error) {
    if (error.response) {
      createUserError.value = error.response.data?.message || error.response.data || '新建用户失败'
    } else if (error.request) {
      createUserError.value = '网络错误，请稍后重试'
    } else {
      createUserError.value = error.message || '新建用户失败'
    }
  } finally {
    creatingUser.value = false
  }
}

generateCaptcha()

async function handleLogin() {
  if (!credentials.username || !credentials.username.trim()) {
    errorMsg.value = '请输入管理员账号'
    return
  }
  if (!credentials.password || !credentials.password.trim()) {
    errorMsg.value = '请输入密码'
    return
  }
  if (!credentials.captchaInput || !credentials.captchaInput.trim()) {
    errorMsg.value = '请输入验证码'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    await storeCaptcha(credentials.username, currentCaptcha.value)

    const res = await login(
      credentials.username,
      credentials.password,
      credentials.captchaInput
    )
    const payload = res?.data || {}
    const finalUsername = payload.username || credentials.username.trim()

    setToken(payload.token, payload.tokenType)
    setUsername(finalUsername)
    setRole(payload.role || '')
    setAgentType(payload.agentType || '')

    loginSuccess.value = true
    errorMsg.value = ''
    username.value = finalUsername

    setTimeout(() => {
      window.location.href = '/dashboard'
    }, 700)
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response) {
      errorMsg.value = error.response.data?.message || error.response.data || '登录失败'
    } else if (error.request) {
      errorMsg.value = '网络错误，请稍后重试'
    } else {
      errorMsg.value = error.message || '登录失败，请稍后重试'
    }
    generateCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const usernameInput = document.querySelector('.input-field')
  if (usernameInput) {
    usernameInput.focus()
  }
})
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.25rem;
  background: #f3f4f8;
}

.aurora {
  position: absolute;
  border-radius: 999px;
  filter: blur(56px);
  pointer-events: none;
}

.aurora-a {
  width: 380px;
  height: 380px;
  background: rgba(255, 122, 89, 0.32);
  top: -130px;
  left: -90px;
}

.aurora-b {
  width: 440px;
  height: 440px;
  background: rgba(34, 197, 168, 0.25);
  right: -130px;
  bottom: -170px;
}

.login-shell {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1.08fr 1fr;
  border-radius: 28px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  box-shadow: 0 20px 60px rgba(27, 39, 70, 0.16);
  animation: shellIn 0.65s ease;
}

.brand-panel {
  padding: 3rem 2.75rem;
  background: linear-gradient(145deg, #0f172a 0%, #1d4ed8 50%, #06b6d4 100%);
  color: #eaf3ff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-kicker {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.78rem;
  font-weight: 700;
  opacity: 0.9;
  margin-bottom: 1rem;
}

.brand-panel h1 {
  font-size: clamp(1.9rem, 2.4vw, 2.45rem);
  line-height: 1.2;
  margin-bottom: 1rem;
}

.brand-copy {
  font-size: 1rem;
  line-height: 1.75;
  opacity: 0.92;
  margin-bottom: 1.5rem;
}

.brand-tags {
  display: flex;
  gap: 0.55rem;
  flex-wrap: wrap;
}

.brand-tags span {
  padding: 0.45rem 0.8rem;
  border: 1px solid rgba(234, 243, 255, 0.32);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  font-size: 0.8rem;
}

.login-card {
  padding: 2.3rem 2rem;
  background: #ffffff;
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.65rem;
}

.login-header h2 {
  font-size: 1.45rem;
  color: #0f172a;
  margin: 0;
}

.role-badge {
  background: #ecfeff;
  color: #0f766e;
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
  font-size: 0.76rem;
  font-weight: 700;
}

.login-subtitle {
  color: #475569;
  font-size: 0.92rem;
  margin-bottom: 1.2rem;
}

.create-user-link {
  border: 1px solid #bfdbfe;
  color: #1d4ed8;
  background: #eff6ff;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: 999px;
  padding: 0.4rem 0.72rem;
  margin-bottom: 0.95rem;
}

.create-user-panel {
  border: 1px solid #dbeafe;
  background: #f8fbff;
  border-radius: 14px;
  padding: 0.9rem;
  margin-bottom: 1.1rem;
}

.create-user-msg {
  margin-top: 0.1rem;
}

.create-user-btn {
  margin-top: 0.25rem;
}

.input-group {
  margin-bottom: 1rem;
}

.input-group label {
  display: block;
  margin-bottom: 0.45rem;
  color: #334155;
  font-size: 0.9rem;
  font-weight: 600;
}

.input-field {
  width: 100%;
  padding: 0.88rem 1rem;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  background: #f8fafc;
  font-size: 0.96rem;
  transition: border-color 0.2s, box-shadow 0.2s, background-color 0.2s;
  outline: none;
}

.input-field:focus {
  border-color: #2563eb;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.14);
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 130px;
  gap: 0.65rem;
}

.captcha-box {
  border-radius: 12px;
  background: linear-gradient(120deg, #0f172a 0%, #334155 100%);
  color: #f8fafc;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 1.32rem;
  letter-spacing: 0.2em;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
}

.error-message {
  margin: 0.5rem 0 0.85rem;
  border-radius: 10px;
  padding: 0.65rem 0.8rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
  font-size: 0.88rem;
}

.success-message {
  border-radius: 12px;
  padding: 0.78rem 0.9rem;
  background: #ecfeff;
  border: 1px solid #a5f3fc;
  color: #155e75;
  margin-bottom: 1rem;
  font-size: 0.92rem;
}

.login-btn {
  width: 100%;
  border: none;
  border-radius: 12px;
  padding: 0.9rem 1rem;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 700;
  background: linear-gradient(120deg, #2563eb 0%, #06b6d4 100%);
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.28);
  transition: transform 0.16s ease, box-shadow 0.16s ease, filter 0.16s ease;
}

.login-btn:hover:enabled {
  transform: translateY(-1px);
  filter: brightness(1.05);
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.34);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.footer-note {
  margin-top: 1.15rem;
  text-align: center;
}

.footer-note strong {
  font-size: 0.76rem;
  color: #64748b;
  font-weight: 600;
}

@media (max-width: 920px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 2rem 1.4rem;
  }

  .login-card {
    padding: 1.5rem 1.15rem;
  }
}

@media (max-width: 520px) {
  .admin-login-page {
    padding: 0.6rem;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }

  .captcha-box {
    min-height: 48px;
  }
}

@keyframes shellIn {
  from {
    transform: translateY(10px) scale(0.985);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}
</style>
