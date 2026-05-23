<template>
  <div class="page-container">
    <div class="page-header">
      <h3>产品管理</h3>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增产品
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索产品名称"
        clearable
        style="width: 280px"
        @keyup.enter="fetchList"
        @clear="fetchList"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="fetchList">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="产品名称" />
      <el-table-column prop="specification" label="规格" />
      <el-table-column prop="shelfLife" label="保质期" />
      <el-table-column prop="antiFakeCode" label="防伪码" width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" @click="handleGenerateQr(row)">生成二维码</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑产品' : '新增产品'"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification" />
        </el-form-item>
        <el-form-item label="保质期" prop="shelfLife">
          <el-input v-model="form.shelfLife" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="qrDialogVisible" title="产品二维码" width="420px" @closed="closeQrDialog">
      <div style="text-align: center;">
        <p style="margin-bottom: 16px; font-weight: 500;">{{ currentProductName }}</p>
        <canvas ref="qrCanvas" style="border: 1px solid #eee; padding: 8px;"></canvas>
        <p style="margin-top: 12px; font-size: 13px; color: #909399; word-break: break-all;">
          {{ currentQrCode }}
        </p>
      </div>
      <template #footer>
        <el-button @click="closeQrDialog">关闭</el-button>
        <el-button type="primary" @click="downloadQrCode">
          <el-icon><Download /></el-icon>下载二维码
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getProducts, createProduct, updateProduct, deleteProduct, generateQrcode } from '@/api/admin'
import { Plus, Search, Download } from '@element-plus/icons-vue'
import QRCode from 'qrcode'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  specification: '',
  shelfLife: '',
  contactPhone: '',
  contactEmail: ''
})

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格', trigger: 'blur' }]
}

const qrDialogVisible = ref(false)
const qrCanvas = ref(null)
const currentQrCode = ref('')
const currentProductName = ref('')

async function fetchList() {
  loading.value = true
  try {
    const res = await getProducts({ keyword: keyword.value })
    list.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    editingId.value = row.id
    Object.assign(form, {
      name: row.name || '',
      specification: row.specification || '',
      shelfLife: row.shelfLife || '',
      contactPhone: row.contactPhone || '',
      contactEmail: row.contactEmail || ''
    })
  } else {
    editingId.value = null
    resetForm()
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  form.name = ''
  form.specification = ''
  form.shelfLife = ''
  form.contactPhone = ''
  form.contactEmail = ''
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateProduct(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createProduct({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteProduct(id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* message handled by interceptor */ }
}

async function handleGenerateQr(row) {
  try {
    const res = await generateQrcode(row.id)
    const code = res?.codes?.[0]
    if (code) {
      currentQrCode.value = code
      currentProductName.value = row.name
      qrDialogVisible.value = true
      await nextTick()
      await QRCode.toCanvas(qrCanvas.value, code, { width: 300, margin: 2 })
      ElMessage.success('二维码生成成功')
    } else {
      ElMessage.error('二维码生成失败：未获取到防伪码')
    }
  } catch { /* */ }
}

function downloadQrCode() {
  const canvas = qrCanvas.value
  if (!canvas) return
  const link = document.createElement('a')
  link.download = `qrcode-${currentProductName.value || currentQrCode.value}.png`
  link.href = canvas.toDataURL('image/png')
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function closeQrDialog() {
  qrDialogVisible.value = false
  const canvas = qrCanvas.value
  if (canvas) {
    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, canvas.width, canvas.height)
  }
}

fetchList()
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
