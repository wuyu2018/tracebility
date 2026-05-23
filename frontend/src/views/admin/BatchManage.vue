<template>
  <div class="page-container">
    <div class="page-header">
      <h3>生产批次管理</h3>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增批次
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="batchNumber" label="批次号" width="180" />
      <el-table-column label="产品" width="120">
        <template #default="{ row }">{{ row.product?.name || row.productName || row.productId || '-' }}</template>
      </el-table-column>
      <el-table-column prop="productionDate" label="生产日期" width="120" />
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button size="small" @click="showCodeDialog(row)">防伪码</el-button>
          <el-button size="small" @click="handleExport(row)">导出</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增批次" width="520px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="batchRules" label-width="90px">
        <el-form-item label="选择产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择" filterable style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker v-model="form.productionDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="保质期">
          <el-input v-model="form.shelfLife" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" />
        </el-form-item>
        <el-form-item label="原料采购" prop="materialPurchaseIds">
          <el-select v-model="form.materialPurchaseIds" multiple placeholder="请选择原料采购记录" filterable style="width:100%">
            <el-option v-for="m in purchases" :key="m.id" :label="m.batchNumber + ' - ' + (m.material?.name || m.materialName || '')" :value="m.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="codeDialogVisible" title="防伪码管理" width="700px">
      <div class="code-header">
        <span>批次：{{ selectedBatch?.batchNumber }}</span>
        <el-button type="primary" size="small" @click="generateDialogVisible = true">
          批量生成防伪码
        </el-button>
      </div>
      <el-table :data="codes" v-loading="codeLoading" max-height="400" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="code" label="防伪码" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ACTIVE' ? 'success' : row.status === 'FROZEN' ? 'warning' : 'info'"
              size="small"
            >
              {{ row.status === 'ACTIVE' ? '已激活' : row.status === 'FROZEN' ? '已冻结' : '未激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scanCount" label="扫码次数" width="100" />
        <el-table-column prop="firstScanTime" label="首次扫描" width="180" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="generateDialogVisible" title="生成防伪码" width="400px">
      <el-form ref="generateFormRef" :model="generateForm" label-width="80px">
        <el-form-item label="生成数量" prop="count">
          <el-input-number v-model="generateForm.count" :min="1" :max="100000" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getBatches, createBatch, deleteBatch,
  generateSecurityCodes, getSecurityCodes, exportSecurityCodes, getProducts, getMaterialPurchases
} from '@/api/admin'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const products = ref([])
const purchases = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  productId: '', productionDate: '', shelfLife: '', quantity: 0, unit: '', materialPurchaseIds: []
})

const batchRules = {
  productId: [{ required: true, message: '请选择产品' }],
  productionDate: [{ required: true, message: '请选择生产日期' }],
  quantity: [{ required: true, message: '请输入数量' }],
  materialPurchaseIds: [{ type: 'array', required: true, message: '请选择至少一个原料采购记录', trigger: 'change' }]
}

const codeDialogVisible = ref(false)
const selectedBatch = ref(null)
const codes = ref([])
const codeLoading = ref(false)

const generateDialogVisible = ref(false)
const generateFormRef = ref(null)
const generating = ref(false)
const generateForm = reactive({ count: 100 })

async function fetchList() {
  loading.value = true
  try {
    const res = await getBatches()
    list.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { loading.value = false }
}

async function openDialog() {
  const [prodRes, purchRes] = await Promise.allSettled([getProducts(), getMaterialPurchases()])
  if (prodRes.status === 'fulfilled') {
    products.value = Array.isArray(prodRes.value) ? prodRes.value : prodRes.value?.data || prodRes.value?.records || []
  }
  if (purchRes.status === 'fulfilled') {
    purchases.value = Array.isArray(purchRes.value) ? purchRes.value : purchRes.value?.data || purchRes.value?.records || []
  }
  resetForm()
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  form.productId = ''
  form.productionDate = ''
  form.shelfLife = ''
  form.quantity = 0
  form.unit = ''
  form.materialPurchaseIds = []
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createBatch({ ...form })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await deleteBatch(id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* */ }
}

async function showCodeDialog(row) {
  selectedBatch.value = row
  codeDialogVisible.value = true
  codeLoading.value = true
  try {
    const res = await getSecurityCodes(row.id)
    codes.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { codeLoading.value = false }
}

async function handleGenerate() {
  generating.value = true
  try {
    await generateSecurityCodes(selectedBatch.value.id, { quantity: generateForm.count })
    ElMessage.success('生成成功')
    generateDialogVisible.value = false
    const res = await getSecurityCodes(selectedBatch.value.id)
    codes.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { generating.value = false }
}

function handleExport(row) {
  try {
    exportSecurityCodes(row.id)
    ElMessage.success('导出请求已发送')
  } catch { /* */ }
}

fetchList()
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
.code-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; font-size: 15px; color: #303133; }
</style>
