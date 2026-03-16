<template>
  <div class="login-card">
    <div class="flex-row">
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
        <div class="captcha-refresh-hint">
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
</template>

<script setup>
import { ref, reactive, onMounted ,nextTick } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const API_BASE_URL = 'http://localhost:8080/api'
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
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-card {
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 32px;
  box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25), 0 0 0 1px rgba(156,163,175,0.1);
  width: 100%;
  max-width: 440px;
  padding: 40px 32px;
  transition: all 0.2s ease;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

h2 {
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: #0a1e2f;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-group {
  margin-bottom: 22px;
}

label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #1e3a5f;
  margin-bottom: 6px;
  letter-spacing: 0.3px;
}

.input-field {
  width: 100%;
  padding: 14px 16px;
  background-color: white;
  border: 1.5px solid #dde3e9;
  border-radius: 18px;
  font-size: 15px;
  transition: all 0.2s;
  outline: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.input-field:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59,130,246,0.15);
}

.input-field::placeholder {
  color: #9ca3af;
  font-size: 14px;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-row .input-field {
  flex: 1;
}

.captcha-box {
  background: linear-gradient(135deg, #2b3c58, #1b2b41);
  color: white;
  font-weight: 700;
  font-size: 26px;
  letter-spacing: 10px;
  text-align: center;
  padding: 8px 10px;
  min-width: 120px;
  border-radius: 18px;
  font-family: 'Courier New', monospace;
  box-shadow: inset 0 -2px 0 rgba(0,0,0,0.2);
  border: 2px solid #ffffff30;
  text-shadow: 2px 2px 0 #00000040;
  cursor: pointer;
  user-select: none;
  transition: 0.15s;
  line-height: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-box:hover {
  background: linear-gradient(135deg, #345, #1f2e44);
  transform: scale(1.01);
}

.captcha-box:active {
  transform: scale(0.98);
}

.captcha-refresh-hint {
  font-size: 11px;
  color: #64748b;
  margin-top: 6px;
  text-align: right;
}

.error-message {
  color: #b91c1c;
  font-size: 13px;
  margin-top: 6px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  background: #fee2e2;
  padding: 8px 16px;
  border-radius: 30px;
  width: fit-content;
}

.success-message {
  color: #065f46;
  background: #d1fae5;
  padding: 16px 20px;
  border-radius: 40px;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #a7f3d0;
}

.login-btn {
  width: 100%;
  background: #1e3a5f;
  color: white;
  border: none;
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 40px;
  cursor: pointer;
  transition: 0.15s;
  box-shadow: 0 10px 20px -8px #1e3a5f70;
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-btn:hover:not(:disabled) {
  background: #143049;
  transform: translateY(-2px);
  box-shadow: 0 15px 25px -10px #0f2b44;
}

.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
  background: #6b7b8f;
}

.login-btn:active:not(:disabled) {
  transform: translateY(1px);
  box-shadow: 0 5px 12px -6px #1e3a5f;
}

.footer-note {
  margin-top: 32px;
  font-size: 13px;
  color: #6d7b8c;
  text-align: center;
  border-top: 1px dashed #cbd5e1;
  padding-top: 22px;
}

.footer-note strong {
  color: #1e3a5f;
  font-weight: 600;
  background: #f0f4f9;
  padding: 4px 14px;
  border-radius: 30px;
  font-size: 12px;
  display: inline-block;
}

.flex-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.role-badge {
  background: #f4f8ff;
  color: #1e4a8b;
  padding: 6px 16px;
  border-radius: 50px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #b9d3f0;
}
</style>