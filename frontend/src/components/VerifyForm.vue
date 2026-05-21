<template>
  <form class="verify-form" @submit.prevent="handleSubmit" novalidate>
    <div class="input-wrapper">
      <input
        v-model="antiFakeCode"
        type="text"
        placeholder="请输入产品防伪码"
        maxlength="64"
        autocomplete="off"
        :disabled="loading"
        ref="inputRef"
      />
      <button type="submit" class="btn-verify" :disabled="loading">
        {{ loading ? '验证中...' : '验证' }}
      </button>
    </div>
    <button type="button" class="btn-scan" @click="startScan">
      扫码查询
    </button>
    
    <!-- 扫码弹窗 -->
    <div v-if="showCamera" class="camera-overlay" @click.self="stopCamera">
      <div class="camera-container">
        <div class="camera-header">
          <span>请对准二维码</span>
          <button type="button" class="btn-close" @click="stopCamera">关闭</button>
        </div>
        <div class="camera-content">
          <video ref="videoRef" class="camera-video" autoplay playsinline></video>
          <div class="scan-frame">
            <div class="scan-corner top-left"></div>
            <div class="scan-corner top-right"></div>
            <div class="scan-corner bottom-left"></div>
            <div class="scan-corner bottom-right"></div>
          </div>
        </div>
      </div>
    </div>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import jsQR from 'jsqr'
import { verifyAntiFakeCode } from '../api'

const emit = defineEmits(['verified', 'invalid'])

const antiFakeCode = ref('')
const loading = ref(false)
const inputRef = ref(null)
const showCamera = ref(false)
const videoRef = ref(null)
let stream = null
let scanInterval = null

async function startScan() {
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' }
    })
    showCamera.value = true

    setTimeout(() => {
      if (videoRef.value) {
        videoRef.value.srcObject = stream
        startQrScan()
      }
    }, 100)
  } catch (err) {
    ElMessage.warning('无法访问摄像头，请确保已授予权限')
  }
}

function startQrScan() {
  scanInterval = setInterval(async () => {
    if (!videoRef.value || videoRef.value.readyState !== 4) return

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
        let scannedCode = ''

        if (url.includes('code=')) {
          const params = new URLSearchParams(url.split('?')[1])
          scannedCode = params.get('code')
        } else {
          scannedCode = url
        }

        if (scannedCode) {
          stopCamera()
          antiFakeCode.value = scannedCode
          await handleSubmit()
        }
      }
    } catch (err) {
      // 忽略识别错误
    }
  }, 500)
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

async function handleSubmit() {
  if (!antiFakeCode.value || antiFakeCode.value.trim().length < 6) {
    ElMessage.warning('防伪码至少 6 位')
    return
  }
  
  loading.value = true
  try {
    const code = antiFakeCode.value.trim()
    sessionStorage.setItem('scannedCode', code)
    const result = await verifyAntiFakeCode(code)
    
    if (result.valid && result.data) {
      emit('verified', result.data)
    } else {
      emit('invalid', result.message || '未找到该防伪码对应的产品信息')
    }
  } catch (err) {
    ElMessage.error('验证失败，请检查网络连接')
    emit('invalid', '验证失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

defineExpose({ 
  focus: () => inputRef.value?.focus(),
  setCode: (code) => { antiFakeCode.value = code }
})
</script>

<style scoped>
.verify-form {
  width: 100%;
  max-width: 520px;
  margin: 0 auto;
}

.input-wrapper {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.input-wrapper input {
  flex: 1;
  padding: 1rem 1.25rem;
  border: 2px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 16px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-wrapper input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(45, 90, 61, 0.15);
}

.input-wrapper input::placeholder {
  color: var(--color-text-light);
}

.btn-verify {
  padding: 1rem 2rem;
  background: var(--color-primary);
  color: white;
  font-weight: 600;
  font-size: 1rem;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.btn-verify:hover:not(:disabled) {
  background: var(--color-primary-light);
}

.btn-verify:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-scan {
  width: 100%;
  padding: 1rem;
  background: var(--color-bg-card);
  color: var(--color-primary);
  font-weight: 500;
  font-size: 1rem;
  border: 2px solid var(--color-primary);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s;
}

.btn-scan:hover {
  background: var(--color-primary);
  color: white;
}

.camera-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.camera-container {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  width: 90%;
  max-width: 500px;
  overflow: hidden;
}

.camera-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
}

.camera-header span {
  font-weight: 500;
}

.btn-close {
  padding: 0.4rem 1rem;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  cursor: pointer;
}

.btn-close:hover {
  background: var(--color-border-light);
}

.camera-content {
  position: relative;
  padding: var(--spacing-lg);
}

.camera-video {
  width: 100%;
  border-radius: var(--radius);
}

.scan-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  pointer-events: none;
}

.scan-corner {
  position: absolute;
  width: 30px;
  height: 30px;
  border: 3px solid var(--color-primary);
}

.scan-corner.top-left {
  top: 0;
  left: 0;
  border-right: none;
  border-bottom: none;
}

.scan-corner.top-right {
  top: 0;
  right: 0;
  border-left: none;
  border-bottom: none;
}

.scan-corner.bottom-left {
  bottom: 0;
  left: 0;
  border-right: none;
  border-top: none;
}

.scan-corner.bottom-right {
  bottom: 0;
  right: 0;
  border-left: none;
  border-top: none;
}

@media (max-width: 768px) {
  .input-wrapper {
    flex-direction: column;
  }
  
  .input-wrapper input {
    font-size: 16px;
  }
  
  .btn-verify {
    width: 100%;
  }
  
  .btn-scan {
    font-size: 0.95rem;
  }
}
</style>
