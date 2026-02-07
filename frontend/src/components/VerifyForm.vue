<template>
  <form class="verify-form" @submit.prevent="handleSubmit" novalidate>
    <div class="form-group">
      <label for="antiFakeCode">请输入产品防伪码</label>
      <div class="input-wrap">
        <input
          id="antiFakeCode"
          v-model="antiFakeCode"
          type="text"
          placeholder="请输入12-20位防伪码"
          maxlength="20"
          autocomplete="off"
          :disabled="loading"
          @keydown.enter.prevent="handleSubmit"
          ref="inputRef"
        />
        <button type="submit" class="btn-submit" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>验证</span>
        </button>
      </div>
      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import { validateAntiFakeCode } from '../utils/validation'
import { verifyAntiFakeCode } from '../services/api'

const emit = defineEmits(['verified', 'invalid'])

const antiFakeCode = ref('')
const loading = ref(false)
const error = ref('')
const inputRef = ref(null)

const handleSubmit = async () => {
  error.value = ''
  
  const validation = validateAntiFakeCode(antiFakeCode.value)
  if (!validation.valid) {
    error.value = validation.message
    return
  }

  loading.value = true
  try {
    const result = await verifyAntiFakeCode(antiFakeCode.value.trim())
    
    if (result.valid && result.data) {
      emit('verified', result.data)
    } else {
      emit('invalid', result.message || '该产品可能是伪品，请谨慎购买！')
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
    emit('invalid', '验证失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

defineExpose({ focus: () => inputRef.value?.focus() })
</script>

<style scoped>
.verify-form {
  width: 100%;
  max-width: 480px;
  padding: 0 0.5rem;
}

@media (max-width: 480px) {
  .verify-form {
    padding: 0;
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--color-text);
}

.input-wrap {
  display: flex;
  gap: 0.75rem;
  align-items: stretch;
}

@media (max-width: 480px) {
  .input-wrap {
    flex-direction: column;
    gap: 0.5rem;
  }
  .btn-submit {
    min-height: 48px;
    width: 100%;
  }
}

.input-wrap input {
  flex: 1;
  padding: 1rem 1.25rem;
  border: 2px solid #e0e6e1;
  border-radius: var(--radius);
  font-size: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-wrap input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(45, 90, 61, 0.15);
}

.input-wrap input::placeholder {
  color: #9ca89e;
}

.btn-submit {
  padding: 1rem 2rem;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  font-weight: 600;
  font-size: 1rem;
  border-radius: var(--radius);
  white-space: nowrap;
  transition: transform 0.2s, box-shadow 0.2s;
  min-width: 100px;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-msg {
  color: var(--color-danger);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}
</style>
