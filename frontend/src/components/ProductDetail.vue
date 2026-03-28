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
            <el-button type="primary" @click="showAddForm('material')">添加记录</el-button>
          </div>
          
          <!-- 添加表单 -->
          <el-card v-if="addingFormType === 'material'" class="add-form-card" shadow="hover">
            <template #header>
              <span>添加原材料采购记录</span>
              <el-button text @click="cancelAddForm">取消</el-button>
            </template>
            <el-form :model="materialForm" label-width="120px">
              <el-form-item label="防伪码">
                <el-input v-model="materialForm.antiFakeCode" disabled>
                  <template #append>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="批号">
                <el-input v-model="materialForm.batchNumber" placeholder="请输入批号" />
              </el-form-item>
              <el-form-item label="原料名称" required>
                <el-input v-model="materialForm.materialName" placeholder="请输入原料名称" />
              </el-form-item>
              <el-form-item label="供应商">
                <el-input v-model="materialForm.supplierName" placeholder="请输入供应商名称" />
              </el-form-item>
              <el-form-item label="生产商">
                <el-input v-model="materialForm.producerName" placeholder="请输入生产商名称" />
              </el-form-item>
              <el-form-item label="生产商地址">
                <el-input v-model="materialForm.producerAddress" placeholder="请输入生产商地址" />
              </el-form-item>
              <el-form-item label="采购时间">
                <el-date-picker v-model="materialForm.purchaseDate" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择采购时间" />
              </el-form-item>
              <el-form-item label="数量">
                <el-input-number v-model="materialForm.quantity" :min="0" placeholder="数量" />
              </el-form-item>
              <el-form-item label="单位">
                <el-input v-model="materialForm.unit" placeholder="如：kg、吨、箱" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitMaterialForm" :loading="submitting">提交</el-button>
                <el-button @click="cancelAddForm">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 记录列表 -->
          <el-table :data="materialList" stripe class="business-table" v-loading="loading">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="materialName" label="原料名称" />
            <el-table-column prop="purchaseDate" label="采购时间" :formatter="formatDateTime" />
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="unit" label="单位" />
            <el-table-column prop="producerName" label="生产商" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button type="danger" size="small" @click="deleteMaterial(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && materialList.length === 0" description="暂无采购记录" />
        </div>
      </el-tab-pane>

      <!-- 检验检测 -->
      <el-tab-pane label="检验检测" name="inspection">
        <div class="business-section">
          <div class="section-header">
            <h3>检验检测记录</h3>
            <el-button type="primary" @click="showAddForm('inspection')">添加记录</el-button>
          </div>
          
          <!-- 添加表单 -->
          <el-card v-if="addingFormType === 'inspection'" class="add-form-card" shadow="hover">
            <template #header>
              <span>添加检验检测记录</span>
              <el-button text @click="cancelAddForm">取消</el-button>
            </template>
            <el-form :model="inspectionForm" label-width="120px">
              <el-form-item label="防伪码">
                <el-input v-model="inspectionForm.antiFakeCode" disabled>
                  <template #append>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="批号">
                <el-input v-model="inspectionForm.batchNumber" placeholder="请输入批号" />
              </el-form-item>
              <el-form-item label="样品名称">
                <el-input v-model="inspectionForm.sampleName" placeholder="请输入样品名称" />
              </el-form-item>
              <el-form-item label="样品数量">
                <el-input-number v-model="inspectionForm.sampleQuantity" :min="0" />
              </el-form-item>
              <el-form-item label="样品规格">
                <el-input v-model="inspectionForm.sampleSpecification" placeholder="请输入样品规格" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitInspectionForm" :loading="submitting">提交</el-button>
                <el-button @click="cancelAddForm">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 记录列表 -->
          <el-table :data="inspectionList" stripe class="business-table" v-loading="loading">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="sampleName" label="样品名称" />
            <el-table-column prop="sampleQuantity" label="样品数量" />
            <el-table-column prop="sampleSpecification" label="样品规格" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button type="danger" size="small" @click="deleteInspection(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && inspectionList.length === 0" description="暂无检验记录" />
        </div>
      </el-tab-pane>

      <!-- 仓储 -->
      <el-tab-pane label="仓储" name="storage">
        <div class="business-section">
          <div class="section-header">
            <h3>仓储记录</h3>
            <el-button type="primary" @click="showAddForm('storage')">添加记录</el-button>
          </div>
          
          <!-- 添加表单 -->
          <el-card v-if="addingFormType === 'storage'" class="add-form-card" shadow="hover">
            <template #header>
              <span>添加仓储记录</span>
              <el-button text @click="cancelAddForm">取消</el-button>
            </template>
            <el-form :model="storageForm" label-width="120px">
              <el-form-item label="防伪码">
                <el-input v-model="storageForm.antiFakeCode" disabled>
                  <template #append>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="批号">
                <el-input v-model="storageForm.batchNumber" placeholder="请输入批号" />
              </el-form-item>
              <el-form-item label="入库时间">
                <el-date-picker
                  v-model="storageForm.storageTime"
                  type="date"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  placeholder="选择入库日期"
                />
              </el-form-item>
              <el-form-item label="出库时间">
                <el-date-picker
                  v-model="storageForm.outboundTime"
                  type="date"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  placeholder="选择出库日期"
                />
              </el-form-item>
              <el-form-item label="数量">
                <el-input-number v-model="storageForm.quantity" :min="0" :precision="2" />
              </el-form-item>
              <el-form-item label="单位">
                <el-input v-model="storageForm.unit" placeholder="例：千克、箱" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitStorageForm" :loading="submitting">提交</el-button>
                <el-button @click="cancelAddForm">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 记录列表 -->
          <el-table :data="storageList" stripe class="business-table" v-loading="loading">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="storageTime" label="入库时间" :formatter="formatDate" />
            <el-table-column prop="outboundTime" label="出库时间" :formatter="formatDate" />
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="unit" label="单位" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button type="danger" size="small" @click="deleteStorage(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && storageList.length === 0" description="暂无仓储记录" />
        </div>
      </el-tab-pane>

      <!-- 运输销售 -->
      <el-tab-pane label="运输销售" name="transport">
        <div class="business-section">
          <div class="section-header">
            <h3>运输销售记录</h3>
            <el-button type="primary" @click="showAddForm('transport')">添加记录</el-button>
          </div>
          
          <!-- 添加表单 -->
          <el-card v-if="addingFormType === 'transport'" class="add-form-card" shadow="hover">
            <template #header>
              <span>添加运输销售记录</span>
              <el-button text @click="cancelAddForm">取消</el-button>
            </template>
            <el-form :model="transportForm" label-width="120px">
              <el-form-item label="防伪码">
                <el-input v-model="transportForm.antiFakeCode" disabled>
                  <template #append>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="批号">
                <el-input v-model="transportForm.batchNumber" placeholder="请输入批号" />
              </el-form-item>
              <el-form-item label="环境温度">
                <el-input-number v-model="transportForm.environmentTemperature" :min="-50" :max="100" :precision="1" />
                <span class="unit-label">℃</span>
              </el-form-item>
              <el-form-item label="产品温度">
                <el-input-number v-model="transportForm.productTemperature" :min="-50" :max="100" :precision="1" />
                <span class="unit-label">℃</span>
              </el-form-item>
              <el-form-item label="记录时间">
                <el-date-picker
                  v-model="transportForm.time"
                  type="date"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  placeholder="选择记录日期"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitTransportForm" :loading="submitting">提交</el-button>
                <el-button @click="cancelAddForm">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 记录列表 -->
          <el-table :data="transportList" stripe class="business-table" v-loading="loading">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="environmentTemperature" label="环境温度(℃)" />
            <el-table-column prop="productTemperature" label="产品温度(℃)" />
            <el-table-column prop="time" label="记录时间" :formatter="formatDate" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button type="danger" size="small" @click="deleteTransport(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && transportList.length === 0" description="暂无运输销售记录" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { getPurchaseInfo } from '../services/api'
import axios from 'axios'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close'])

const activeBusinessTab = ref('material')
const loading = ref(false)
const submitting = ref(false)
const addingFormType = ref('')

// 业务数据列表
const materialList = ref([])
const inspectionList = ref([])
const storageList = ref([])
const transportList = ref([])

// 表单数据
const materialForm = reactive({
  antiFakeCode: '',
  batchNumber: '',
  materialName: '',
  supplierName: '',
  producerName: '',
  producerAddress: '',
  purchaseDate: '',
  quantity: null,
  unit: ''
})

const inspectionForm = reactive({
  antiFakeCode: '',
  batchNumber: '',
  sampleName: '',
  sampleQuantity: null,
  sampleSpecification: ''
})

const storageForm = reactive({
  antiFakeCode: '',
  batchNumber: '',
  storageTime: '',
  outboundTime: '',
  quantity: null,
  unit: ''
})

const transportForm = reactive({
  antiFakeCode: '',
  batchNumber: '',
  environmentTemperature: null,
  productTemperature: null,
  time: ''
})

const API_BASE_URL = '/api'

function formatDate(row, column, cellValue) {
  if (!cellValue) return '-'
  return cellValue.split('T')[0]
}

function formatDateTime(row, column, cellValue) {
  if (!cellValue) return '-'
  return cellValue.replace('T', ' ').substring(0, 16)
}

function initFormAntiFakeCode() {
  const code = props.product.antiFakeCode
  materialForm.antiFakeCode = code
  inspectionForm.antiFakeCode = code
  storageForm.antiFakeCode = code
  transportForm.antiFakeCode = code
}

function resetForms() {
  Object.keys(materialForm).forEach(key => {
    if (key !== 'antiFakeCode') materialForm[key] = key === 'quantity' ? null : ''
  })
  Object.keys(inspectionForm).forEach(key => {
    if (key !== 'antiFakeCode') inspectionForm[key] = key === 'sampleQuantity' ? null : ''
  })
  Object.keys(storageForm).forEach(key => {
    if (key !== 'antiFakeCode') storageForm[key] = key === 'quantity' ? null : ''
  })
  Object.keys(transportForm).forEach(key => {
    if (key !== 'antiFakeCode') transportForm[key] = key === 'environmentTemperature' || key === 'productTemperature' ? null : ''
  })
}

function showAddForm(type) {
  addingFormType.value = type
  resetForms()
  initFormAntiFakeCode()
}

function cancelAddForm() {
  addingFormType.value = ''
  resetForms()
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
    ElMessage.error('加载业务数据失败')
  } finally {
    loading.value = false
  }
}

async function submitMaterialForm() {
  if (!materialForm.materialName) {
    ElMessage.warning('请输入原料名称')
    return
  }
  
  submitting.value = true
  try {
    await axios.post(`${API_BASE_URL}/insert/material-purchase`, {
      antiFakeCode: materialForm.antiFakeCode,
      batchNumber: materialForm.batchNumber || null,
      materialName: materialForm.materialName,
      supplierName: materialForm.supplierName || null,
      producerName: materialForm.producerName || null,
      producerAddress: materialForm.producerAddress || null,
      purchaseDate: materialForm.purchaseDate || null,
      quantity: materialForm.quantity,
      unit: materialForm.unit || null
    })
    ElMessage.success('添加成功')
    cancelAddForm()
    await loadBusinessData()
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error(error.response?.data || '添加失败')
  } finally {
    submitting.value = false
  }
}

async function submitInspectionForm() {
  submitting.value = true
  try {
    await axios.post(`${API_BASE_URL}/insert/inspection`, {
      antiFakeCode: inspectionForm.antiFakeCode,
      batchNumber: inspectionForm.batchNumber || null,
      sampleName: inspectionForm.sampleName || null,
      sampleQuantity: inspectionForm.sampleQuantity,
      sampleSpecification: inspectionForm.sampleSpecification || null
    })
    ElMessage.success('添加成功')
    cancelAddForm()
    await loadBusinessData()
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error(error.response?.data || '添加失败')
  } finally {
    submitting.value = false
  }
}

async function submitStorageForm() {
  submitting.value = true
  try {
    await axios.post(`${API_BASE_URL}/insert/storage`, {
      antiFakeCode: storageForm.antiFakeCode,
      batchNumber: storageForm.batchNumber || null,
      storageTime: storageForm.storageTime ? `${storageForm.storageTime}T00:00:00` : null,
      outboundTime: storageForm.outboundTime ? `${storageForm.outboundTime}T00:00:00` : null,
      quantity: storageForm.quantity,
      unit: storageForm.unit || null
    })
    ElMessage.success('添加成功')
    cancelAddForm()
    await loadBusinessData()
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error(error.response?.data || '添加失败')
  } finally {
    submitting.value = false
  }
}

async function submitTransportForm() {
  submitting.value = true
  try {
    await axios.post(`${API_BASE_URL}/insert/transport-sale`, {
      antiFakeCode: transportForm.antiFakeCode,
      batchNumber: transportForm.batchNumber || null,
      environmentTemperature: transportForm.environmentTemperature,
      productTemperature: transportForm.productTemperature,
      time: transportForm.time ? `${transportForm.time}T00:00:00` : null
    })
    ElMessage.success('添加成功')
    cancelAddForm()
    await loadBusinessData()
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error(error.response?.data || '添加失败')
  } finally {
    submitting.value = false
  }
}

async function deleteMaterial(id) {
  try {
    await axios.delete(`${API_BASE_URL}/insert/material-purchase/${id}`)
    ElMessage.success('删除成功')
    await loadBusinessData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

async function deleteInspection(id) {
  try {
    await axios.delete(`${API_BASE_URL}/insert/inspection/${id}`)
    ElMessage.success('删除成功')
    await loadBusinessData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

async function deleteStorage(id) {
  try {
    await axios.delete(`${API_BASE_URL}/insert/storage/${id}`)
    ElMessage.success('删除成功')
    await loadBusinessData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

async function deleteTransport(id) {
  try {
    await axios.delete(`${API_BASE_URL}/insert/transport-sale/${id}`)
    ElMessage.success('删除成功')
    await loadBusinessData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  initFormAntiFakeCode()
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

.add-form-card {
  margin-bottom: 1.5rem;
  background: var(--color-bg);
}

.business-table {
  margin-top: 1rem;
}

.unit-label {
  margin-left: 0.5rem;
  color: var(--color-text-muted);
}

:deep(.el-input-number) {
  width: 200px;
}

:deep(.el-date-editor) {
  width: 200px !important;
}

:deep(.el-input) {
  width: 200px;
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
