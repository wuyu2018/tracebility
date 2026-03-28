<template>
  <div class="product-detail">
    <!-- 产品基本信息 -->
    <el-card class="product-info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="product-name">{{ product.name }}</span>
          <el-tag type="info">{{ product.antiFakeCode }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="产品名称">{{ product.name }}</el-descriptions-item>
        <el-descriptions-item label="防伪码">
          <el-tag type="warning">{{ product.antiFakeCode }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格">{{ product.specification || '-' }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ product.batchNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产日期">{{ product.productionDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保质期">{{ product.shelfLife || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ product.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ product.contactEmail || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 业务数据 Tab -->
    <el-tabs v-model="activeBusinessTab" type="border-card" class="business-tabs">
      <!-- 原材料采购 -->
      <el-tab-pane label="原材料采购" name="material">
        <div class="business-section">
          <div class="section-header">
            <h3>原材料采购记录</h3>
          </div>
          
          <!-- 记录列表 -->
          <el-table :data="materialList" stripe class="business-table" v-loading="loading">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="materialName" label="原料名称" />
            <el-table-column prop="purchaseDate" label="采购时间" :formatter="formatDateTime" />
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="unit" label="单位" />
            <el-table-column prop="producerName" label="生产商" />
          </el-table>
          <el-empty v-if="!loading && materialList.length === 0" description="暂无采购记录" />
        </div>
      </el-tab-pane>

      <!-- 检验检测 -->
      <el-tab-pane label="检验检测" name="inspection">
        <div class="business-section">
          <div class="section-header">
            <h3>检验检测记录</h3>
          </div>
          
          <!-- 记录列表 -->
          <el-table :data="inspectionList" stripe class="business-table" v-loading="loading">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="sampleName" label="样品名称" />
            <el-table-column prop="sampleQuantity" label="样品数量" />
            <el-table-column prop="sampleSpecification" label="样品规格" />
          </el-table>
          <el-empty v-if="!loading && inspectionList.length === 0" description="暂无检验记录" />
        </div>
      </el-tab-pane>

      <!-- 仓储 -->
      <el-tab-pane label="仓储" name="storage">
        <div class="business-section">
          <div class="section-header">
            <h3>仓储记录</h3>
          </div>
          
          <!-- 记录列表 -->
          <el-table :data="storageList" stripe class="business-table" v-loading="loading">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="storageTime" label="入库时间" :formatter="formatDate" />
            <el-table-column prop="outboundTime" label="出库时间" :formatter="formatDate" />
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="unit" label="单位" />
          </el-table>
          <el-empty v-if="!loading && storageList.length === 0" description="暂无仓储记录" />
        </div>
      </el-tab-pane>

      <!-- 运输销售 -->
      <el-tab-pane label="运输销售" name="transport">
        <div class="business-section">
          <div class="section-header">
            <h3>运输销售记录</h3>
          </div>
          
          <!-- 记录列表 -->
          <el-table :data="transportList" stripe class="business-table" v-loading="loading">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="environmentTemperature" label="环境温度(℃)" />
            <el-table-column prop="productTemperature" label="产品温度(℃)" />
            <el-table-column prop="time" label="记录时间" :formatter="formatDate" />
          </el-table>
          <el-empty v-if="!loading && transportList.length === 0" description="暂无运输销售记录" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const loading = ref(false)

// 业务数据列表
const materialList = ref([])
const inspectionList = ref([])
const storageList = ref([])
const transportList = ref([])

const API_BASE_URL = '/api'

function formatDate(row, column, cellValue) {
  if (!cellValue) return '-'
  return cellValue.split('T')[0]
}

function formatDateTime(row, column, cellValue) {
  if (!cellValue) return '-'
  return cellValue.replace('T', ' ').substring(0, 16)
}

async function loadBusinessData() {
  loading.value = true
  try {
    const antiFakeCode = props.product.antiFakeCode
    const response = await axios.get(`${API_BASE_URL}/product-detail`, { 
      params: { antiFakeCode } 
    })
    const data = response.data
    
    if (data.product) {
      materialList.value = data.materialPurchases || []
      inspectionList.value = data.inspections || []
      storageList.value = data.storages || []
      transportList.value = data.transportSales || []
    }
  } catch (error) {
    console.error('加载业务数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBusinessData()
})
</script>

<style scoped>
.product-detail {
  padding: 1rem 0;
}

.product-info-card {
  margin-bottom: 1.5rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-primary-dark);
}

.business-tabs {
  margin-top: 1rem;
}

.business-section {
  padding: 1rem 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--color-text);
}

.business-table {
  margin-top: 1rem;
}

@media (max-width: 768px) {
  .product-detail {
    padding: 0.5rem 0;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
}
</style>
