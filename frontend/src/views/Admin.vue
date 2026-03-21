<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h2>{{ '🔐 后台管理' }}</h2>
        <span class="role-badge">管理员</span>
      </div>

      <div v-if="loginSuccess" class="success-message">
        <span>✅</span> 登录成功！欢迎回来，管理员 {{ adminUsername }}
      </div>

      <template v-else>
        <div class="input-group">
          <label>👤 管理员账号</label>
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
          <label>🔑 密码</label>
          <input
            type="password"
            class="input-field"
            v-model="credentials.password"
            autocomplete="off"
            @keyup.enter="handleLogin"
          />
        </div>

        <div class="input-group">
          <label>📷 验证码</label>
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
          <span>⚠️</span> {{ errorMsg }}
        </div>

        <button type="button" class="login-btn" :disabled="loading" @click="handleLogin">
          <span v-if="loading">⏳ 处理中...</span>
          <span v-else>🚀 安全登录</span>
        </button>
      </template>

      <div class="footer-note">
        <strong>⚠️ 仅限管理员登录</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted ,nextTick } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

// Use same-origin path so HTTPS/TLS is always respected in production.
const API_BASE_URL = '/api'
const router = useRouter()
const isOpen = ref(false)

const credentials = reactive({
  username: '',
  password: '',
  captchaInput: ''
})

const currentCaptcha = ref('')
const errorMsg = ref('')
const loading = ref(false)
const loginSuccess = ref(false)
const adminUsername = ref('')


const generateCaptcha = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'
  let captcha = ''
  const length = 5
  for (let i = 0; i < length; i++) {
    captcha += chars[Math.floor(Math.random() * chars.length)]
  }
  currentCaptcha.value = captcha
  credentials.captchaInput = ''
  
  if (credentials.username && credentials.username.trim()) {
    axios.post(`${API_BASE_URL}/captcha`, {
      username: credentials.username,
      captcha: captcha
    }).catch(err => {
      console.error('验证码存储失败:', err)
    })
  }
}

generateCaptcha()

const handleLogin = async () => {
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
    await axios.post(`${API_BASE_URL}/captcha`, {
      username: credentials.username,
      captcha: currentCaptcha.value
    })
    
    const response = await axios.post(`${API_BASE_URL}/login`, {
      username: credentials.username,
      password: credentials.password,
      captcha: credentials.captchaInput
    })

    loginSuccess.value = true
    adminUsername.value = response.data.username
    localStorage.setItem('adminUsername', response.data.username)
    errorMsg.value = ''
    window.location.href = '/ToolsStandalone'
    return 
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response) {
      errorMsg.value = error.response.data
      console.error('错误详情:', error.response.data);
    }else if (error.request) {
      errorMsg.value = '网络错误，请稍后重试'
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
.login-page {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  background: linear-gradient(135deg, var(--color-bg) 0%, #e8efe9 100%);
}

@media (max-width: 768px) {
  .login-page {
    padding: 1.5rem 1rem;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 1rem 0.875rem;
    align-items: flex-start;
    padding-top: 2rem;
  }
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 440px;
  padding: 2.5rem 2rem;
  transition: all 0.2s ease;
}

@media (max-width: 768px) {
  .login-card {
    padding: 2rem 1.5rem;
    max-width: 400px;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 1.5rem 1.25rem;
    max-width: 100%;
    border-radius: var(--radius);
  }
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
  color: var(--color-primary-dark);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

@media (max-width: 480px) {
  .login-header h2 {
    font-size: 1.25rem;
    gap: 0.35rem;
  }
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

@media (max-width: 480px) {
  .role-badge {
    padding: 0.25rem 0.75rem;
    font-size: 0.75rem;
  }
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
  letter-spacing: 0.3px;
}

.input-field {
  width: 100%;
  padding: 1rem 1.25rem;
  background-color: white;
  border: 2px solid #e0e6e1;
  border-radius: var(--radius);
  font-size: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
}

.input-field:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(45, 90, 61, 0.15);
}

.input-field::placeholder {
  color: #9ca89e;
  font-size: 0.95rem;
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
  box-shadow: inset 0 -2px 0 rgba(0,0,0,0.2);
  border: 2px solid rgba(255,255,255,0.2);
  cursor: pointer;
  user-select: none;
  transition: transform 0.15s, box-shadow 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1.2;
}

.captcha-box:hover {
  transform: scale(1.02);
  box-shadow: var(--shadow-md);
}

.captcha-box:active {
  transform: scale(0.98);
}

.error-message {
  color: var(--color-danger);
  font-size: 0.9rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  background: #fee2e2;
  padding: 0.75rem 1rem;
  border-radius: var(--radius);
  width: fit-content;
}

.success-message {
  color: #065f46;
  background: #d1fae5;
  padding: 1rem 1.25rem;
  border-radius: var(--radius-lg);
  font-size: 0.95rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border: 1px solid #a7f3d0;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  border: none;
  padding: 1rem 1.25rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--radius);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: var(--shadow-md);
  margin-top: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.footer-note {
  margin-top: 2rem;
  font-size: 0.85rem;
  color: var(--color-text-muted);
  text-align: center;
  border-top: 1px dashed #e0e6e1;
  padding-top: 1.25rem;
}

.footer-note strong {
  color: var(--color-primary-dark);
  font-weight: 600;
  background: var(--color-bg);
  padding: 0.25rem 0.875rem;
  border-radius: 50px;
  font-size: 0.8rem;
  display: inline-block;
}
</style>