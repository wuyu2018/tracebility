<template>
  <div v-if="traceData" class="result-display">
    <div class="result-header">
      <span class="success-badge" v-if="traceData.status === '已激活'">✓ 正品验证通过</span>
      <span class="info-badge" v-else-if="traceData.status === '未激活'">○ 待激活</span>
      <span class="info-badge" v-else>{{ traceData.status }}</span>
      <h2>{{ traceData.product?.name }}</h2>
      <p class="specification">{{ traceData.product?.specification }}</p>
    </div>

    <div class="result-cards">
      <section class="card">
        <h3>基本信息</h3>
        <dl>
          <dt>批次号</dt><dd>{{ traceData.batch?.batchNumber || '-' }}</dd>
          <dt>生产日期</dt><dd>{{ formatDate(traceData.batch?.productionDate) }}</dd>
          <dt>保质期</dt><dd>{{ traceData.batch?.shelfLife || traceData.product?.shelfLife || '-' }}</dd>
        </dl>
      </section>

      <section v-if="traceData.materials?.length" class="card">
        <h3>原料信息</h3>
        <ul class="list">
          <li v-for="(m, i) in traceData.materials" :key="i">
            <strong>{{ m.materialName }}</strong>
            <span>批次：{{ m.batchNumber }}</span>
            <span>供应商：{{ m.supplierName || '-' }}</span>
            <span>生产商：{{ m.producerName || '-' }}</span>
          </li>
        </ul>
      </section>

      <section v-if="traceData.inspection" class="card">
        <h3>检测报告</h3>
        <dl>
          <dt>样品名称</dt><dd>{{ traceData.inspection.sampleName || '-' }}</dd>
          <dt>抽样数量</dt><dd>{{ traceData.inspection.sampleQuantity || '-' }}</dd>
          <dt>规格</dt><dd>{{ traceData.inspection.sampleSpecification || '-' }}</dd>
        </dl>
        <div v-if="traceData.inspection.imageUrl" class="inspection-image">
          <img :src="traceData.inspection.imageUrl" alt="检测报告" />
        </div>
      </section>

      <section v-if="traceData.storage" class="card">
        <h3>仓储信息</h3>
        <dl>
          <dt>入库时间</dt><dd>{{ formatDateTime(traceData.storage.storageTime) }}</dd>
          <dt>出库时间</dt><dd>{{ formatDateTime(traceData.storage.outboundTime) }}</dd>
        </dl>
      </section>

      <section v-if="traceData.transportSale" class="card">
        <h3>运输销售</h3>
        <dl>
          <dt>运输时间</dt><dd>{{ formatDateTime(traceData.transportSale.transportTime) }}</dd>
          <dt>销售区域</dt><dd>{{ traceData.transportSale.salesRegion || '-' }}</dd>
        </dl>
      </section>

      <section v-if="traceData.firstScanTime" class="card card-highlight">
        <h3>首次扫码</h3>
        <dl>
          <dt>首次扫码时间</dt><dd class="highlight">{{ formatDateTime(traceData.firstScanTime) }}</dd>
        </dl>
      </section>
    </div>
  </div>
</template>

<script setup>
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

.info-badge {
  display: inline-block;
  background: linear-gradient(135deg, #4a90a4 0%, #357a8a 100%);
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
  margin: 0;
}

.specification {
  color: var(--color-text-muted);
  font-size: 0.95rem;
  margin: 0.5rem 0 0;
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

.card-highlight {
  background: linear-gradient(145deg, #f0fff4 0%, #e6ffe9 100%);
  border-color: #a7f3d0;
}

.card-highlight .highlight {
  color: var(--color-success);
  font-weight: 600;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.list li {
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.list li:last-child {
  border-bottom: none;
}

.list li strong {
  color: var(--color-primary-dark);
}

.list li span {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.inspection-image {
  margin-top: 1rem;
  text-align: center;
}

.inspection-image img {
  max-width: 100%;
  border-radius: var(--radius);
  border: 1px solid #eee;
}

@media (max-width: 480px) {
  .card dl {
    grid-template-columns: 1fr;
    gap: 0.35rem;
  }
  
  .card {
    padding: 1.25rem 1rem;
  }
}
</style>