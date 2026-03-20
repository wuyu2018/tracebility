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

    <div v-if="showQueriedWarning" class="queried-warning">
      <div class="warning-content">
        <span class="warning-icon">⚠️</span>
        <div class="warning-text">
          <h4>注意：此产品曾被查询过</h4>
          <p>首次查询时间：{{ previousQueryTime }}</p>
        </div>
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
import { ref, onMounted } from 'vue'
import VerifyForm from '../components/VerifyForm.vue'
import ResultDisplay from '../components/ResultDisplay.vue'
import IntroductionSection from '../components/IntroductionSection.vue'
import { verifyAntiFakeCodeGet } from '../services/api'

const verifyFormRef = ref(null)
const traceData = ref(null)
const showResult = ref(false)
const showFakeAlert = ref(false)
const fakeAlertMessage = ref('')
const showQueriedWarning = ref(false)
const previousQueryTime = ref('')

const onVerified = (data, isQueriedBefore = false, queryTime = '') => {
  traceData.value = data
  showResult.value = true
  showFakeAlert.value = false
  showQueriedWarning.value = isQueriedBefore
  previousQueryTime.value = queryTime
}

const onInvalid = (message) => {
  fakeAlertMessage.value = message || '未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！'
  showFakeAlert.value = true
  showResult.value = false
  traceData.value = null
  showQueriedWarning.value = false
}

onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const code = params.get('code')
  if (code) {
    queryByCode(code)
  }
})

async function queryByCode(code) {
  try {
    const result = await verifyAntiFakeCodeGet(code)
    if (result.valid) {
      if (result.data) {
        onVerified(result.data, result.isQueriedBefore || false, result.previousQueryTime || '')
      } else if (result.productName) {
        onVerified({ product: { name: result.productName, specification: result.specification } })
      } else {
        onInvalid('产品信息验证通过')
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
  align-items: center;
  margin-bottom: 2.5rem;
}

.result-section {
  margin-top: 2rem;
}

.home-intro {
  margin-top: auto;
}

.queried-warning {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
}

.warning-content {
  background: linear-gradient(145deg, #fffbe6 0%, #fff3cc 100%);
  border: 2px solid #f5c6c0;
  border-radius: var(--radius-lg);
  padding: 1rem 1.5rem;
  max-width: 480px;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.warning-icon {
  font-size: 1.5rem;
}

.warning-text h4 {
  color: #856404;
  font-size: 1rem;
  margin-bottom: 0.25rem;
}

.warning-text p {
  color: #856404;
  font-size: 0.875rem;
  margin: 0;
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
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-5px);
  }
  75% {
    transform: translateX(5px);
  }
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