<template>
  <div class="verify-page">
    <h1>产品验证</h1>
    <p class="page-desc">输入防伪码或扫码验证产品真伪</p>
    
    <VerifyForm
      ref="verifyFormRef"
      @verified="onVerified"
      @invalid="onInvalid"
    />
    
    <div v-if="showResult && traceData" class="result-section">
      <ResultDisplay
        :trace-data="traceData"
        @complaint="showComplaintDialog = true"
      />
    </div>
    
    <div v-else-if="showFakeAlert" class="fake-alert">
      <div class="alert-content">
        <div class="alert-icon">⚠️</div>
        <h3>验证失败</h3>
        <p>{{ fakeAlertMessage }}</p>
      </div>
    </div>
    
    <ComplaintDialog
      v-if="showComplaintDialog"
      :complaint-data="complaintData"
      @close="showComplaintDialog = false"
      @submitted="showComplaintDialog = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
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
  fakeAlertMessage.value = message
  showFakeAlert.value = true
  showResult.value = false
}
</script>

<style scoped>
.verify-page {
  max-width: 600px;
  margin: 0 auto;
  padding: var(--spacing-2xl) var(--spacing-lg);
}

.verify-page h1 {
  text-align: center;
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
}

.page-desc {
  text-align: center;
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xl);
}

.result-section {
  margin-top: var(--spacing-xl);
}

.fake-alert {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: center;
}

.alert-content {
  background: #fef3c7;
  border: 2px solid #f59e0b;
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  text-align: center;
  max-width: 480px;
  width: 100%;
}

.alert-icon {
  font-size: 2.5rem;
  margin-bottom: var(--spacing-sm);
}

.alert-content h3 {
  color: #92400e;
  font-size: 1.2rem;
  margin-bottom: var(--spacing-sm);
}

.alert-content p {
  color: #78350f;
}
</style>
