<template>
  <div class="add-admin-container">
    <div class="add-admin-card">
      <div class="card-header">
        <h2>添加管理员</h2>
        <span class="subtitle">创建新的管理员账户</span>
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
          />
        </div>

        <div class="input-group">
          <label>密码</label>
          <input
            type="password"
            class="input-field"
            v-model="adminForm.password"
            placeholder="请输入密码（至少6位）"
            autocomplete="off"
          />
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

        <div v-if="errorMsg" class="error-message">
          <span>⚠️</span> {{ errorMsg }}
        </div>

        <div v-if="successMsg" class="success-message">
          <span>✅</span> {{ successMsg }}
        </div>

        <button type="button" class="submit-btn" :disabled="loading" @click="handleAddAdmin">
          <span v-if="loading">⏳ 处理中...</span>
          <span v-else>🚀 创建管理员</span>
        </button>
      </div>

      <div class="footer-note">
        <strong>⚠️ 仅限超级管理员操作</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const API_BASE_URL = '/api'

const adminForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const errorMsg = ref('')
const successMsg = ref('')
const loading = ref(false)

const handleAddAdmin = async () => {
  errorMsg.value = ''
  successMsg.value = ''

  if (!adminForm.username || !adminForm.username.trim()) {
    errorMsg.value = '请输入管理员账号'
    return
  }
  if (!adminForm.password || !adminForm.password.trim()) {
    errorMsg.value = '请输入密码'
    return
  }
  if (adminForm.password.length < 6) {
    errorMsg.value = '密码长度不能少于6位'
    return
  }
  if (adminForm.password !== adminForm.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  loading.value = true

  try {
    const response = await axios.post(`${API_BASE_URL}/admin/register`, {
      username: adminForm.username,
      password: adminForm.password
    })

    successMsg.value = response.data.message || '管理员创建成功'
    ElMessage.success(successMsg.value)
    
    adminForm.username = ''
    adminForm.password = ''
    adminForm.confirmPassword = ''

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
  max-width: 440px;
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
  opacity: 0.7;
  cursor: not-allowed;
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
