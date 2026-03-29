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

      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="输入防伪码或批次号快速定位"
          clearable
          style="max-width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-button v-if="searchKeyword" text @click="clearSearch">清除搜索</el-button>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
      <el-tab-pane label="产品列表" name="products">
        <div class="tab-content">
          <h3>所有产品 ({{ searchResult ? 1 : productList.length }})</h3>
          <el-table :data="searchResult ? [searchResult.product] : productList" stripe v-loading="loading" max-height="500">
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
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewProductTrace(row)">溯源</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="原材料采购" name="materials">
        <div class="tab-content">
          <h3>所有原材料采购记录 ({{ filteredMaterials.length }})</h3>
          <el-table :data="filteredMaterials" stripe v-loading="loading" max-height="500">
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
          <h3>所有生产批次 ({{ filteredBatches.length }})</h3>
          <el-table :data="filteredBatches" stripe v-loading="loading" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="batchNumber" label="批次号" />
            <el-table-column prop="productName" label="产品名称" />
            <el-table-column prop="productionDate" label="生产日期" />
            <el-table-column prop="shelfLife" label="保质期" />
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="unit" label="单位" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewBatchDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="检验检测" name="inspections">
        <div class="tab-content">
          <h3>所有检验检测记录 ({{ filteredInspections.length }})</h3>
          <el-table :data="filteredInspections" stripe v-loading="loading" max-height="500">
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
          <h3>所有仓储记录 ({{ filteredStorages.length }})</h3>
          <el-table :data="filteredStorages" stripe v-loading="loading" max-height="500">
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
          <h3>所有运输销售记录 ({{ filteredTransports.length }})</h3>
          <el-table :data="filteredTransports" stripe v-loading="loading" max-height="500">
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

    <el-dialog v-model="traceDialogVisible" title="产品完整溯源链路" width="800px" destroy-on-close>
      <div v-if="currentTrace" class="trace-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品名称">{{ currentTrace.product?.name }}</el-descriptions-item>
          <el-descriptions-item label="产品规格">{{ currentTrace.product?.specification }}</el-descriptions-item>
          <el-descriptions-item label="防伪码">{{ currentTrace.product?.antiFakeCode || '无' }}</el-descriptions-item>
          <el-descriptions-item label="保质期">{{ currentTrace.product?.shelfLife }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">生产批次</h4>
        <el-descriptions :column="2" border v-if="currentTrace.batch">
          <el-descriptions-item label="批次号">{{ currentTrace.batch?.batchNumber }}</el-descriptions-item>
          <el-descriptions-item label="生产日期">{{ currentTrace.batch?.productionDate }}</el-descriptions-item>
          <el-descriptions-item label="保质期">{{ currentTrace.batch?.shelfLife }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTrace.batch?.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">原料信息</h4>
        <el-table :data="currentTrace.materials" stripe size="small" v-if="currentTrace.materials?.length">
          <el-table-column prop="materialName" label="原料名称" />
          <el-table-column prop="batchNumber" label="批次号" />
          <el-table-column prop="supplierName" label="供应商" />
          <el-table-column prop="producerName" label="生产商" />
        </el-table>
        <p v-else class="no-data">无原料记录</p>

        <h4 class="section-title">检验检测</h4>
        <el-descriptions :column="2" border v-if="currentTrace.inspection">
          <el-descriptions-item label="样品名称">{{ currentTrace.inspection?.sampleName }}</el-descriptions-item>
          <el-descriptions-item label="抽样数量">{{ currentTrace.inspection?.sampleQuantity }}</el-descriptions-item>
          <el-descriptions-item label="样品规格">{{ currentTrace.inspection?.sampleSpecification }}</el-descriptions-item>
          <el-descriptions-item label="检测报告">{{ currentTrace.inspection?.imageUrl ? '有' : '无' }}</el-descriptions-item>
        </el-descriptions>
        <p v-else class="no-data">无检验记录</p>

        <h4 class="section-title">仓储信息</h4>
        <el-descriptions :column="2" border v-if="currentTrace.storage">
          <el-descriptions-item label="入库时间">{{ currentTrace.storage?.storageTime }}</el-descriptions-item>
          <el-descriptions-item label="出库时间">{{ currentTrace.storage?.outboundTime }}</el-descriptions-item>
          <el-descriptions-item label="仓库地址" :span="2">{{ currentTrace.storage?.warehouseLocation }}</el-descriptions-item>
        </el-descriptions>
        <p v-else class="no-data">无仓储记录</p>

        <h4 class="section-title">运输销售 <span class="sensitive-hint">(敏感信息)</span></h4>
        <el-descriptions :column="2" border v-if="currentTrace.transportSale">
          <el-descriptions-item label="运输时间">{{ currentTrace.transportSale?.transportTime }}</el-descriptions-item>
          <el-descriptions-item label="运输公司">{{ currentTrace.transportSale?.transportCompany }}</el-descriptions-item>
          <el-descriptions-item label="车牌号">{{ currentTrace.transportSale?.vehicleNumber }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentTrace.transportSale?.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="收货人联系方式">{{ currentTrace.transportSale?.receiverContact }}</el-descriptions-item>
          <el-descriptions-item label="销售区域">{{ currentTrace.transportSale?.salesRegion }}</el-descriptions-item>
        </el-descriptions>
        <p v-else class="no-data">无运输记录</p>

        <h4 class="section-title">查询记录</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="状态">{{ currentTrace.status }}</el-descriptions-item>
          <el-descriptions-item label="查询次数">{{ currentTrace.scanCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="首次查询时间">{{ currentTrace.firstScanTime || '未查询' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" title="批次详情" width="700px" destroy-on-close>
      <div v-if="currentBatch" class="batch-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次号">{{ currentBatch.batchNumber }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ currentBatch.productName }}</el-descriptions-item>
          <el-descriptions-item label="生产日期">{{ currentBatch.productionDate }}</el-descriptions-item>
          <el-descriptions-item label="保质期">{{ currentBatch.shelfLife }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ currentBatch.quantity }} {{ currentBatch.unit }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">关联的原材料</h4>
        <el-table :data="getMaterialsByBatchNumber(currentBatch.batchNumber)" stripe size="small">
          <el-table-column prop="materialName" label="原料名称" />
          <el-table-column prop="batchNumber" label="采购批次号" />
          <el-table-column prop="supplierName" label="供应商" />
          <el-table-column prop="producerName" label="生产商" />
        </el-table>

        <h4 class="section-title">关联的仓储</h4>
        <el-table :data="getStoragesByBatchId(currentBatch.id)" stripe size="small">
          <el-table-column prop="storageTime" label="入库时间" :formatter="formatDateTime" />
          <el-table-column prop="outboundTime" label="出库时间" :formatter="formatDateTime" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column prop="warehouseLocation" label="仓库地址" />
        </el-table>

        <h4 class="section-title">关联的运输销售 <span class="sensitive-hint">(敏感信息)</span></h4>
        <el-table :data="getTransportsByBatchId(currentBatch.id)" stripe size="small">
          <el-table-column prop="transportCompany" label="运输公司" />
          <el-table-column prop="vehicleNumber" label="车牌号" />
          <el-table-column prop="salesRegion" label="销售区域" />
          <el-table-column prop="receiverName" label="收货人" />
          <el-table-column prop="receiverContact" label="联系方式" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const API_BASE = '/api'

const activeTab = ref('products')
const loading = ref(false)
const searchKeyword = ref('')
const searchResult = ref(null)

const productList = ref([])
const materialList = ref([])
const batchList = ref([])
const inspectionList = ref([])
const storageList = ref([])
const transportList = ref([])

const traceDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const currentTrace = ref(null)
const currentBatch = ref(null)

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

async function handleSearch() {
  if (!searchKeyword.value.trim()) return
  
  searchResult.value = null
  loading.value = true
  
  try {
    const res = await axios.get(`${API_BASE}/product-detail?antiFakeCode=${encodeURIComponent(searchKeyword.value.trim())}`)
    if (res.data && !res.data.error) {
      searchResult.value = res.data
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

function clearSearch() {
  searchKeyword.value = ''
  searchResult.value = null
}

async function viewProductTrace(product) {
  if (!product.antiFakeCode) {
    alert('该产品暂无防伪码，无法查看溯源')
    return
  }
  
  loading.value = true
  traceDialogVisible.value = true
  
  try {
    const res = await axios.get(`${API_BASE}/product-detail?antiFakeCode=${encodeURIComponent(product.antiFakeCode)}`)
    if (res.data && !res.data.error) {
      currentTrace.value = res.data
    } else {
      alert('未找到该产品的溯源信息')
      traceDialogVisible.value = false
    }
  } catch (error) {
    console.error('加载溯源信息失败:', error)
    traceDialogVisible.value = false
  } finally {
    loading.value = false
  }
}

function viewBatchDetail(batch) {
  currentBatch.value = batch
  batchDialogVisible.value = true
}

const filteredMaterials = computed(() => {
  if (!searchKeyword.value) return materialList.value
  const kw = searchKeyword.value.toLowerCase()
  return materialList.value.filter(m => 
    m.batchNumber?.toLowerCase().includes(kw) ||
    m.materialName?.toLowerCase().includes(kw) ||
    m.supplierName?.toLowerCase().includes(kw)
  )
})

const filteredBatches = computed(() => {
  if (!searchKeyword.value) return batchList.value
  const kw = searchKeyword.value.toLowerCase()
  return batchList.value.filter(b => 
    b.batchNumber?.toLowerCase().includes(kw) ||
    b.productName?.toLowerCase().includes(kw)
  )
})

const filteredInspections = computed(() => {
  if (!searchKeyword.value) return inspectionList.value
  return inspectionList.value
})

const filteredStorages = computed(() => {
  if (!searchKeyword.value) return storageList.value
  return storageList.value
})

const filteredTransports = computed(() => {
  if (!searchKeyword.value) return transportList.value
  return transportList.value
})

function getMaterialsByBatchNumber(batchNumber) {
  return materialList.value.filter(m => m.batchNumber === batchNumber)
}

function getStoragesByBatchId(batchId) {
  return storageList.value.filter(s => s.batchId === batchId)
}

function getTransportsByBatchId(batchId) {
  return transportList.value.filter(t => t.batchId === batchId)
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

.search-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.section-title {
  margin: 1.5rem 0 0.75rem;
  padding: 0.5rem 0;
  font-size: 1rem;
  color: var(--color-primary);
  border-bottom: 2px solid var(--color-accent-light);
}

.sensitive-hint {
  font-size: 0.75rem;
  color: #e6a23c;
  font-weight: normal;
}

.no-data {
  color: var(--color-text-muted);
  font-size: 0.875rem;
  padding: 0.5rem 0;
}

.trace-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.batch-detail {
  max-height: 60vh;
  overflow-y: auto;
}
</style>
