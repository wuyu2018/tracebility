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
    </div>

    <div v-if="showResult && traceData" class="result-section">
      <ResultDisplay
        :trace-data="traceData"
        @complaint="showComplaintDialog = true"
      />
    </div>

    <div v-else-if="showFakeAlert" class="fake-alert">
      <div class="alert-content">
        <div class="alert-icon">⚠️</div>
        <h3>产品可能是伪品</h3>
        <p>{{ fakeAlertMessage }}</p>
        <p class="tip">请谨慎购买，如有疑问请联系正规渠道核实</p>
      </div>
    </div>

    <ComplaintDialog
      v-if="showComplaintDialog"
      :complaint-data="complaintData"
      @close="showComplaintDialog = false"
      @submitted="onComplaintSubmitted"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import VerifyForm from '../components/VerifyForm.vue'
import ResultDisplay from '../components/ResultDisplay.vue'
import ComplaintDialog from '../components/ComplaintDialog.vue'
import { verifyAntiFakeCode } from '../api'

const verifyFormRef = ref(null)
const traceData = ref(null)
const showResult = ref(false)
const showFakeAlert = ref(false)
const fakeAlertMessage = ref('')
const showComplaintDialog = ref(false)
const complaintData = reactive({
  antiFakeCode: '',
  productName: '',
  batchNumber: ''
})

function onVerified(data) {
  traceData.value = data
  showResult.value = true
  showFakeAlert.value = false
  
  complaintData.antiFakeCode = sessionStorage.getItem('scannedCode') || ''
  complaintData.productName = data.product?.name || ''
  complaintData.batchNumber = data.batch?.batchNumber || ''
}

function onInvalid(message) {
  fakeAlertMessage.value = message || '未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！'
  showFakeAlert.value = true
  showResult.value = false
  traceData.value = null
}

function onComplaintSubmitted() {
  showComplaintDialog.value = false
}

onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const code = params.get('code')
  if (code) {
    queryByCode(code)
  }
})

async function queryByCode(code) {
  sessionStorage.setItem('scannedCode', code)
  verifyFormRef.value?.setCode(code)
  
  try {
    const result = await verifyAntiFakeCode(code)
    if (result.valid) {
      if (result.data) {
        onVerified(result.data)
      } else {
        onInvalid('未找到产品信息')
      }
    } else {
      onInvalid(result.message || '该产品可能是伪品，请谨慎购买！')
    }
  } catch (error) {
    onInvalid('验证失败，请检查网络连接')
  }
}
</script>

<style scoped>
.home {
  padding: var(--spacing-2xl) var(--spacing-lg);
  max-width: 800px;
  margin: 0 auto;
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.hero {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.hero h1 {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
}

.hero p {
  color: var(--color-text-muted);
  font-size: 1rem;
  line-height: 1.6;
}

.verify-section {
  margin-bottom: var(--spacing-xl);
}

.result-section {
  margin-top: var(--spacing-lg);
}

.fake-alert {
  margin-top: var(--spacing-lg);
  display: flex;
  justify-content: center;
}

.alert-content {
  background: linear-gradient(145deg, #fff5f3 0%, #ffe8e4 100%);
  border: 2px solid #f5c6c0;
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  max-width: 480px;
  width: 100%;
  text-align: center;
}

.alert-icon {
  font-size: 2.5rem;
  margin-bottom: var(--spacing-sm);
}

.alert-content h3 {
  color: var(--color-danger);
  font-size: 1.2rem;
  margin-bottom: var(--spacing-sm);
}

.alert-content p {
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.alert-content .tip {
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin-top: var(--spacing-md);
}

@media (max-width: 768px) {
  .home {
    padding: var(--spacing-xl) var(--spacing-md);
  }
  
  .hero h1 {
    font-size: 1.5rem;
  }
  
  .hero p {
    font-size: 0.9rem;
  }
  
  .alert-content {
    padding: var(--spacing-lg);
  }
}

@media (max-width: 480px) {
  .home {
    padding: var(--spacing-lg) var(--spacing-sm);
  }
  
  .hero {
    margin-bottom: var(--spacing-xl);
  }
  
  .hero h1 {
    font-size: 1.25rem;
  }
}
</style>
