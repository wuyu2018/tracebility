<template>
  <div class="home">
    <section class="hero">
      <p class="hero-kicker">Consumer Portal</p>
      <h1>食品溯源 · 安全可追溯</h1>
      <p>扫描二维码或输入防伪码，实时验证真伪并查看批次全流程信息</p>
      <div class="hero-badges">
        <span>官方数据源</span>
        <span>实时校验</span>
        <span>投诉直达</span>
      </div>
    </section>

    <section class="verify-shell">
      <h2>快速验证</h2>
      <p>支持输入防伪码或直接扫码查询</p>
      <div class="verify-section">
        <VerifyForm
          ref="verifyFormRef"
          @verified="onVerified"
          @invalid="onInvalid"
        />
      </div>
    </section>

    <section class="result-shell" v-if="showResult && traceData">
      <h3>验证结果</h3>
      <div class="result-section">
        <ResultDisplay
          :trace-data="traceData"
          @complaint="showComplaintDialog = true"
        />
      </div>
    </section>

    <section v-else-if="showFakeAlert" class="fake-alert">
      <div class="alert-content">
        <div class="alert-icon">!</div>
        <h3>产品可能是伪品</h3>
        <p>{{ fakeAlertMessage }}</p>
        <p class="tip">请谨慎购买，并通过投诉入口提交问题线索</p>
      </div>
    </section>

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
  padding: 2rem 1rem 2.8rem;
  max-width: 960px;
  margin: 0 auto;
  min-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.hero {
  border-radius: 22px;
  padding: 2rem 1.4rem;
  text-align: left;
  background: linear-gradient(135deg, #0f172a 0%, #1e40af 55%, #0ea5e9 100%);
  color: #e6efff;
  box-shadow: 0 18px 45px rgba(30, 64, 175, 0.25);
}

.hero-kicker {
  font-size: 0.76rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  opacity: 0.88;
  margin-bottom: 0.8rem;
}

.hero h1 {
  font-size: clamp(1.6rem, 2.8vw, 2.2rem);
  font-weight: 700;
  color: #f8fbff;
  margin-bottom: 0.7rem;
}

.hero p {
  color: #dbeafe;
  font-size: 1rem;
  line-height: 1.6;
}

.hero-badges {
  display: flex;
  gap: 0.55rem;
  flex-wrap: wrap;
  margin-top: 1rem;
}

.hero-badges span {
  font-size: 0.8rem;
  border: 1px solid rgba(219, 234, 254, 0.38);
  border-radius: 999px;
  padding: 0.3rem 0.68rem;
  background: rgba(255, 255, 255, 0.08);
}

.verify-shell,
.result-shell {
  border-radius: 18px;
  border: 1px solid #dbe6f2;
  background: #ffffff;
  padding: 1.2rem;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.verify-shell h2,
.result-shell h3 {
  color: #0f172a;
  margin-bottom: 0.35rem;
}

.verify-shell p {
  color: #64748b;
  margin-bottom: 0.85rem;
}

.verify-section {
  margin-bottom: 0;
}

.result-section {
  margin-top: 0.3rem;
}

.fake-alert {
  margin-top: 0.2rem;
  display: flex;
  justify-content: center;
}

.alert-content {
  background: linear-gradient(145deg, #fff1f2 0%, #ffe4e6 100%);
  border: 1px solid #fecdd3;
  border-radius: 16px;
  padding: 1.1rem;
  max-width: 560px;
  width: 100%;
  text-align: center;
}

.alert-icon {
  width: 42px;
  height: 42px;
  margin: 0 auto 0.5rem;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: 800;
  color: #b91c1c;
  background: #fee2e2;
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
    padding: 1.2rem 0.8rem 2rem;
  }

  .hero {
    padding: 1.35rem 1rem;
  }

  .hero h1 {
    font-size: 1.5rem;
  }

  .hero p {
    font-size: 0.9rem;
  }

  .alert-content {
    padding: 0.95rem;
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
