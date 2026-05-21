<template>
  <div class="admin-login-page">
    <div class="login-card">
      <div class="login-header">
        <h2>后台管理</h2>
        <span class="role-badge">管理员</span>
      </div>

      <div v-if="loginSuccess" class="success-message">
        登录成功！欢迎回来，{{ username }}
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
          <span v-else>安全登录</span>
        </button>
      </template>

      <div class="footer-note">
        <strong>仅限管理员登录</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { login, storeCaptcha } from '../api'
import { setToken, setUsername, setRole, setAgentType } from '../utils/auth'

const router = useRouter()

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
    
    const response = await login(
      credentials.username,
      credentials.password,
      credentials.captchaInput
    )

    setToken(response.token, response.tokenType)
    setUsername(response.username)
    setRole(response.role)
    setAgentType(response.agentType)

    loginSuccess.value = true
    errorMsg.value = ''
    username.value = response.username
    
    setTimeout(() => {
      window.location.href = '/dashboard'
    }, 1000)
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
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  background: linear-gradient(135deg, var(--color-bg) 0%, #e8efe9 100%);
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 440px;
  padding: 2.5rem 2rem;
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.login-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
}

.role-badge {
  background: #f4f8ff;
  color: #1e4a8b;
  padding: 0.35rem 1rem;
  border-radius: 50px;
  font-size: 0.8rem;
  font-weight: 600;
  border: 1px solid #b9d3f0;
}

.input-group {
  margin-bottom: 1.25rem;
}

.input-group label {
  display: block;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.input-field {
  width: 100%;
  padding: 1rem 1.25rem;
  background: white;
  border: 2px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
}

.input-field:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(45, 90, 61, 0.15);
}

.captcha-row {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.captcha-row .input-field {
  flex: 1;
}

.captcha-box {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  color: white;
  font-weight: 700;
  font-size: 1.5rem;
  letter-spacing: 0.25em;
  text-align: center;
  padding: 0.75rem 1rem;
  min-width: 120px;
  border-radius: var(--radius);
  font-family: 'Courier New', monospace;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-message {
  color: var(--color-danger);
  font-size: 0.9rem;
  margin-bottom: 1rem;
  background: #fee2e2;
  padding: 0.75rem 1rem;
  border-radius: var(--radius);
}

.success-message {
  color: #065f46;
  background: #d1fae5;
  padding: 1rem 1.25rem;
  border-radius: var(--radius-lg);
  font-size: 0.95rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
  text-align: center;
}

.login-btn {
  width: 100%;
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 1rem 1.25rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 1rem;
}

.login-btn:hover:not(:disabled) {
  background: var(--color-primary-light);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.footer-note {
  margin-top: 2rem;
  font-size: 0.85rem;
  color: var(--color-text-muted);
  text-align: center;
  border-top: 1px dashed var(--color-border);
  padding-top: 1.25rem;
}

.footer-note strong {
  color: var(--color-primary);
  font-weight: 600;
  background: var(--color-bg);
  padding: 0.25rem 0.875rem;
  border-radius: 50px;
  font-size: 0.8rem;
}
</style>
