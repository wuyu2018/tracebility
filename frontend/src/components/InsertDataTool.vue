<template>
  <div class="data-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h1>数据管理中心</h1>
          <p class="subtitle">产品追溯信息管理</p>
        </div>
      </template>

      <el-steps :active="currentStep" finish-status="success" class="main-steps">
        <el-step title="产品信息" description="录入产品基本信息" />
        <el-step title="原材料" description="录入原材料采购信息" />
        <el-step title="生产批次" description="创建生产批次并关联原材料" />
        <el-step title="防伪码" description="生成防伪码" />
      </el-steps>

      <div class="step-content">
        <div v-show="currentStep === 0" class="step-panel">
          <h2>产品信息管理</h2>
          <el-form :model="productForm" label-width="120px" class="product-form">
            <el-form-item label="产品名称" required>
              <el-input v-model="productForm.name" placeholder="请输入产品名称" />
            </el-form-item>
            <el-form-item label="规格">
              <el-input v-model="productForm.specification" placeholder="例：400g、5kg" />
            </el-form-item>
            <el-form-item label="保质期">
              <el-input v-model="productForm.shelfLife" placeholder="例：12个月" />
            </el-form-item>
            <el-form-item label="产品图片">
              <el-input v-model="productForm.imageUrl" placeholder="图片URL地址" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="productForm.contactPhone" placeholder="联系电话" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="productForm.contactEmail" placeholder="邮箱地址" />
            </el-form-item>
          </el-form>
          <div class="form-actions">
            <el-button type="primary" @click="submitProduct" :loading="loading">保存产品</el-button>
          </div>

          <el-divider>已录入产品</el-divider>
          <el-table :data="products" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="产品名称" />
            <el-table-column prop="specification" label="规格" />
            <el-table-column prop="shelfLife" label="保质期" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="primary" size="small" @click="editProduct(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteProduct(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-show="currentStep === 1" class="step-panel">
          <h2>原材料采购管理</h2>
          <el-form :model="materialForm" label-width="120px" class="product-form">
            <el-form-item label="选择产品" required>
              <el-select v-model="materialForm.productId" placeholder="请选择产品" @change="onProductSelect">
                <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="原料名称" required>
              <el-input v-model="materialForm.materialName" placeholder="请输入原料名称" />
            </el-form-item>
            <el-form-item label="采购批次号" required>
              <el-input v-model="materialForm.batchNumber" placeholder="请输入采购批次号" />
            </el-form-item>
            <el-form-item label="供应商">
              <el-input v-model="materialForm.supplierName" placeholder="供应商名称" />
            </el-form-item>
            <el-form-item label="生产商">
              <el-input v-model="materialForm.producerName" placeholder="生产商名称" />
            </el-form-item>
            <el-form-item label="生产商地址">
              <el-input v-model="materialForm.producerAddress" placeholder="生产商地址" />
            </el-form-item>
          </el-form>
          <div class="form-actions">
            <el-button type="primary" @click="submitMaterial" :loading="loading">保存原材料</el-button>
            <el-button @click="resetMaterialForm">重置</el-button>
          </div>

          <el-divider>已录入原材料</el-divider>
          <el-table :data="materials" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="materialName" label="原料名称" />
            <el-table-column prop="batchNumber" label="批次号" />
            <el-table-column prop="supplierName" label="供应商" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="danger" size="small" @click="deleteMaterial(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-show="currentStep === 2" class="step-panel">
          <h2>生产批次管理</h2>
          <el-form :model="batchForm" label-width="140px" class="batch-form">
            <el-form-item label="选择产品" required>
              <el-select v-model="batchForm.productId" placeholder="请选择产品" @change="loadMaterialsForProduct">
                <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="生产日期" required>
              <el-date-picker v-model="batchForm.productionDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择生产日期" />
            </el-form-item>
            <el-form-item label="保质期">
              <el-input v-model="batchForm.shelfLife" placeholder="例：12个月" />
            </el-form-item>
            <el-form-item label="数量">
              <el-input-number v-model="batchForm.quantity" :min="0" />
            </el-form-item>
            <el-form-item label="单位">
              <el-input v-model="batchForm.unit" placeholder="例：千克、箱" />
            </el-form-item>
            <el-form-item label="选择原材料批次" required>
              <el-select v-model="batchForm.materialIds" multiple placeholder="至少选择一个原料批次">
                <el-option v-for="m in availableMaterials" :key="m.id" :label="`${m.materialName} (${m.batchNumber})`" :value="m.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="入库时间">
              <el-date-picker v-model="batchForm.storageTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择入库时间" />
            </el-form-item>
            <el-form-item label="出库时间">
              <el-date-picker v-model="batchForm.outboundTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择出库时间" />
            </el-form-item>
            <el-form-item label="仓库地址">
              <el-input v-model="batchForm.warehouseLocation" placeholder="仓库地址" />
            </el-form-item>
            <el-form-item label="运输公司">
              <el-input v-model="batchForm.transportCompany" placeholder="运输公司" />
            </el-form-item>
            <el-form-item label="车牌号">
              <el-input v-model="batchForm.vehicleNumber" placeholder="车牌号" />
            </el-form-item>
            <el-form-item label="销售区域">
              <el-input v-model="batchForm.salesRegion" placeholder="销售区域" />
            </el-form-item>
            <el-form-item label="收货人">
              <el-input v-model="batchForm.receiverName" placeholder="收货人" />
            </el-form-item>
            <el-form-item label="收货人联系方式">
              <el-input v-model="batchForm.receiverContact" placeholder="联系方式" />
            </el-form-item>
          </el-form>
          <div class="form-actions">
            <el-button type="primary" @click="submitBatch" :loading="loading">生成批次</el-button>
            <el-button @click="resetBatchForm">重置</el-button>
          </div>

          <el-divider>已生成批次</el-divider>
          <el-table :data="batches" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="batchNumber" label="批次号" />
            <el-table-column prop="product.name" label="产品名称" />
            <el-table-column prop="productionDate" label="生产日期" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="success" size="small" @click="goToSecurityCode(scope.row)">生成防伪码</el-button>
                <el-button type="primary" size="small" @click="viewBatchDetail(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-show="currentStep === 3" class="step-panel">
          <h2>防伪码管理</h2>
          <div v-if="currentBatch" class="current-batch-info">
            <el-alert type="info" :closable="false">
              <template #title>
                当前批次：{{ currentBatch.batchNumber }} - {{ currentBatch.product.name }}
              </template>
            </el-alert>
          </div>
          <div v-else class="no-batch">
            <el-empty description="请先在生产批次中选择一个批次" />
          </div>

          <div class="code-generation" v-if="currentBatch">
            <el-form label-width="100px">
              <el-form-item label="生成数量">
                <el-input-number v-model="codeQuantity" :min="1" :max="10000" />
              </el-form-item>
            </el-form>
            <el-button type="primary" @click="generateCodes" :loading="generatingCodes">生成防伪码</el-button>
            <el-button type="success" @click="exportCodes" :disabled="!securityCodes.length">导出防伪码</el-button>
          </div>

          <el-divider v-if="securityCodes.length">已生成的防伪码 ({{ securityCodes.length }})</el-divider>
          <el-table v-if="securityCodes.length" :data="securityCodes" style="width: 100%" max-height="400">
            <el-table-column prop="code" label="防伪码" />
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column prop="firstScanTime" label="首次扫码时间" width="180" />
            <el-table-column prop="scanCount" label="扫码次数" width="100" />
          </el-table>
        </div>
      </div>

      <div class="step-navigation">
        <el-button @click="prevStep" :disabled="currentStep === 0">上一步</el-button>
        <el-button type="primary" @click="nextStep" :disabled="currentStep === 3">下一步</el-button>
      </div>
    </el-card>

    <el-dialog v-model="batchDetailVisible" title="批次详情" width="700px">
      <div v-if="batchDetail" class="batch-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次号">{{ batchDetail.batchNumber }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ batchDetail.product?.name }}</el-descriptions-item>
          <el-descriptions-item label="生产日期">{{ batchDetail.productionDate }}</el-descriptions-item>
          <el-descriptions-item label="保质期">{{ batchDetail.shelfLife }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ batchDetail.quantity }} {{ batchDetail.unit }}</el-descriptions-item>
        </el-descriptions>

        <h4>检测报告</h4>
        <el-form v-if="inspectionForm" :model="inspectionForm" label-width="100px">
          <el-form-item label="样品名称">
            <el-input v-model="inspectionForm.sampleName" />
          </el-form-item>
          <el-form-item label="抽样数量">
            <el-input-number v-model="inspectionForm.sampleQuantity" :min="0" />
          </el-form-item>
          <el-form-item label="样品规格">
            <el-input v-model="inspectionForm.sampleSpecification" />
          </el-form-item>
          <el-form-item label="检测报告图片">
            <el-input v-model="inspectionForm.imageUrl" placeholder="图片URL" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitInspection">保存检测报告</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const API_BASE = '/api'

const currentStep = ref(0)
const loading = ref(false)
const generatingCodes = ref(false)

const products = ref([])
const materials = ref([])
const batches = ref([])
const securityCodes = ref([])
const currentBatch = ref(null)
const batchDetail = ref(null)
const batchDetailVisible = ref(false)
const availableMaterials = ref([])

const codeQuantity = ref(100)

const productForm = reactive({
  id: null,
  name: '',
  specification: '',
  shelfLife: '',
  imageUrl: '',
  contactPhone: '',
  contactEmail: ''
})

const materialForm = reactive({
  id: null,
  productId: null,
  materialName: '',
  batchNumber: '',
  supplierName: '',
  producerName: '',
  producerAddress: ''
})

const batchForm = reactive({
  productId: null,
  productionDate: '',
  shelfLife: '',
  quantity: null,
  unit: '',
  materialIds: [],
  storageTime: '',
  outboundTime: '',
  warehouseLocation: '',
  transportCompany: '',
  vehicleNumber: '',
  salesRegion: '',
  receiverName: '',
  receiverContact: ''
})

const inspectionForm = reactive({
  sampleName: '',
  sampleQuantity: null,
  sampleSpecification: '',
  imageUrl: ''
})

async function loadProducts() {
  try {
    const res = await axios.get(`${API_BASE}/products`)
    products.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function loadMaterials() {
  try {
    const res = await axios.get(`${API_BASE}/materials`)
    materials.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function loadBatches() {
  try {
    const res = await axios.get(`${API_BASE}/batches`)
    batches.value = res.data
  } catch (e) {
    console.error(e)
  }
}

function onProductSelect() {
  loadMaterialsForProduct()
}

async function loadMaterialsForProduct() {
  if (batchForm.productId) {
    try {
      const res = await axios.get(`${API_BASE}/materials?productId=${batchForm.productId}`)
      availableMaterials.value = res.data
    } catch (e) {
      console.error(e)
    }
  }
}

async function submitProduct() {
  if (!productForm.name) {
    ElMessage.warning('产品名称不能为空')
    return
  }
  loading.value = true
  try {
    if (productForm.id) {
      await axios.put(`${API_BASE}/products/${productForm.id}`, productForm)
      ElMessage.success('产品更新成功')
    } else {
      await axios.post(`${API_BASE}/products`, productForm)
      ElMessage.success('产品保存成功')
    }
    resetProductForm()
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '保存失败')
  } finally {
    loading.value = false
  }
}

function editProduct(row) {
  Object.assign(productForm, row)
}

async function deleteProduct(id) {
  try {
    await ElMessageBox.confirm('确定要删除该产品吗？', '提示', { type: 'warning' })
    await axios.delete(`${API_BASE}/products/${id}`)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function resetProductForm() {
  Object.keys(productForm).forEach(k => {
    if (k === 'id') productForm[k] = null
    else if (typeof productForm[k] === 'string') productForm[k] = ''
    else productForm[k] = null
  })
}

async function submitMaterial() {
  if (!materialForm.productId || !materialForm.materialName || !materialForm.batchNumber) {
    ElMessage.warning('请填写必填项')
    return
  }
  loading.value = true
  try {
    if (materialForm.id) {
      await axios.put(`${API_BASE}/materials/${materialForm.id}`, materialForm)
      ElMessage.success('原材料更新成功')
    } else {
      await axios.post(`${API_BASE}/materials`, materialForm)
      ElMessage.success('原材料保存成功')
    }
    resetMaterialForm()
    loadMaterials()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '保存失败')
  } finally {
    loading.value = false
  }
}

function resetMaterialForm() {
  Object.keys(materialForm).forEach(k => {
    if (k === 'id' || k === 'productId') materialForm[k] = null
    else if (typeof materialForm[k] === 'string') materialForm[k] = ''
    else materialForm[k] = null
  })
}

async function deleteMaterial(id) {
  try {
    await ElMessageBox.confirm('确定要删除该原材料吗？', '提示', { type: 'warning' })
    await axios.delete(`${API_BASE}/materials/${id}`)
    ElMessage.success('删除成功')
    loadMaterials()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function submitBatch() {
  if (!batchForm.productId || !batchForm.productionDate) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (!batchForm.materialIds || batchForm.materialIds.length === 0) {
    ElMessage.warning('至少选择一个原料批次')
    return
  }
  loading.value = true
  try {
    const payload = { ...batchForm }
    if (payload.storageTime && payload.outboundTime) {
      payload.storage = {
        storageTime: payload.storageTime,
        outboundTime: payload.outboundTime,
        warehouseLocation: payload.warehouseLocation
      }
      payload.transportSale = {
        time: payload.storageTime,
        transportCompany: payload.transportCompany,
        vehicleNumber: payload.vehicleNumber,
        salesRegion: payload.salesRegion,
        receiverName: payload.receiverName,
        receiverContact: payload.receiverContact
      }
    }
    delete payload.storageTime
    delete payload.outboundTime
    delete payload.warehouseLocation
    delete payload.transportCompany
    delete payload.vehicleNumber
    delete payload.salesRegion
    delete payload.receiverName
    delete payload.receiverContact

    const res = await axios.post(`${API_BASE}/batches`, payload)
    ElMessage.success(`批次生成成功！批次号：${res.data.batchNumber}`)
    resetBatchForm()
    loadBatches()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '生成失败')
  } finally {
    loading.value = false
  }
}

function resetBatchForm() {
  Object.keys(batchForm).forEach(k => {
    if (k === 'productId' || k === 'materialIds') batchForm[k] = k === 'materialIds' ? [] : null
    else if (typeof batchForm[k] === 'string') batchForm[k] = ''
    else batchForm[k] = null
  })
}

function goToSecurityCode(batch) {
  currentBatch.value = batch
  currentStep.value = 3
  loadSecurityCodes(batch.id)
}

async function loadSecurityCodes(batchId) {
  try {
    const res = await axios.get(`${API_BASE}/batches/${batchId}/security-codes`)
    securityCodes.value = res.data
  } catch (e) {
    console.error('加载防伪码失败:', e)
    ElMessage.error('加载防伪码失败')
  }
}

async function generateCodes() {
  if (!currentBatch.value) return
  generatingCodes.value = true
  try {
    const res = await axios.post(`${API_BASE}/batches/${currentBatch.value.id}/security-codes`, {
      quantity: codeQuantity.value
    })
    ElMessage.success(`成功生成 ${res.data.count} 个防伪码`)
    loadSecurityCodes(currentBatch.value.id)
  } catch (e) {
    ElMessage.error('生成失败')
  } finally {
    generatingCodes.value = false
  }
}

function exportCodes() {
  const content = securityCodes.value.map(c => c.code).join('\n')
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `防伪码_${currentBatch.value.batchNumber}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

async function viewBatchDetail(batch) {
  batchDetail.value = batch
  batchDetailVisible.value = true
}

async function submitInspection() {
  if (!batchDetail.value) return
  try {
    await axios.post(`${API_BASE}/batches/${batchDetail.value.id}/inspection`, inspectionForm)
    ElMessage.success('检测报告保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

function prevStep() {
  if (currentStep.value > 0) currentStep.value--
}

function nextStep() {
  if (currentStep.value < 3) currentStep.value++
}

onMounted(() => {
  loadProducts()
  loadMaterials()
  loadBatches()
})
</script>

<style scoped>
.data-management {
  padding: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

.main-card {
  border-radius: var(--radius-lg);
}

.card-header h1 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--color-primary-dark);
}

.subtitle {
  margin: 0.25rem 0 0;
  color: var(--color-text-muted);
}

.main-steps {
  margin: 2rem 0;
}

.step-content {
  min-height: 400px;
}

.step-panel h2 {
  margin: 0 0 1.5rem;
  font-size: 1.2rem;
  color: var(--color-primary);
}

.product-form,
.batch-form {
  max-width: 600px;
}

.form-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}

.step-navigation {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.current-batch-info {
  margin-bottom: 1.5rem;
}

.no-batch {
  padding: 3rem 0;
}

.code-generation {
  margin: 1.5rem 0;
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.batch-detail h4 {
  margin: 1.5rem 0 1rem;
  color: var(--color-primary);
}
</style>