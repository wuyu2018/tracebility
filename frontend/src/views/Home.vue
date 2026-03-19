<template>
  <div class="home">
    <div class="hero">
      <h1>食品溯源 · 安全可追溯</h1>
      <p>扫描商品二维码或输入防伪码，验证产品真伪并查看全程溯源信息</p>
    </div>

    <div class="verify-section">
      <VerifyForm
        ref="verifyFormRef"
        @verified="onVerified"
        @invalid="onInvalid"
      />
      <button class="btn-scan" @click="startScan">
        <span class="scan-icon">📷</span>
        <span>扫码查询</span>
      </button>
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

    <div v-if="showResult && traceData" class="result-section">
      <ResultDisplay :trace-data="traceData" />
    </div>

    <div v-else-if="showFakeAlert" class="fake-alert">
      <div class="alert-content">
        <span class="alert-icon">⚠️</span>
        <h3>产品可能是伪品</h3>
        <p>{{ fakeAlertMessage }}</p>
        <p class="tip">请谨慎购买，如有疑问请联系正规渠道核实</p>
      </div>
    </div>

    <IntroductionSection class="home-intro" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import VerifyForm from '../components/VerifyForm.vue'
import ResultDisplay from '../components/ResultDisplay.vue'
import IntroductionSection from '../components/IntroductionSection.vue'
import { verifyAntiFakeCode } from '../services/api'

const verifyFormRef = ref(null)
const traceData = ref(null)
const showResult = ref(false)
const showFakeAlert = ref(false)
const fakeAlertMessage = ref('')

const showCamera = ref(false)
const videoRef = ref(null)
let stream = null
let scanInterval = null
let jsQR = null

const onVerified = (data) => {
  traceData.value = data
  showResult.value = true
  showFakeAlert.value = false
}

const onInvalid = (message) => {
  fakeAlertMessage.value = message || '未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！'
  showFakeAlert.value = true
  showResult.value = false
  traceData.value = null
}

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
    console.error('Camera access error:', error)
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
      
      const code = jsQR(ctx.getImageData(0, 0, canvas.width, canvas.height).data, canvas.width, canvas.height)
      
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
      console.error('QR scan error:', error)
    }
  }, 500)
}

async function queryByCode(code) {
  try {
    const result = await verifyAntiFakeCode(code)
    if (result.valid && result.data) {
      onVerified(result.data)
    } else {
      onInvalid(result.message || '该产品可能是伪品，请谨慎购买！')
    }
  } catch (error) {
    console.error('Query error:', error)
    onInvalid('验证失败，请检查网络连接')
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

onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const code = params.get('code')
  if (code) {
    queryByCode(code)
  }
})

onUnmounted(() => {
  stopCamera()
})
</script>

<style scoped>
.home {
  padding: 2rem 1.5rem 4rem;
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: max(4rem, env(safe-area-inset-bottom));
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) {
  .home {
    padding: 1.5rem 1rem 3rem;
  }
}

@media (max-width: 480px) {
  .home {
    padding: 1rem 0.875rem 2.5rem;
  }
}

.hero {
  text-align: center;
  margin-bottom: 3rem;
}

@media (max-width: 768px) {
  .hero {
    margin-bottom: 2rem;
  }
}

.hero h1 {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary-dark);
  margin-bottom: 0.75rem;
  letter-spacing: 0.02em;
}

@media (max-width: 768px) {
  .hero h1 {
    font-size: 1.6rem;
  }
}

@media (max-width: 480px) {
  .hero h1 {
    font-size: 1.35rem;
  }
}

.hero p {
  color: var(--color-text-muted);
  font-size: 1rem;
  line-height: 1.6;
}

@media (max-width: 480px) {
  .hero p {
    font-size: 0.9rem;
  }
}

.verify-section {
  display: flex;
  justify-content: center;
  align-items: stretch;
  gap: 0.75rem;
  margin-bottom: 2.5rem;
}

@media (max-width: 768px) {
  .verify-section {
    flex-direction: column;
    align-items: center;
  }
  .verify-section > * {
    width: 100%;
    max-width: 480px;
  }
}

.btn-scan {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  font-weight: 600;
  font-size: 1rem;
  border-radius: var(--radius);
  border: none;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  white-space: nowrap;
  min-width: 100px;
}

.btn-scan:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.scan-icon {
  font-size: 1.2rem;
}

.camera-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-container {
  position: relative;
  width: 100%;
  max-width: 500px;
}

.camera-video {
  width: 100%;
  display: block;
}

.scan-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 250px;
  height: 250px;
  border: 2px solid #fff;
  border-radius: 12px;
}

.scan-corner {
  position: absolute;
  width: 30px;
  height: 30px;
  border-color: var(--color-primary);
  border-style: solid;
}

.scan-corner.top-left {
  top: -2px;
  left: -2px;
  border-width: 4px 0 0 4px;
  border-radius: 8px 0 0 0;
}

.scan-corner.top-right {
  top: -2px;
  right: -2px;
  border-width: 4px 4px 0 0;
  border-radius: 0 8px 0 0;
}

.scan-corner.bottom-left {
  bottom: -2px;
  left: -2px;
  border-width: 0 0 4px 4px;
  border-radius: 0 0 0 8px;
}

.scan-corner.bottom-right {
  bottom: -2px;
  right: -2px;
  border-width: 0 4px 4px 0;
  border-radius: 0 0 8px 0;
}

.btn-close-camera {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.75rem 2rem;
  background: #fff;
  color: #333;
  border: none;
  border-radius: var(--radius);
  font-size: 1rem;
  cursor: pointer;
}

.result-section {
  margin-top: 2rem;
}

.home-intro {
  margin-top: auto;
}

.fake-alert {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}

.alert-content {
  background: linear-gradient(145deg, #fff5f3 0%, #ffe8e4 100%);
  border: 2px solid #f5c6c0;
  border-radius: var(--radius-lg);
  padding: 2rem;
  max-width: 480px;
  width: 100%;
  text-align: center;
  animation: shake 0.5s ease;
}

@media (max-width: 480px) {
  .alert-content {
    padding: 1.5rem 1rem;
    margin: 0 0.5rem;
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.alert-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 0.75rem;
}

.alert-content h3 {
  color: var(--color-danger);
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
}

.alert-content p {
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.alert-content .tip {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-top: 0.75rem;
}
</style>
