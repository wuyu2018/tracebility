<template>
  <div class="admin-auth-page">
    <div class="mesh mesh-a"></div>
    <div class="mesh mesh-b"></div>

    <div class="auth-frame">
      <aside class="hero-panel">
        <p class="hero-kicker">Food Traceability Platform</p>
        <h1>食品溯源后台中心</h1>
        <p class="hero-copy">统一登录、分权注册、全链路监管，面向生产与流通协同场景。</p>
        <div class="hero-metrics">
          <article>
            <strong>99.95%</strong>
            <span>系统可用性</span>
          </article>
          <article>
            <strong>24h</strong>
            <span>实时监控</span>
          </article>
          <article>
            <strong>Role-based</strong>
            <span>权限模型</span>
          </article>
        </div>
      </aside>

      <section class="auth-panel">
        <div class="tab-row">
          <button
            type="button"
            class="tab-btn"
            :class="{ active: activeTab === 'login' }"
            @click="activeTab = 'login'"
          >
            登录
          </button>
          <button
            type="button"
            class="tab-btn"
            :class="{ active: activeTab === 'register' }"
            @click="activeTab = 'register'"
          >
            注册
          </button>
        </div>

        <div v-if="activeTab === 'login'" class="panel-body">
          <h2>管理员登录</h2>
          <p class="panel-subtitle">输入账号、密码与验证码进入后台管理系统</p>

          <div v-if="loginSuccess" class="notice success">登录成功，欢迎 {{ username }}</div>

          <template v-else>
            <div class="input-group">
              <label>管理员账号</label>
              <input
                ref="usernameInput"
                v-model="credentials.username"
                class="input-field"
                type="text"
                autocomplete="off"
                placeholder="请输入账号"
                @keyup.enter="handleLogin"
              />
            </div>

            <div class="input-group">
              <label>密码</label>
              <input
                v-model="credentials.password"
                class="input-field"
                type="password"
                autocomplete="off"
                placeholder="请输入密码"
                @keyup.enter="handleLogin"
              />
            </div>

            <div class="input-group">
              <label>验证码</label>
              <div class="captcha-row">
                <input
                  v-model="credentials.captchaInput"
                  class="input-field"
                  type="text"
                  maxlength="6"
                  autocomplete="off"
                  placeholder="请输入验证码"
                  @keyup.enter="handleLogin"
                />
                <div class="captcha-box" title="点击刷新验证码" @click="generateCaptcha">{{ currentCaptcha }}</div>
              </div>
            </div>

            <div v-if="errorMsg" class="notice error">{{ errorMsg }}</div>

            <button type="button" class="primary-btn" :disabled="loading" @click="handleLogin">
              <span v-if="loading">登录中...</span>
              <span v-else>登录后台</span>
            </button>
          </template>
        </div>

        <div v-else class="panel-body">
          <h2>新建管理员</h2>
          <p class="panel-subtitle">当前账号需为超级管理员，并通过当前密码验证</p>

          <div class="input-grid">
            <div class="input-group">
              <label>新用户名</label>
              <input
                v-model="registerForm.username"
                class="input-field"
                type="text"
                autocomplete="off"
                placeholder="3-20 位，字母数字下划线"
              />
            </div>

            <div class="input-group">
              <label>新用户密码</label>
              <input
                v-model="registerForm.password"
                class="input-field"
                type="password"
                autocomplete="off"
                placeholder="至少 6 位"
              />
            </div>

            <div class="input-group">
              <label>角色</label>
              <select v-model="registerForm.role" class="input-field">
                <option value="ADMIN">普通管理员</option>
                <option value="SUPER_ADMIN">超级管理员</option>
              </select>
            </div>

            <div class="input-group">
              <label>业务类型</label>
              <select v-model="registerForm.agentType" class="input-field">
                <option value="">不指定</option>
                <option value="PRODUCTION">生产方</option>
                <option value="CIRCULATION">流通方</option>
                <option value="SALES">销售方</option>
              </select>
            </div>

            <div class="input-group">
              <label>当前管理员账号</label>
              <input
                v-model="registerForm.currentAdminUsername"
                class="input-field"
                type="text"
                autocomplete="off"
                placeholder="用于后端身份核验"
              />
            </div>

            <div class="input-group">
              <label>当前管理员密码</label>
              <input
                v-model="registerForm.currentPassword"
                class="input-field"
                type="password"
                autocomplete="off"
                placeholder="请输入当前管理员密码"
              />
            </div>
          </div>

          <div v-if="registerError" class="notice error">{{ registerError }}</div>
          <div v-if="registerSuccess" class="notice success">{{ registerSuccess }}</div>

          <button type="button" class="primary-btn" :disabled="registering" @click="handleRegister">
            <span v-if="registering">创建中...</span>
            <span v-else>创建管理员</span>
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { login, registerAdmin, storeCaptcha } from '../api'
import { setAgentType, setRole, setToken, setUsername } from '../utils/auth'

const activeTab = ref('login')

const credentials = reactive({
  username: '',
  password: '',
  captchaInput: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  role: 'ADMIN',
  agentType: '',
  currentPassword: '',
  currentAdminUsername: ''
})

const currentCaptcha = ref('')
const errorMsg = ref('')
const loading = ref(false)
const loginSuccess = ref(false)
const username = ref('')

const registering = ref(false)
const registerError = ref('')
const registerSuccess = ref('')

function generateCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'
  let captcha = ''
  for (let i = 0; i < 5; i += 1) {
    captcha += chars[Math.floor(Math.random() * chars.length)]
  }
  currentCaptcha.value = captcha
  credentials.captchaInput = ''

  if (credentials.username && credentials.username.trim()) {
    storeCaptcha(credentials.username.trim(), captcha).catch((err) => {
      console.error('验证码存储失败:', err)
    })
  }
}

generateCaptcha()

async function handleLogin() {
  if (!credentials.username.trim()) {
    errorMsg.value = '请输入管理员账号'
    return
  }
  if (!credentials.password.trim()) {
    errorMsg.value = '请输入密码'
    return
  }
  if (!credentials.captchaInput.trim()) {
    errorMsg.value = '请输入验证码'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    await storeCaptcha(credentials.username.trim(), currentCaptcha.value)
    const res = await login(credentials.username.trim(), credentials.password, credentials.captchaInput.trim())
    const payload = res?.data || {}
    const finalUsername = payload.username || credentials.username.trim()

    setToken(payload.token, payload.tokenType)
    setUsername(finalUsername)
    setRole(payload.role || '')
    setAgentType(payload.agentType || '')

    loginSuccess.value = true
    username.value = finalUsername

    setTimeout(() => {
      window.location.href = '/dashboard'
    }, 650)
  } catch (error) {
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

async function handleRegister() {
  registerError.value = ''
  registerSuccess.value = ''

  if (!registerForm.username.trim()) {
    registerError.value = '请输入新用户名'
    return
  }
  if (!registerForm.password.trim()) {
    registerError.value = '请输入新用户密码'
    return
  }
  if (!registerForm.currentAdminUsername.trim()) {
    registerError.value = '请输入当前管理员账号'
    return
  }
  if (!registerForm.currentPassword.trim()) {
    registerError.value = '请输入当前管理员密码'
    return
  }

  registering.value = true
  try {
    await registerAdmin({
      username: registerForm.username.trim(),
      password: registerForm.password,
      role: registerForm.role,
      agentType: registerForm.agentType || null,
      currentPassword: registerForm.currentPassword,
      currentAdminUsername: registerForm.currentAdminUsername.trim()
    })

    registerSuccess.value = `用户 ${registerForm.username.trim()} 创建成功`
    registerForm.username = ''
    registerForm.password = ''
    registerForm.role = 'ADMIN'
    registerForm.agentType = ''
    registerForm.currentPassword = ''
    registerForm.currentAdminUsername = ''
  } catch (error) {
    if (error.response) {
      registerError.value = error.response.data?.message || error.response.data || '注册失败'
    } else if (error.request) {
      registerError.value = '网络错误，请稍后重试'
    } else {
      registerError.value = error.message || '注册失败，请稍后重试'
    }
  } finally {
    registering.value = false
  }
}

onMounted(() => {
  const usernameInput = document.querySelector('.input-field')
  if (usernameInput) usernameInput.focus()
})
</script>

<style scoped>
.admin-auth-page {
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.25rem;
  background: linear-gradient(140deg, #f7fafc 0%, #eef3ff 50%, #edfdfb 100%);
}

.mesh {
  position: absolute;
  border-radius: 999px;
  filter: blur(56px);
  pointer-events: none;
}

.mesh-a {
  width: 380px;
  height: 380px;
  left: -90px;
  top: -120px;
  background: rgba(56, 189, 248, 0.25);
}

.mesh-b {
  width: 420px;
  height: 420px;
  right: -140px;
  bottom: -170px;
  background: rgba(16, 185, 129, 0.22);
}

.auth-frame {
  width: min(1080px, 100%);
  border-radius: 28px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.15);
  display: grid;
  grid-template-columns: 1.02fr 1fr;
}

.hero-panel {
  background: linear-gradient(145deg, #0f172a 0%, #1e3a8a 52%, #0ea5e9 100%);
  color: #e9f0ff;
  padding: 3rem 2.5rem;
}

.hero-kicker {
  font-size: 0.78rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  margin-bottom: 1rem;
  opacity: 0.9;
}

.hero-panel h1 {
  font-size: clamp(1.9rem, 2.5vw, 2.5rem);
  line-height: 1.2;
  margin: 0 0 1rem;
}

.hero-copy {
  font-size: 1rem;
  line-height: 1.75;
  opacity: 0.95;
}

.hero-metrics {
  margin-top: 1.8rem;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.6rem;
}

.hero-metrics article {
  padding: 0.7rem;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.3);
  background: rgba(255, 255, 255, 0.08);
}

.hero-metrics strong {
  display: block;
  font-size: 0.95rem;
}

.hero-metrics span {
  font-size: 0.75rem;
  opacity: 0.9;
}

.auth-panel {
  padding: 1.35rem;
  background: #ffffff;
}

.tab-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.65rem;
  margin-bottom: 1rem;
}

.tab-btn {
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 0.65rem;
  font-size: 0.93rem;
  font-weight: 700;
  color: #334155;
  background: #f8fafc;
}

.tab-btn.active {
  color: #ffffff;
  border-color: transparent;
  background: linear-gradient(120deg, #2563eb 0%, #0ea5e9 100%);
}

.panel-body {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 1.05rem;
  background: #fbfdff;
}

.panel-body h2 {
  font-size: 1.2rem;
  margin: 0;
  color: #0f172a;
}

.panel-subtitle {
  margin: 0.35rem 0 0.95rem;
  color: #475569;
  font-size: 0.9rem;
}

.input-group {
  margin-bottom: 0.86rem;
}

.input-group label {
  display: block;
  margin-bottom: 0.42rem;
  color: #334155;
  font-size: 0.88rem;
  font-weight: 600;
}

.input-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.72rem;
}

.input-field {
  width: 100%;
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  border-radius: 11px;
  font-size: 0.92rem;
  padding: 0.76rem 0.86rem;
  outline: none;
  transition: border-color 0.16s, box-shadow 0.16s, background-color 0.16s;
}

.input-field:focus {
  border-color: #3b82f6;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.14);
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 0.62rem;
}

.captcha-box {
  border-radius: 11px;
  background: linear-gradient(120deg, #0f172a 0%, #334155 100%);
  color: #f8fafc;
  font-size: 1.22rem;
  letter-spacing: 0.18em;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  user-select: none;
  cursor: pointer;
}

.notice {
  margin-bottom: 0.8rem;
  border-radius: 10px;
  padding: 0.62rem 0.75rem;
  font-size: 0.86rem;
}

.notice.error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
}

.notice.success {
  background: #ecfeff;
  border: 1px solid #a5f3fc;
  color: #155e75;
}

.primary-btn {
  width: 100%;
  border: none;
  border-radius: 11px;
  padding: 0.8rem 0.9rem;
  color: #ffffff;
  font-size: 0.92rem;
  font-weight: 700;
  background: linear-gradient(120deg, #2563eb 0%, #0ea5e9 100%);
  box-shadow: 0 9px 20px rgba(37, 99, 235, 0.28);
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 980px) {
  .auth-frame {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    padding: 1.8rem 1.2rem;
  }

  .hero-metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .admin-auth-page {
    padding: 0.6rem;
  }

  .auth-panel {
    padding: 0.8rem;
  }

  .input-grid {
    grid-template-columns: 1fr;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }

  .captcha-box {
    min-height: 44px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
