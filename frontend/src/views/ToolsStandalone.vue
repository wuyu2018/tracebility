<template>
  <div class="tools-standalone">
    <div class="tools-page">
      <h1>防伪工具</h1>
      <p class="tools-desc">生成防伪验证二维码及防伪码</p>

      <section class="tool-card">
        <h2>防伪验证二维码</h2>
        <p class="card-desc">扫描此二维码可直达防伪验证页面</p>
        <div class="qr-wrap">
          <canvas ref="qrCanvas" class="qr-canvas"></canvas>
        </div>
        <p class="qr-url">{{ verifyUrl }}</p>
        <button type="button" class="btn-download" @click="downloadQr">下载二维码</button>
      </section>

      <section class="tool-card">
        <h2>防伪码生成器</h2>
        <p class="card-desc">生成12-20位防伪码，格式：AF + 年份 + 6位序号</p>
        <div class="gen-form">
          <div class="form-row">
            <label>生成数量</label>
            <input v-model.number="genCount" type="number" min="1" max="100" />
          </div>
          <div class="form-row">
            <label>起始序号</label>
            <input v-model.number="startSeq" type="number" min="1" />
          </div>
          <button type="button" class="btn-generate" @click="generate">生成防伪码</button>
        </div>
        <div v-if="generatedCodes.length" class="codes-output">
          <div class="codes-header">
            <span>已生成 {{ generatedCodes.length }} 个防伪码</span>
            <button type="button" class="btn-copy" @click="copyCodes">复制全部</button>
          </div>
          <div class="codes-list">
            <div v-for="(code, i) in generatedCodes" :key="i" class="code-item">{{ code }}</div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import QRCode from 'https://cdn.jsdelivr.net/npm/qrcode@1.5.3/+esm'

const qrCanvas = ref(null)
const genCount = ref(5)
const startSeq = ref(1)
const generatedCodes = ref([])

function getVerifyUrl() {
  if (typeof window === 'undefined') return ''
  return window.location.origin + '/'
}

const verifyUrl = computed(() => getVerifyUrl())

function drawQr() {
  const url = getVerifyUrl()
  if (!qrCanvas.value || !url) return
  QRCode.toCanvas(qrCanvas.value, url, {
    width: 200,
    margin: 2,
  }, (err) => {
    if (err) console.error(err)
  })
}

function downloadQr() {
  const url = getVerifyUrl()
  if (!url) return
  QRCode.toDataURL(url, { width: 400, margin: 2 }).then((dataUrl) => {
    const a = document.createElement('a')
    a.href = dataUrl
    a.download = '防伪验证二维码.png'
    a.click()
  })
}

function generate() {
  const count = Math.min(Math.max(1, genCount.value || 1), 100)
  const start = Math.max(1, startSeq.value || 1)
  const year = new Date().getFullYear()
  const codes = []
  for (let i = 0; i < count; i++) {
    const seq = String(start + i).padStart(6, '0')
    codes.push(`AF${year}${seq}`)
  }
  generatedCodes.value = codes
}

function copyCodes() {
  const text = generatedCodes.value.join('\n')
  navigator.clipboard.writeText(text).then(() => {
    alert('已复制到剪贴板')
  }).catch(() => {
    alert('复制失败，请手动复制')
  })
}

onMounted(() => {
  drawQr()
})
</script>

<style scoped>
.tools-standalone {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 2rem 1rem;
}

.tools-page {
  max-width: 500px;
  width: 100%;
}

.tools-page h1 {
  font-size: 1.5rem;
  color: var(--color-primary-dark);
  margin-bottom: 0.5rem;
}

.tools-desc {
  color: var(--color-text-muted);
  font-size: 0.95rem;
  margin-bottom: 2rem;
}

.tool-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(45, 90, 61, 0.08);
}

.tool-card h2 {
  font-size: 1.1rem;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.card-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-bottom: 1.25rem;
}

.qr-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.qr-canvas {
  border-radius: var(--radius);
}

.qr-url {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  word-break: break-all;
  margin-bottom: 1rem;
  text-align: center;
}

.btn-download {
  width: 100%;
  padding: 0.75rem;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius);
  font-weight: 500;
  border: none;
  cursor: pointer;
}

.btn-download:hover {
  background: var(--color-primary-light);
}

.gen-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.form-row label {
  min-width: 80px;
  font-size: 0.95rem;
}

.form-row input {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: 1px solid #e0e6e1;
  border-radius: 6px;
}

.btn-generate {
  padding: 0.75rem;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius);
  font-weight: 500;
  border: none;
  cursor: pointer;
}

.btn-generate:hover {
  background: var(--color-primary-light);
}

.codes-output {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.codes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.btn-copy {
  padding: 0.35rem 0.75rem;
  background: rgba(45, 90, 61, 0.1);
  color: var(--color-primary);
  border-radius: 6px;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
}

.btn-copy:hover {
  background: rgba(45, 90, 61, 0.2);
}

.codes-list {
  max-height: 200px;
  overflow-y: auto;
  background: #f8faf8;
  border-radius: 6px;
  padding: 0.5rem;
}

.code-item {
  font-family: monospace;
  font-size: 0.9rem;
  padding: 0.35rem 0;
  border-bottom: 1px solid #eee;
}

.code-item:last-child {
  border-bottom: none;
}

@media (max-width: 480px) {
  .tools-standalone {
    padding: 1rem 0.875rem;
  }
}
</style>
