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
            {{ summary.brokenBlocks ? summary.brokenBlocks.length + ' 个区块异常 · ' : '' }}最后更新: {{ formatTime(summary.lastUpdated) }}
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

      <!-- 异常详情 -->
      <div v-if="summary.brokenBlocks && summary.brokenBlocks.length > 0" class="broken-details">
        <h3 class="section-title">
          <el-icon><WarningFilled /></el-icon>
          异常详情
          <el-tag type="danger" size="small" effect="dark">{{ summary.brokenBlocks.length }} 个异常区块</el-tag>
        </h3>

        <!-- 原材料链异常 -->
        <div v-if="materialBroken.length > 0" class="broken-group">
          <h4 class="group-title">
            原材料链
            <el-tag type="danger" size="small">{{ materialBroken.length }} 个异常</el-tag>
          </h4>
          <el-table :data="materialBroken" stripe size="small" class="broken-table" max-height="300">
            <el-table-column label="区块ID" prop="blockId" width="90" />
            <el-table-column label="操作" prop="action" width="130" />
            <el-table-column label="关联单据" min-width="180">
              <template #default="{ row }">
                {{ entityTypeLabel(row.entityType) }}
                <span v-if="row.entityId"> #{{ row.entityId }}</span>
              </template>
            </el-table-column>
            <el-table-column label="错误原因" min-width="260">
              <template #default="{ row }">
                <div v-for="(err, i) in row.errors" :key="i" class="error-item">
                  {{ errorLabel(err) }}
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 批次链异常 -->
        <div v-if="batchBrokenGroups.length > 0" class="broken-group">
          <h4 class="group-title">
            批次链
            <el-tag type="danger" size="small">{{ brokenBatchCount }} 个异常批次</el-tag>
          </h4>
          <el-collapse v-model="activeBatchIds" class="batch-collapse">
            <el-collapse-item v-for="group in batchBrokenGroups" :key="group.batchId" :title="'批次 #' + group.batchId + '（' + group.blocks.length + ' 个异常区块）'" :name="group.batchId">
              <el-table :data="group.blocks" stripe size="small" class="broken-table">
                <el-table-column label="区块ID" prop="blockId" width="90" />
                <el-table-column label="操作" prop="action" width="130" />
                <el-table-column label="关联单据" min-width="180">
                  <template #default="{ row }">
                    {{ entityTypeLabel(row.entityType) }}
                    <span v-if="row.entityId"> #{{ row.entityId }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="错误原因" min-width="260">
                  <template #default="{ row }">
                    <div v-for="(err, i) in row.errors" :key="i" class="error-item">
                      {{ errorLabel(err) }}
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </div>

    <el-skeleton v-else-if="!error" :rows="6" animated class="monitor-skeleton" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Refresh, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import { getBlockchainMonitorSummary } from '../services/api'
import { ElMessage } from 'element-plus'

const summary = ref(null)
const loading = ref(false)
const error = ref(false)
const autoRefresh = ref(true)
let refreshTimer = null

// 展开所有有异常的批次
const activeBatchIds = ref([])

// 原材料链异常区块
const materialBroken = computed(() =>
  (summary.value?.brokenBlocks || []).filter(b => b.batchId == null)
)

// 批次链按 batchId 分组
const batchBrokenGroups = computed(() => {
  const map = {}
  for (const b of (summary.value?.brokenBlocks || [])) {
    if (b.batchId == null) continue
    if (!map[b.batchId]) map[b.batchId] = { batchId: b.batchId, blocks: [] }
    map[b.batchId].blocks.push(b)
  }
  return Object.values(map)
})

const brokenBatchCount = computed(() => batchBrokenGroups.value.length)

// 实体类型 → 中文标签
function entityTypeLabel(type) {
  const map = {
    Material: '原材料',
    Product: '产品',
    Storage: '仓储',
    Inspection: '检验',
    TransportSale: '运输销售',
    MaterialPurchase: '原料采购',
    ProductionBatch: '生产批次',
    Complaint: '投诉',
    SecurityCode: '防伪码',
    MaterialVariety: '原料品种',
    ProductMaterialRelation: '物料配方',
  }
  return map[type] || type
}

// 错误消息简化
function errorLabel(err) {
  if (err.includes('previous_hash mismatch')) return '❌ 链断裂：前序哈希不匹配（数据链路被篡改）'
  if (err.includes('current_hash mismatch')) return '❌ 数据被篡改：当前哈希与计算值不一致'
  if (err.includes('signature verification failed')) return '❌ 签名验证失败：区块签名无效（私钥不匹配或数据被篡改）'
  return '⚠️ ' + err
}

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

.broken-details {
  margin-top: 1.5rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.05rem;
  color: #991b1b;
  margin: 0 0 1rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #fecaca;
}

.broken-group {
  margin-bottom: 1.25rem;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  color: var(--color-text);
  margin: 0 0 0.5rem 0;
}

.broken-table {
  border: 1px solid #fecaca;
  border-radius: var(--radius);
}

.broken-table :deep(th.el-table__cell) {
  background-color: #fef2f2 !important;
  color: #991b1b;
  font-weight: 600;
}

.error-item {
  font-size: 0.85rem;
  color: #b91c1c;
  padding: 0.15rem 0;
  line-height: 1.4;
}

.batch-collapse {
  border: 1px solid #fecaca;
  border-radius: var(--radius);
}

.batch-collapse :deep(.el-collapse-item__header) {
  padding-left: 1rem;
  font-weight: 600;
  font-size: 0.9rem;
  background: #fef2f2;
}

.batch-collapse :deep(.el-collapse-item__content) {
  padding: 0.5rem;
}

.batch-collapse :deep(.el-collapse-item__wrap) {
  border-bottom: none;
}

.monitor-skeleton {
  padding: 1rem 0;
}
</style>
