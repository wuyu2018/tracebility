<template>
  <div class="blockchain-monitor">
    <div class="monitor-header">
      <h2>区块链完整性监控</h2>
      <div class="monitor-controls">
        <el-tag :type="autoRefresh ? 'success' : 'info'" size="small" effect="plain">
          自动刷新: {{ autoRefresh ? '已开启' : '已关闭' }}
        </el-tag>
        <el-button size="small" @click="toggleAutoRefresh">
          {{ autoRefresh ? '暂停' : '开启' }}
        </el-button>
        <el-button size="small" type="primary" @click="refreshData" :loading="loading" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <div v-if="error" class="error-banner">
      <el-icon :size="20"><WarningFilled /></el-icon>
      <span>监控数据获取失败，请稍后重试</span>
    </div>

    <div v-if="summary" class="monitor-body">
      <div class="health-banner" :class="summary.overallHealthy ? 'healthy' : 'unhealthy'">
        <el-icon :size="28">
          <SuccessFilled v-if="summary.overallHealthy" />
          <WarningFilled v-else />
        </el-icon>
        <div class="health-text">
          <div class="health-title">
            {{ summary.overallHealthy ? '所有链运行正常' : '存在完整性异常' }}
          </div>
          <div class="health-subtitle">
            最后更新: {{ formatTime(summary.lastUpdated) }}
          </div>
        </div>
      </div>

      <div class="status-cards">
        <el-card shadow="never" class="status-card">
          <template #header>
            <div class="card-header">
              <span>原材料链</span>
              <el-tag :type="summary.materialChain.intact ? 'success' : 'danger'" size="small" effect="dark">
                {{ summary.materialChain.intact ? '完整' : '异常' }}
              </el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="stat-row">
              <span class="stat-label">区块数量</span>
              <span class="stat-value">{{ summary.materialChain.blockCount }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">最近锚定</span>
              <span class="stat-value">{{ summary.materialChain.lastAnchorDate || '未锚定' }}</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="status-card">
          <template #header>
            <div class="card-header">
              <span>批次链</span>
              <el-tag :type="summary.batchChains.brokenCount === 0 ? 'success' : 'warning'" size="small" effect="dark">
                {{ summary.batchChains.brokenCount === 0 ? '正常' : '部分异常' }}
              </el-tag>
            </div>
          </template>
          <div class="card-body">
            <div class="stat-row">
              <span class="stat-label">总批次数</span>
              <span class="stat-value">{{ summary.batchChains.totalBatches }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">完整批次</span>
              <span class="stat-value healthy-count">{{ summary.batchChains.intactCount }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">异常批次</span>
              <span class="stat-value" :class="summary.batchChains.brokenCount > 0 ? 'broken-count' : ''">{{ summary.batchChains.brokenCount }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">总区块数</span>
              <span class="stat-value">{{ summary.batchChains.totalBlockCount }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">最近锚定</span>
              <span class="stat-value">{{ summary.batchChains.lastAnchorDate || '未锚定' }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <el-skeleton v-else-if="!error" :rows="6" animated class="monitor-skeleton" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Refresh, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import { getBlockchainMonitorSummary } from '../services/api'

const summary = ref(null)
const loading = ref(false)
const error = ref(false)
const autoRefresh = ref(true)
let refreshTimer = null

async function refreshData() {
  loading.value = true
  error.value = false
  try {
    summary.value = await getBlockchainMonitorSummary()
  } catch (e) {
    console.error('Failed to fetch blockchain monitor summary:', e)
    error.value = true
  } finally {
    loading.value = false
  }
}

function toggleAutoRefresh() {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

function startAutoRefresh() {
  stopAutoRefresh()
  refreshTimer = setInterval(refreshData, 30000)
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

function formatTime(isoStr) {
  if (!isoStr) return '-'
  try {
    const d = new Date(isoStr)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  } catch {
    return isoStr
  }
}

onMounted(() => {
  refreshData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.blockchain-monitor {
  max-width: 720px;
  margin: 0 auto;
  padding: 1rem 0;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.monitor-header h2 {
  font-size: 1.35rem;
  color: var(--color-primary-dark);
  margin: 0;
}

.monitor-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.error-banner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius);
  color: #b91c1c;
  margin-bottom: 1rem;
}

.health-banner {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  border-radius: var(--radius-lg);
  margin-bottom: 1.5rem;
}

.health-banner.healthy {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
  border: 1px solid #a7f3d0;
  color: #065f46;
}

.health-banner.unhealthy {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  border: 1px solid #fecaca;
  color: #991b1b;
}

.health-text {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.health-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.health-subtitle {
  font-size: 0.85rem;
  opacity: 0.75;
}

.status-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

@media (max-width: 600px) {
  .status-cards {
    grid-template-columns: 1fr;
  }

  .monitor-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

.status-card {
  border: 1px solid #e5e7eb;
  border-radius: var(--radius-lg);
}

.status-card :deep(.el-card__header) {
  padding: 0.875rem 1.25rem;
  border-bottom: 1px solid #f3f4f6;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 0.95rem;
  color: var(--color-text);
}

.card-body {
  padding: 0.25rem 0;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.625rem 1.25rem;
  font-size: 0.9rem;
}

.stat-label {
  color: var(--color-text-muted);
}

.stat-value {
  font-weight: 600;
  color: var(--color-text);
  font-variant-numeric: tabular-nums;
}

.healthy-count {
  color: var(--color-success, #10b981);
}

.broken-count {
  color: var(--color-danger, #ef4444);
}

.monitor-skeleton {
  padding: 1rem 0;
}
</style>
