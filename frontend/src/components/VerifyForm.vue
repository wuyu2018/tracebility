<template>
  <form class="verify-form" @submit.prevent="handleSubmit" novalidate>
    <div class="form-group">
      <label for="antiFakeCode">请输入产品防伪码</label>
      <div class="input-wrap">
        <input
          id="antiFakeCode"
          v-model="antiFakeCode"
          type="text"
          placeholder="防伪码"
          maxlength="20"
          autocomplete="off"
          :disabled="loading"
          @keydown.enter.prevent="handleSubmit"
          ref="inputRef"
        />
        <input
          id="batchNumber"
          v-model="batchNumber"
          type="text"
          placeholder="批次号（选填）"
          maxlength="50"
          autocomplete="off"
          :disabled="loading"
          @keydown.enter.prevent="handleSubmit"
        />
        <button type="submit" class="btn-submit" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>验证</span>
        </button>
        <button type="button" class="btn-submit" @click="startScan">
            <span class="scan-icon">扫码查询</span>
        </button>
      </div>
      <p class="form-tip">输入防伪码快速验证，输入防伪码+批次号查看完整溯源信息</p>
      <p v-if="error" class="form-error">{{ error }}</p>
    </div>
    <div v-if="showCamera" class="camera-overlay">
      <div class="camera-container">
        <video ref="videoRef" class="camera-video" autoplay></video>
        <div class="scan-frame">
          <div class="scan-corner top-left"></div>
          <div class="scan-corner top-right"></div>
          <div class="scan-corner bottom-left"></div>
          <div class="scan-corner bottom-right"></div>
        </div>
        <button class="btn-close-camera" @click="stopCamera">关闭</button>
      </div>
    </div>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import { validateAntiFakeCode } from '../utils/validation'
import { verifyAntiFakeCode, verifyAntiFakeCodeGet, verifyAntiFakeCodeWithBatch } from '../services/api'

const emit = defineEmits(['verified', 'invalid'])

const antiFakeCode = ref('')
const batchNumber = ref('')
const loading = ref(false)
const error = ref('')
const inputRef = ref(null)

const showCamera = ref(false)
const videoRef = ref(null)
let stream = null
let scanInterval = null
let jsQR = null

async function loadJsQR() {
  if (jsQR) return jsQR
  const module = await import('https://cdn.jsdelivr.net/npm/jsqr@1.4.0/+esm')
  jsQR = module.default
  return jsQR
}

async function startScan() {
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' }
    })
    showCamera.value = true

    await loadJsQR()

    setTimeout(() => {
      if (videoRef.value) {
        videoRef.value.srcObject = stream
        startQrScan()
      }
    }, 100)
  } catch (error) {
    alert('无法访问摄像头，请确保已授予权限')
  }
}

function startQrScan() {
  scanInterval = setInterval(async () => {
    if (!videoRef.value || videoRef.value.readyState !== 4) return
    if (!jsQR) return

    try {
      const canvas = document.createElement('canvas')
      canvas.width = videoRef.value.videoWidth
      canvas.height = videoRef.value.videoHeight
      const ctx = canvas.getContext('2d')
      ctx.drawImage(videoRef.value, 0, 0)

      const code = jsQR(
        ctx.getImageData(0, 0, canvas.width, canvas.height).data,
        canvas.width,
        canvas.height
      )

      if (code && code.data) {
        const url = code.data
        let antiFakeCode = ''

        if (url.includes('code=')) {
          const params = new URLSearchParams(url.split('?')[1])
          antiFakeCode = params.get('code')
        } else {
          antiFakeCode = url
        }

        if (antiFakeCode && antiFakeCode.length >= 12) {
          stopCamera()
          await queryByCode(antiFakeCode)
        }
      }
    } catch (error) {
    }
  }, 500)
}

async function queryByCode(code) {
  loading.value = true
  try {
    const result = await verifyAntiFakeCodeGet(code)
    if (result.valid) {
      if (result.data) {
        emit('verified', result.data)
      } else if (result.productName) {
        emit('verified', { product: { name: result.productName, specification: result.specification } })
      } else {
        emit('invalid', '产品信息验证通过')
      }
    } else {
      emit('invalid', result.message || '该产品可能是伪品，请谨慎购买！')
    }
  } catch (error) {
    emit('invalid', '验证失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

function stopCamera() {
  showCamera.value = false
  if (scanInterval) {
    clearInterval(scanInterval)
    scanInterval = null
  }
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }
}

const handleSubmit = async () => {
  error.value = ''

  const validation = validateAntiFakeCode(antiFakeCode.value)
  if (!validation.valid) {
    error.value = validation.message
    return
  }
  loading.value = true
  try {
    let result
    if (batchNumber.value && batchNumber.value.trim()) {
      result = await verifyAntiFakeCodeWithBatch(antiFakeCode.value.trim(), batchNumber.value.trim())
    } else {
      result = await verifyAntiFakeCode(antiFakeCode.value.trim())
    }

    if (result.valid) {
      if (result.data) {
        emit('verified', result.data)
      } else if (result.productName) {
        emit('invalid', result.message || '请提供批次号以查看完整溯源信息')
      } else {
        emit('invalid', result.message || '产品信息验证通过')
      }
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
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: stretch;
}

.input-wrap input:first-child {
  flex: 1;
  min-width: 150px;
}

.input-wrap input:nth-child(2) {
  flex: 1;
  min-width: 120px;
}

@media (max-width: 480px) {
  .input-wrap {
    flex-direction: column;
    gap: 0.5rem;
  }
  .input-wrap input {
    width: 100%;
  }
  .btn-submit {
    display: flex;         
    align-items: center;
    justify-content: center;
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
  display: inline-flex;       
  align-items: center;        
  justify-content: center;  
  text-align: center;
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

</style>
