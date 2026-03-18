<template>
  <div class="tools-standalone">
    <!-- 添加标签页切换 -->
    <el-tabs v-model="activeTab" class="tools-tabs">
      <el-tab-pane label="防伪工具" name="security">
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
            <p class="card-desc">生成12-20位防伪码</p>
            <div class="gen-form">
              <div class="form-row">
                <label>生成数量</label>
                <input v-model.number="genCount" type="number" min="1" max="100" />
              </div>
              <div class="form-row">
                <label>密码长度</label>
                <input v-model="length" type="number" min="12" />
              </div>
              <button type="button" class="btn-generate" @click="generate">生成防伪码</button>
            </div>
            <div v-if="generatedCodes.length" class="codes-output">
              <div class="codes-header">
                <span>已生成 {{ generatedCodes.length }} 个防伪码</span>
                <button type="button" class="btn-copy" @click="copyCodes">复制全部</button>
              </div>
              <div class="codes-list">
                <div v-for="(code, i) in generatedCodes" :key="i" class="code-item">
                  {{ code }}
                </div>
              </div>
            </div>
          </section>
        </div>
      </el-tab-pane>

      <!-- 防伪码生成管理标签页 -->
      <el-tab-pane label="防伪码生成" name="qrcode">
        <div class="tools-page">
          <h1>防伪码管理</h1>
          <p class="tools-desc">为已录入的产品生成防伪二维码，下载后贴到对应产品包装上</p>

          <section class="tool-card">
            <h2>选择产品生成二维码</h2>
            <div class="qr-gen-form">
              <div class="form-row">
                <label>选择产品</label>
                <el-select v-model="selectedProductId" placeholder="选择产品" filterable @change="onProductChange">
                  <el-option
                    v-for="product in productList"
                    :key="product.id || product.productId"
                    :label="`${product.name} (${product.batchNumber})`"
                    :value="product.id || product.productId"
                  />
                </el-select>
              </div>
              <button type="button" class="btn-generate" @click="generateProductQrCode" :disabled="!selectedProductId || generating">
                {{ generating ? '生成中...' : '生成二维码' }}
              </button>
            </div>
            
            <div v-if="generatedQrCode" class="qr-display">
              <h3>{{ selectedProductName }} - {{ selectedBatchNumber }}</h3>
              <div class="qr-wrap">
                <img :src="generatedQrCode" alt="产品二维码" class="qr-image" />
              </div>
              <p class="qr-code-text">防伪码: {{ selectedAntiFakeCode }}</p>
              <button type="button" class="btn-download" @click="downloadProductQrCode">下载二维码</button>
            </div>
          </section>

          <section class="tool-card">
            <h2>已生成二维码的产品列表</h2>
            <el-table :data="productsWithQrCode" style="width: 100%">
              <el-table-column prop="name" label="产品名称" />
              <el-table-column prop="batchNumber" label="批号" />
              <el-table-column prop="antiFakeCode" label="防伪码" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewQrCode(scope.row)">查看</el-button>
                  <el-button type="success" size="small" @click="downloadProductQrCodeFromRow(scope.row)">下载</el-button>
                </template>
              </el-table-column>
            </el-table>
          </section>
        </div>
      </el-tab-pane>

      <!--投诉管理工具标签页 -->
      <el-tab-pane label="投诉管理" name="complaint">
        <ComplaintAdminTool />
      </el-tab-pane>

      <!--数据录入标签页 -->
      <el-tab-pane label="数据录入" name="insert">
        <InsertDataTool />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import QRCode from 'https://cdn.jsdelivr.net/npm/qrcode@1.5.3/+esm'
import ComplaintAdminTool from '/src/views/getAllComplaintInfo.vue'
import { ElMessage } from 'element-plus'
import InsertDataTool from '../components/InsertDataTool.vue';
import { listAllProducts, generateQrCode } from '../services/api'


const length = ref(16)
const loading = ref(false)
const lastClickTime = ref(0)

const activeTab = ref('security')

const qrCanvas = ref(null)
const genCount = ref(5)
const startSeq = ref(1)
const generatedCodes = ref([])

// 二维码生成相关
const productList = ref([])
const selectedProductId = ref(null)
const selectedProductName = ref('')
const selectedBatchNumber = ref('')
const selectedAntiFakeCode = ref('')
const generatedQrCode = ref('')
const generating = ref(false)

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

const generate = async () => {
  const now = Date.now()
  if (now - lastClickTime.value < 1000) {
    ElMessage.warning('操作太频繁，请稍后再试')
    return
  }

  lastClickTime.value = now
  loading.value = true
  generatedCodes.value = []

  try {
    const baseParams = new URLSearchParams({
      length: length.value.toString()
    })
    baseParams.append('numbers', '')
    baseParams.append('uppercase', '')
    baseParams.append('lowercase', '')

    const baseUrl = 'https://60s.viki.moe/v2/password'
    const requestUrl = `${baseUrl}?${baseParams.toString()}`

    const requests = Array(genCount.value).fill().map(() =>
      fetch(requestUrl).then(res => res.json())
    )

    const results = await Promise.all(requests)
    generatedCodes.value = results.map(item => item.data.password)

    ElMessage.success(`成功生成 ${genCount.value} 个防伪码`)

  } catch (error) {
    console.error(error)
    ElMessage.error('获取失败，请重试')
  } finally {
    loading.value = false
  }
}

function copyCodes() {
  const text = generatedCodes.value.join('\n')
  navigator.clipboard.writeText(text).then(() => {
    alert('已复制到剪贴板')
  }).catch(() => {
    alert('复制失败，请手动复制')
  })
}

async function loadProductList() {
  try {
    const data = await listAllProducts()
    productList.value = data || []
  } catch (error) {
    console.error('Failed to load product list:', error)
  }
}

const productsWithQrCode = computed(() => {
  return productList.value.filter(p => p.qrCodeUrl || (p.productId && p.qrCodeUrl))
})

function onProductChange() {
  const product = productList.value.find(p => p.id === selectedProductId.value || p.productId === selectedProductId.value)
  if (product) {
    selectedProductName.value = product.name
    selectedBatchNumber.value = product.batchNumber
    selectedAntiFakeCode.value = product.antiFakeCode
    generatedQrCode.value = product.qrCodeUrl || ''
  } else {
    selectedProductName.value = ''
    selectedBatchNumber.value = ''
    selectedAntiFakeCode.value = ''
    generatedQrCode.value = ''
  }
}

async function generateProductQrCode() {
  if (!selectedProductId.value) return
  
  generating.value = true
  try {
    const result = await generateQrCode(selectedProductId.value)
    generatedQrCode.value = result.qrCodeUrl
    ElMessage.success('二维码生成成功')
    await loadProductList()
  } catch (error) {
    console.error('Failed to generate QR code:', error)
    ElMessage.error('二维码生成失败')
  } finally {
    generating.value = false
  }
}

function viewQrCode(row) {
  selectedProductId.value = row.id || row.productId
  selectedProductName.value = row.name
  selectedBatchNumber.value = row.batchNumber
  selectedAntiFakeCode.value = row.antiFakeCode
  generatedQrCode.value = row.qrCodeUrl || ''
}

function downloadProductQrCode() {
  if (!generatedQrCode.value) return
  const a = document.createElement('a')
  a.href = generatedQrCode.value
  a.download = `防伪码_${selectedAntiFakeCode.value}.png`
  a.click()
}

function downloadProductQrCodeFromRow(row) {
  if (!row.qrCodeUrl) return
  const a = document.createElement('a')
  a.href = row.qrCodeUrl
  a.download = `防伪码_${row.antiFakeCode}.png`
  a.click()
}

onMounted(() => {
  drawQr()
  loadProductList()
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

.tools-standalone :deep(.el-tabs__content) {
  overflow: visible;
}

@media (max-width: 480px) {
  .tools-standalone {
    padding: 1rem 0.875rem;
  }
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
  border: 1px solid rgba(45, 90, 61, 0.06);
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
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  border-radius: var(--radius);
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-download:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
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
  color: var(--color-text);
}

.form-row input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #e0e6e1;
  border-radius: var(--radius);
  font-size: 0.95rem;
  transition: border-color 0.2s;
}

.form-row input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.btn-generate {
  padding: 0.75rem;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  border-radius: var(--radius);
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-generate:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
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
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.btn-copy {
  padding: 0.35rem 0.75rem;
  background: rgba(45, 90, 61, 0.1);
  color: var(--color-primary);
  border-radius: 6px;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-copy:hover {
  background: rgba(45, 90, 61, 0.2);
}

.codes-list {
  max-height: 200px;
  overflow-y: auto;
  background: var(--color-bg);
  border-radius: var(--radius);
  padding: 0.5rem;
}

.code-item {
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  padding: 0.35rem 0;
  border-bottom: 1px solid #eee;
}

.code-item:last-child {
  border-bottom: none;
}

.tools-tabs {
  width: 100%;
  max-width: 1200px;
}

.tools-tabs :deep(.el-tabs__header) {
  margin-bottom: 1.5rem;
}

.tools-tabs :deep(.el-tabs__item) {
  font-size: 1rem;
  padding: 0 1.25rem;
}

.qr-gen-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.qr-gen-form .form-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.qr-gen-form .form-row label {
  min-width: 80px;
  font-size: 0.95rem;
  color: var(--color-text);
}

.qr-gen-form .el-select {
  flex: 1;
}

.qr-display {
  margin-top: 2rem;
  text-align: center;
  padding: 1.5rem;
  background: var(--color-bg);
  border-radius: var(--radius);
}

.qr-display h3 {
  margin-bottom: 1rem;
  color: var(--color-primary);
}

.qr-image {
  max-width: 250px;
  border-radius: var(--radius);
  border: 2px solid #eee;
}

.qr-code-text {
  margin: 1rem 0;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}
</style>