<template>
  <div class="product-detail">
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>管理员数据总览</span>
          <el-button text @click="loadAllData">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </template>
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-value">{{ productList.length }}</span>
          <span class="stat-label">产品</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ materialList.length }}</span>
          <span class="stat-label">原材料</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ batchList.length }}</span>
          <span class="stat-label">批次</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ inspectionList.length }}</span>
          <span class="stat-label">检验检测</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ storageList.length }}</span>
          <span class="stat-label">仓储</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ transportList.length }}</span>
          <span class="stat-label">运输销售</span>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
      <el-tab-pane label="产品列表" name="products">
        <div class="tab-content">
          <h3>所有产品 ({{ productList.length }})</h3>
          <el-table :data="productList" stripe v-loading="loading" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="产品名称" />
            <el-table-column prop="specification" label="规格" />
            <el-table-column prop="antiFakeCode" label="防伪码">
              <template #default="{ row }">
                <el-tag v-if="row.antiFakeCode" type="warning" size="small">{{ row.antiFakeCode }}</el-tag>
                <el-tag v-else type="info" size="small">无</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="qrCodeUrl" label="二维码">
              <template #default="{ row }">
                <el-tag v-if="row.qrCodeUrl" type="success" size="small">有</el-tag>
                <el-tag v-else type="info" size="small">无</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="shelfLife" label="保质期" />
            <el-table-column prop="contactPhone" label="联系电话" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="原材料采购" name="materials">
        <div class="tab-content">
          <h3>所有原材料采购记录 ({{ materialList.length }})</h3>
          <el-table :data="materialList" stripe v-loading="loading" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="productName" label="产品名称" />
            <el-table-column prop="materialName" label="原料名称" />
            <el-table-column prop="batchNumber" label="采购批次号" />
            <el-table-column prop="supplierName" label="供应商" />
            <el-table-column prop="producerName" label="生产商" />
            <el-table-column prop="purchaseDate" label="采购时间" :formatter="formatDateTime" width="160" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="unit" label="单位" width="60" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="生产批次" name="batches">
        <div class="tab-content">
          <h3>所有生产批次 ({{ batchList.length }})</h3>
          <el-table :data="batchList" stripe v-loading="loading" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="batchNumber" label="批次号" />
            <el-table-column prop="productName" label="产品名称" />
            <el-table-column prop="productionDate" label="生产日期" />
            <el-table-column prop="shelfLife" label="保质期" />
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="unit" label="单位" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="检验检测" name="inspections">
        <div class="tab-content">
          <h3>所有检验检测记录 ({{ inspectionList.length }})</h3>
          <el-table :data="inspectionList" stripe v-loading="loading" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="batchId" label="批次ID" width="80" />
            <el-table-column prop="sampleName" label="样品名称" />
            <el-table-column prop="sampleQuantity" label="样品数量" width="100" />
            <el-table-column prop="sampleSpecification" label="样品规格" />
            <el-table-column prop="imageUrl" label="检测报告图片" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="仓储" name="storages">
        <div class="tab-content">
          <h3>所有仓储记录 ({{ storageList.length }})</h3>
          <el-table :data="storageList" stripe v-loading="loading" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="batchId" label="批次ID" width="80" />
            <el-table-column prop="storageTime" label="入库时间" :formatter="formatDateTime" width="160" />
            <el-table-column prop="outboundTime" label="出库时间" :formatter="formatDateTime" width="160" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="unit" label="单位" width="60" />
            <el-table-column prop="warehouseLocation" label="仓库地址" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="运输销售" name="transport">
        <div class="tab-content">
          <h3>所有运输销售记录 ({{ transportList.length }})</h3>
          <el-table :data="transportList" stripe v-loading="loading" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="batchId" label="批次ID" width="80" />
            <el-table-column prop="transportCompany" label="运输公司" />
            <el-table-column prop="vehicleNumber" label="车牌号" />
            <el-table-column prop="salesRegion" label="销售区域" />
            <el-table-column prop="receiverName" label="收货人" />
            <el-table-column prop="receiverContact" label="联系方式" />
            <el-table-column prop="time" label="记录时间" :formatter="formatDateTime" width="160" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const API_BASE = '/api'

const activeTab = ref('products')
const loading = ref(false)

const productList = ref([])
const materialList = ref([])
const batchList = ref([])
const inspectionList = ref([])
const storageList = ref([])
const transportList = ref([])

function formatDate(row, column, cellValue) {
  if (!cellValue) return '-'
  if (typeof cellValue === 'string' && cellValue.includes('T')) {
    return cellValue.split('T')[0]
  }
  return cellValue
}

function formatDateTime(row, column, cellValue) {
  if (!cellValue) return '-'
  if (typeof cellValue === 'string' && cellValue.includes('T')) {
    return cellValue.replace('T', ' ').substring(0, 16)
  }
  return cellValue
}

async function loadAllData() {
  loading.value = true
  try {
    const [productsRes, materialsRes, batchesRes, inspectionsRes, storagesRes, transportsRes] = await Promise.all([
      axios.get(`${API_BASE}/products`),
      axios.get(`${API_BASE}/materials`),
      axios.get(`${API_BASE}/batches`),
      axios.get(`${API_BASE}/insert/inspections`),
      axios.get(`${API_BASE}/insert/storages`),
      axios.get(`${API_BASE}/insert/transport-sales`)
    ])
    
    productList.value = productsRes.data
    materialList.value = materialsRes.data
    batchList.value = batchesRes.data
    inspectionList.value = inspectionsRes.data
    storageList.value = storagesRes.data
    transportList.value = transportsRes.data
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.product-detail {
  padding: 1rem 0;
}

.info-card {
  margin-bottom: 1rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-row {
  display: flex;
  gap: 1.5rem;
  padding: 0.5rem 0;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-primary);
}

.stat-label {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.main-tabs {
  margin-top: 1rem;
}

.tab-content {
  padding: 1rem 0;
}

.tab-content h3 {
  margin: 0 0 1rem;
  font-size: 1rem;
  color: var(--color-text);
}
</style>
