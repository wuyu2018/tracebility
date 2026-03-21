<template>
  <div class="add-admin-container">
    <div class="add-admin-card">
      <div class="card-header">
        <h2>添加管理员</h2>
        <span class="subtitle">创建新的管理员账户（需超级管理员权限）</span>
      </div>

      <div class="form-section">
        <div class="input-group">
          <label>管理员账号</label>
          <input
            type="text"
            class="input-field"
            v-model="adminForm.username"
            placeholder="请输入管理员账号"
            autocomplete="off"
            @input="validateUsername"
          />
          <span class="field-hint" :class="{ 'hint-error': usernameError }">
            {{ usernameError || '4-20位字母或数字组合' }}
          </span>
        </div>

        <div class="input-group">
          <label>新管理员密码</label>
          <input
            type="password"
            class="input-field"
            v-model="adminForm.password"
            placeholder="请输入密码"
            autocomplete="off"
            @input="validatePassword"
          />
          <span class="field-hint" :class="{ 'hint-error': passwordError }">
            {{ passwordError || '8-20位，必须包含字母、数字和特殊字符' }}
          </span>
        </div>

        <div class="input-group">
          <label>确认密码</label>
          <input
            type="password"
            class="input-field"
            v-model="adminForm.confirmPassword"
            placeholder="请再次输入密码"
            autocomplete="off"
            @keyup.enter="handleAddAdmin"
          />
        </div>

        <div class="security-notice">
          <div class="notice-icon">🔒</div>
          <div class="notice-content">
            <strong>安全验证</strong>
            <p>为确保安全，请输入当前管理员账号的密码进行身份验证</p>
          </div>
        </div>

        <div class="input-group">
          <label>当前管理员密码</label>
          <input
            type="password"
            class="input-field security-input"
            v-model="adminForm.currentPassword"
            placeholder="请输入当前管理员密码"
            autocomplete="off"
          />
        </div>

        <div v-if="errorMsg" class="error-message">
          <span>⚠️</span> {{ errorMsg }}
        </div>

        <div v-if="successMsg" class="success-message">
          <span>✅</span> {{ successMsg }}
        </div>

        <button type="button" class="submit-btn" :disabled="loading || !isFormValid" @click="handleAddAdmin">
          <span v-if="loading">⏳ 处理中...</span>
          <span v-else>🚀 创建管理员</span>
        </button>
      </div>

      <div class="footer-note">
        <strong>⚠️ 操作安全警告</strong>
        <ul>
          <li>仅限超级管理员操作</li>
          <li>新建管理员账号可登录系统</li>
          <li>请妥善保管管理员账号密码</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const API_BASE_URL = '/api'

const adminForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  currentPassword: ''
})

const errorMsg = ref('')
const successMsg = ref('')
const loading = ref(false)
const usernameError = ref('')
const passwordError = ref('')

const isFormValid = computed(() => {
  return (
    adminForm.username &&
    adminForm.password &&
    adminForm.confirmPassword &&
    adminForm.currentPassword &&
    !usernameError.value &&
    !passwordError.value &&
    adminForm.password === adminForm.confirmPassword
  )
})

function validateUsername() {
  const username = adminForm.username
  if (!username) {
    usernameError.value = ''
    return
  }
  if (username.length < 4 || username.length > 20) {
    usernameError.value = '用户名长度必须为4-20位'
    return
  }
  if (!/^[a-zA-Z0-9]+$/.test(username)) {
    usernameError.value = '用户名只能包含字母和数字'
    return
  }
  usernameError.value = ''
}

function validatePassword() {
  const password = adminForm.password
  if (!password) {
    passwordError.value = ''
    return
  }
  if (password.length < 8 || password.length > 20) {
    passwordError.value = '密码长度必须为8-20位'
    return
  }
  if (!/[a-zA-Z]/.test(password)) {
    passwordError.value = '密码必须包含字母'
    return
  }
  if (!/[0-9]/.test(password)) {
    passwordError.value = '密码必须包含数字'
    return
  }
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
    passwordError.value = '密码必须包含特殊字符'
    return
  }
  passwordError.value = ''
}

const handleAddAdmin = async () => {
  errorMsg.value = ''
  successMsg.value = ''

  if (!adminForm.username || !adminForm.username.trim()) {
    errorMsg.value = '请输入管理员账号'
    return
  }
  if (usernameError.value) {
    errorMsg.value = usernameError.value
    return
  }
  if (!adminForm.password || !adminForm.password.trim()) {
    errorMsg.value = '请输入密码'
    return
  }
  if (passwordError.value) {
    errorMsg.value = passwordError.value
    return
  }
  if (adminForm.password !== adminForm.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  if (!adminForm.currentPassword) {
    errorMsg.value = '请输入当前管理员密码进行身份验证'
    return
  }

  const currentAdminUsername = localStorage.getItem('adminUsername')
  if (!currentAdminUsername) {
    errorMsg.value = '无法获取当前管理员信息，请重新登录'
    return
  }

  loading.value = true

  try {
    const response = await axios.post(`${API_BASE_URL}/admin/register`, {
      username: adminForm.username,
      password: adminForm.password,
      currentPassword: adminForm.currentPassword,
      currentAdminUsername: currentAdminUsername
    })

    successMsg.value = response.data.message || '管理员创建成功'
    ElMessage.success(successMsg.value)
    
    adminForm.username = ''
    adminForm.password = ''
    adminForm.confirmPassword = ''
    adminForm.currentPassword = ''

  } catch (error) {
    console.error('创建管理员失败:', error)
    if (error.response) {
      errorMsg.value = error.response.data || error.response.statusText
    } else if (error.request) {
      errorMsg.value = '网络错误，请稍后重试'
    } else {
      errorMsg.value = error.message
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.add-admin-container {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.add-admin-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 480px;
  padding: 2.5rem 2rem;
}

.card-header {
  text-align: center;
  margin-bottom: 2rem;
}

.card-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-primary-dark);
  margin: 0 0 0.5rem 0;
}

.subtitle {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.form-section {
  margin-bottom: 1.5rem;
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

.security-input {
  border-color: var(--color-warning, #e6a23c);
  background-color: #fdf6ec;
}

.field-hint {
  display: block;
  font-size: 0.8rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.hint-error {
  color: var(--color-danger, #f56c6c);
}

.security-notice {
  display: flex;
  gap: 1rem;
  background: #fff3e6;
  border: 1px solid #ffe4b5;
  border-radius: var(--radius);
  padding: 1rem;
  margin-bottom: 1.25rem;
}

.notice-icon {
  font-size: 1.5rem;
  line-height: 1;
}

.notice-content {
  flex: 1;
}

.notice-content strong {
  display: block;
  color: var(--color-warning-dark, #b37b00);
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.notice-content p {
  margin: 0;
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.error-message {
  color: var(--color-danger, #f56c6c);
  font-size: 0.9rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  background: #fef0f0;
  padding: 0.75rem 1rem;
  border-radius: var(--radius);
}

.success-message {
  color: #065f46;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  background: #d1fae5;
  padding: 0.75rem 1rem;
  border-radius: var(--radius);
}

.submit-btn {
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.footer-note {
  margin-top: 2rem;
  font-size: 0.85rem;
  color: var(--color-text-muted);
  text-align: left;
  border-top: 1px dashed #e0e6e1;
  padding-top: 1.25rem;
}

.footer-note strong {
  display: block;
  color: var(--color-danger, #f56c6c);
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.footer-note ul {
  margin: 0;
  padding-left: 1.25rem;
  line-height: 1.8;
}

@media (max-width: 480px) {
  .add-admin-card {
    padding: 1.5rem 1.25rem;
    max-width: 100%;
  }

  .card-header h2 {
    font-size: 1.25rem;
  }
}
</style>
