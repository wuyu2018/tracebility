<template>
  <div class="verify-page">
    <section class="hero-card">
      <h1>产品验证中心</h1>
      <p class="page-desc">输入防伪码或扫码验证产品真伪，并查看完整溯源链路</p>
    </section>

    <section class="form-card">
      <VerifyForm
        ref="verifyFormRef"
        @verified="onVerified"
        @invalid="onInvalid"
      />
    </section>

    <section v-if="showResult && traceData" class="result-section">
      <ResultDisplay
        :trace-data="traceData"
        @complaint="showComplaintDialog = true"
      />
    </section>

    <section v-else-if="showFakeAlert" class="fake-alert">
      <div class="alert-content">
        <div class="alert-icon">!</div>
        <h3>验证失败</h3>
        <p>{{ fakeAlertMessage }}</p>
      </div>
    </section>

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
  max-width: 840px;
  margin: 0 auto;
  padding: 1.8rem 1rem 2.4rem;
}

.hero-card {
  border-radius: 18px;
  padding: 1.35rem 1.1rem;
  background: linear-gradient(130deg, #0f172a 0%, #0369a1 100%);
  color: #f8fbff;
  box-shadow: 0 16px 36px rgba(3, 105, 161, 0.24);
  margin-bottom: 1rem;
}

.verify-page h1 {
  margin-bottom: 0.35rem;
}

.page-desc {
  color: #dbeafe;
}

.form-card {
  border: 1px solid #dbe6f2;
  border-radius: 16px;
  background: #ffffff;
  padding: 1rem;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.result-section {
  margin-top: 1rem;
}

.fake-alert {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
}

.alert-content {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 16px;
  padding: 1rem;
  text-align: center;
  max-width: 560px;
  width: 100%;
}

.alert-icon {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  margin: 0 auto 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  font-weight: 800;
  background: #fee2e2;
  color: #b91c1c;
}

.alert-content h3 {
  color: #991b1b;
  font-size: 1.2rem;
  margin-bottom: 0.4rem;
}

.alert-content p {
  color: #7f1d1d;
}

@media (max-width: 768px) {
  .verify-page {
    padding: 1.1rem 0.75rem 1.8rem;
  }
}
</style>
