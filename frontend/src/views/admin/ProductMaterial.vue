<template>
  <div class="page-container">
    <div class="page-header">
      <h3>产品原料关联</h3>
      <el-button type="primary" @click="openBindDialog">
        <el-icon><Plus /></el-icon>新增关联
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="产品">
        <template #default="{ row }">{{ row.product?.name || row.productName || '-' }}</template>
      </el-table-column>
      <el-table-column label="原料品种">
        <template #default="{ row }">{{ row.material?.name || row.materialName || '-' }}</template>
      </el-table-column>
      <el-table-column label="可见性" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isHidden ? 'info' : 'success'" size="small">
            {{ row.isHidden ? '隐藏' : '可见' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="toggleVisibility(row)">
            {{ row.isHidden ? '设为可见' : '设为隐藏' }}
          </el-button>
          <el-popconfirm title="确定解绑？" @confirm="handleUnbind(row)">
            <template #reference>
              <el-button size="small" type="danger">解绑</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增关联" width="400px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择产品" filterable style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="原料品种" prop="materialId">
          <el-select v-model="form.materialId" placeholder="请选择原料" filterable style="width:100%">
            <el-option v-for="m in materials" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleBind">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getProductMaterials, bindProductMaterial, unbindProductMaterial,
  toggleProductMaterialVisibility, getProducts, getMaterialVarieties
} from '@/api/admin'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const products = ref([])
const materials = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({ productId: '', materialId: '' })
const rules = {
  productId: [{ required: true, message: '请选择产品' }],
  materialId: [{ required: true, message: '请选择原料品种' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getProductMaterials()
    list.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } catch {
    // error already shown by response interceptor
  } finally { loading.value = false }
}

async function openBindDialog() {
  const [prodRes, matRes] = await Promise.allSettled([getProducts(), getMaterialVarieties()])
  if (prodRes.status === 'fulfilled') {
    products.value = Array.isArray(prodRes.value) ? prodRes.value : prodRes.value?.data || prodRes.value?.records || []
  }
  if (matRes.status === 'fulfilled') {
    materials.value = Array.isArray(matRes.value) ? matRes.value : matRes.value?.data || matRes.value?.records || []
  }
  resetForm()
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  form.productId = ''
  form.materialId = ''
}

async function handleBind() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await bindProductMaterial({ productId: form.productId, materialId: form.materialId })
    ElMessage.success('关联成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleUnbind(row) {
  try {
    const productId = row.product?.id || row.productId
    const materialId = row.material?.id || row.materialId
    await unbindProductMaterial(productId, materialId)
    ElMessage.success('解绑成功')
    fetchList()
  } catch { /* */ }
}

async function toggleVisibility(row) {
  try {
    await toggleProductMaterialVisibility(row.id)
    ElMessage.success('操作成功')
    fetchList()
  } catch { /* */ }
}

fetchList()
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
</style>
