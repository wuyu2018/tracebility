<template>
  <div class="blockchain-page">
    <h1>区块链监控</h1>
    <p class="page-desc">查看区块链完整性状态</p>
    
    <div class="page-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="summary" class="summary">
        <div class="summary-card" :class="{ healthy: summary.overallHealthy }">
          <div class="summary-status">
            {{ summary.overallHealthy ? '✓ 区块链完整' : '⚠️ 发现异常' }}
          </div>
          <div class="summary-detail">
            <div>原料链：{{ summary.materialChain?.intact ? '完整' : '异常' }}</div>
            <div>批次总数：{{ summary.batchChains?.totalBatches || 0 }}</div>
            <div>完整批次：{{ summary.batchChains?.intactCount || 0 }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBlockchainMonitorSummary } from '../api'

const loading = ref(true)
const summary = ref(null)

onMounted(async () => {
  try {
    const res = await getBlockchainMonitorSummary()
    summary.value = res.data
  } catch (err) {
    console.error('加载区块链监控失败:', err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.blockchain-page {
  padding: var(--spacing-lg);
}

.blockchain-page h1 {
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
}

.page-desc {
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xl);
}

.page-content {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-md);
}

.loading {
  text-align: center;
  color: var(--color-text-muted);
}

.summary-card {
  padding: var(--spacing-lg);
  border-radius: var(--radius);
  border: 2px solid;
}

.summary-card.healthy {
  background: #d1fae5;
  border-color: #10b981;
  color: #065f46;
}

.summary-card:not(.healthy) {
  background: #fef3c7;
  border-color: #f59e0b;
  color: #92400e;
}

.summary-status {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: var(--spacing-md);
}

.summary-detail {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  font-size: 0.9rem;
}
</style>
