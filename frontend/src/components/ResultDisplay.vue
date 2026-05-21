<template>
  <div class="result-display" v-if="traceData">
    <div class="status-card" :class="statusClass">
      <div class="status-header">
        <span class="status-text">{{ statusText }}</span>
        <span class="status-tip" v-if="traceData.repeatedQuery">
          该产品已被查询 {{ traceData.scanCount }} 次，首次查询：{{ formatTime(traceData.firstScanTime) }}，请谨慎购买！
        </span>
      </div>
    </div>

    <div class="info-card">
      <h3>产品信息</h3>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">产品名称</span>
          <span class="value">{{ traceData.product?.name || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">规格</span>
          <span class="value">{{ traceData.product?.specification || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">保质期</span>
          <span class="value">{{ traceData.product?.shelfLife || '-' }}</span>
        </div>
        <div class="info-item" v-if="traceData.batch?.batchNumber">
          <span class="label">批次号</span>
          <span class="value">{{ traceData.batch.batchNumber }}</span>
        </div>
      </div>
    </div>

    <div class="trace-card">
      <h3>溯源信息</h3>
      <div class="trace-timeline">
        <div class="trace-item" v-if="traceData.materials?.length">
          <div class="trace-dot"></div>
          <div class="trace-content">
            <div class="trace-title">原料采购</div>
            <div class="trace-desc">
              <span v-for="(m, i) in traceData.materials" :key="i">
                {{ m.materialName }} ({{ m.batchNumber }})
                <span v-if="i < traceData.materials.length - 1">、</span>
              </span>
            </div>
          </div>
        </div>
        <div class="trace-item" v-if="traceData.inspection">
          <div class="trace-dot"></div>
          <div class="trace-content">
            <div class="trace-title">质检完成</div>
            <div class="trace-desc">
              样品：{{ traceData.inspection.sampleName }}
              {{ traceData.inspection.qualified ? '✓ 合格' : '✗ 不合格' }}
            </div>
          </div>
        </div>
        <div class="trace-item" v-if="traceData.storage">
          <div class="trace-dot"></div>
          <div class="trace-content">
            <div class="trace-title">仓储</div>
            <div class="trace-desc">
              {{ traceData.storage.warehouseLocation }}
              <br>
              入库：{{ formatTime(traceData.storage.storageTime) }}
              出库：{{ formatTime(traceData.storage.outboundTime) }}
            </div>
          </div>
        </div>
        <div class="trace-item" v-if="traceData.transportSale">
          <div class="trace-dot"></div>
          <div class="trace-content">
            <div class="trace-title">运输销售</div>
            <div class="trace-desc">
              区域：{{ traceData.transportSale.salesRegion }}
              <br>
              物流：{{ traceData.transportSale.transportCompany || '-' }}
              {{ traceData.transportSale.vehicleNumber || '' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <button class="btn-complaint" @click="$emit('complaint')">
      我要投诉
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  traceData: Object
})

defineEmits(['complaint'])

const statusClass = computed(() => {
  if (props.traceData?.repeatedQuery) return 'status-warning'
  return 'status-success'
})

const statusText = computed(() => {
  if (props.traceData?.repeatedQuery) return '⚠️ 重复查询告警'
  return '✓ 验证成功 - 正品'
})

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.result-display {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.status-card {
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  text-align: center;
}

.status-success {
  background: #d1fae5;
  border: 2px solid #10b981;
  color: #065f46;
}

.status-warning {
  background: #fef3c7;
  border: 2px solid #f59e0b;
  color: #92400e;
}

.status-header {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.status-text {
  font-size: 1.1rem;
  font-weight: 600;
}

.status-tip {
  font-size: 0.9rem;
  opacity: 0.9;
}

.info-card, .trace-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-md);
}

.info-card h3, .trace-card h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: var(--spacing-md);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-item .label {
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.info-item .value {
  font-size: 0.95rem;
  font-weight: 500;
}

.trace-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.trace-item {
  display: flex;
  gap: var(--spacing-sm);
  position: relative;
}

.trace-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 30px;
  bottom: -20px;
  width: 2px;
  background: var(--color-border);
}

.trace-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-primary);
  flex-shrink: 0;
  margin-top: 2px;
  border: 3px solid white;
  box-shadow: 0 0 0 2px var(--color-primary);
}

.trace-content {
  flex: 1;
}

.trace-title {
  font-weight: 600;
  font-size: 0.95rem;
  margin-bottom: 0.25rem;
}

.trace-desc {
  font-size: 0.85rem;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.btn-complaint {
  padding: 1rem;
  background: white;
  color: var(--color-danger);
  font-weight: 600;
  font-size: 1rem;
  border: 2px solid var(--color-danger);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s;
}

.btn-complaint:hover {
  background: var(--color-danger);
  color: white;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .status-card {
    padding: var(--spacing-md);
  }
  
  .info-card, .trace-card {
    padding: var(--spacing-md);
  }
}
</style>
