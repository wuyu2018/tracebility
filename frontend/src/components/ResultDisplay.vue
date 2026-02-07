<template>
  <div v-if="traceData" class="result-display">
    <div class="result-header">
      <span class="success-badge">✓ 正品验证通过</span>
      <h2>{{ traceData.product?.name }}</h2>
    </div>

    <div class="result-cards">
      <section class="card">
        <h3>产品信息</h3>
        <dl>
          <dt>规格</dt><dd>{{ traceData.product?.specification || '-' }}</dd>
          <dt>批次号</dt><dd>{{ traceData.product?.batchNumber || '-' }}</dd>
          <dt>生产日期</dt><dd>{{ formatDate(traceData.product?.productionDate) }}</dd>
          <dt>保质期</dt><dd>{{ traceData.product?.shelfLife || '-' }}</dd>
        </dl>
      </section>

      <section v-if="traceData.materialPurchases?.length" class="card">
        <h3>原料采购</h3>
        <ul class="list">
          <li v-for="(mp, i) in traceData.materialPurchases" :key="i">
            <strong>{{ mp.materialName }}</strong>
            <span>{{ mp.producerName }} · {{ mp.producerAddress }}</span>
          </li>
        </ul>
      </section>

      <section v-if="traceData.storages?.length" class="card">
        <h3>贮存记录</h3>
        <ul class="list">
          <li v-for="(s, i) in traceData.storages" :key="i">
            入库 {{ formatDateTime(s.storageTime) }} → 出库 {{ formatDateTime(s.outboundTime) }}
            <span>{{ s.quantity }} {{ s.unit }}</span>
          </li>
        </ul>
      </section>

      <section v-if="traceData.inspections?.length" class="card">
        <h3>出厂检验</h3>
        <ul class="list">
          <li v-for="(ins, i) in traceData.inspections" :key="i">
            {{ ins.sampleName }} · {{ ins.sampleQuantity }}件 · {{ ins.sampleSpecification }}
          </li>
        </ul>
      </section>

      <section v-if="traceData.transportSales?.length" class="card">
        <h3>储运销售</h3>
        <ul class="list">
          <li v-for="(ts, i) in traceData.transportSales" :key="i">
            环境 {{ ts.environmentTemperature }}°C / 产品 {{ ts.productTemperature }}°C
            <span>{{ formatDateTime(ts.time) }}</span>
          </li>
        </ul>
      </section>

      <section v-if="traceData.complaints?.length" class="card card-warning">
        <h3>投诉记录</h3>
        <ul class="list">
          <li v-for="(c, i) in traceData.complaints" :key="i">
            {{ c.complaintReason }}
            <span>{{ formatDateTime(c.complaintTime) }}</span>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  traceData: Object,
})

function formatDate(val) {
  if (!val) return '-'
  const d = new Date(val)
  return isNaN(d.getTime()) ? val : d.toLocaleDateString('zh-CN')
}

function formatDateTime(val) {
  if (!val) return '-'
  const d = new Date(val)
  return isNaN(d.getTime()) ? val : d.toLocaleString('zh-CN')
}
</script>

<style scoped>
.result-display {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.result-header {
  text-align: center;
  margin-bottom: 2rem;
}

.success-badge {
  display: inline-block;
  background: linear-gradient(135deg, var(--color-success) 0%, #5a9c6a 100%);
  color: white;
  padding: 0.35rem 1rem;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 0.75rem;
}

.result-header h2 {
  font-size: 1.5rem;
  color: var(--color-primary-dark);
}

@media (max-width: 480px) {
  .result-header {
    margin-bottom: 1.5rem;
  }
  .result-header h2 {
    font-size: 1.25rem;
    word-break: break-word;
  }
}

.result-cards {
  display: grid;
  gap: 1.25rem;
}

.card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(45, 90, 61, 0.08);
}

@media (max-width: 480px) {
  .card {
    padding: 1.25rem 1rem;
  }
  .card dl {
    grid-template-columns: 1fr;
    gap: 0.35rem;
  }
  .card dl dt {
    font-size: 0.85rem;
  }
}

.card h3 {
  font-size: 1rem;
  color: var(--color-primary);
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--color-accent-light);
}

.card dl {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.5rem 1.5rem;
}

.card dt {
  color: var(--color-text-muted);
  font-weight: 500;
}

.card dd {
  margin: 0;
}

.list {
  list-style: none;
}

.list li {
  padding: 0.5rem 0;
  border-bottom: 1px solid #eee;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.list li:last-child {
  border-bottom: none;
}

.list li span {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  word-break: break-word;
}

.card-warning {
  border-left: 4px solid var(--color-accent);
}
</style>
