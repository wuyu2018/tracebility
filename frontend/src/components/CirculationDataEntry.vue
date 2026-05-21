<template>
  <div class="circulation-entry">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h1>运输数据录入</h1>
          <p class="subtitle">为生产批次绑定运输信息</p>
        </div>
      </template>

      <div class="step-content">
        <div class="step-panel">
          <h2>选择批次</h2>
          <el-form label-width="120px">
            <el-form-item label="生产批次" required>
              <el-select v-model="form.batchId" placeholder="请选择批次" filterable style="width:100%">
                <el-option v-for="b in batches" :key="b.id" :label="`${b.batchNumber} - ${b.productName || ''}`" :value="b.id" />
              </el-select>
            </el-form-item>
          </el-form>

          <el-divider>运输信息</el-divider>

          <el-form :model="form" label-width="140px" class="entry-form">
            <el-form-item label="运输公司" required>
              <el-input v-model="form.transportCompany" placeholder="运输公司名称" />
            </el-form-item>
            <el-form-item label="车牌号">
              <el-input v-model="form.vehicleNumber" placeholder="车牌号" />
            </el-form-item>
            <el-form-item label="记录人">
              <el-input v-model="form.recorderName" placeholder="记录人姓名" />
            </el-form-item>
          </el-form>

          <div class="form-actions">
            <el-button type="primary" @click="submitTransport" :loading="loading" :disabled="!form.batchId">保存运输信息</el-button>
            <el-button @click="resetForm">重置</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'

const API_BASE = ''
const loading = ref(false)
const batches = ref([])

const form = reactive({
  batchId: null,
  transportCompany: '',
  vehicleNumber: '',
  recorderName: ''
})

async function loadBatches() {
  try {
    const res = await axios.get(`${API_BASE}/batches`)
    batches.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function submitTransport() {
  if (!form.batchId) {
    ElMessage.warning('请选择生产批次')
    return
  }
  if (!form.transportCompany) {
    ElMessage.warning('请输入运输公司名称')
    return
  }
  loading.value = true
  try {
    await axios.post(`${API_BASE}/v2/transport-sales`, {
      batchId: form.batchId,
      transportCompany: form.transportCompany,
      vehicleNumber: form.vehicleNumber || '',
      recorderName: form.recorderName || ''
    })
    ElMessage.success('运输信息已保存')
    resetForm()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '保存失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.batchId = null
  form.transportCompany = ''
  form.vehicleNumber = ''
  form.recorderName = ''
}

onMounted(() => {
  loadBatches()
})
</script>

<style scoped>
.circulation-entry {
  padding: 1.5rem;
  max-width: 800px;
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

.step-content {
  min-height: 300px;
}

.step-panel h2 {
  margin: 0 0 1.5rem;
  font-size: 1.2rem;
  color: var(--color-primary);
}

.entry-form {
  max-width: 500px;
}

.form-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}
</style>
