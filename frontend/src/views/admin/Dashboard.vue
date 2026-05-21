<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :md="6" :sm="12" v-for="card in cards" :key="card.label">
        <el-card class="stat-card" shadow="hover" v-loading="card.loading">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon :size="28" color="#fff"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :md="12" :sm="24">
        <el-card header="区块链状态" shadow="hover" v-loading="blockchainLoading">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="总体健康">
              <el-tag :type="blockchainData.overallHealthy ? 'success' : 'danger'" size="small">
                {{ blockchainData.overallHealthy ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="原料链区块">{{ blockchainData.materialBlocks ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="批次链总区块">{{ blockchainData.batchBlocks ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="损坏区块">{{ blockchainData.brokenCount ?? '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :md="12" :sm="24">
        <el-card header="Agent 系统状态" shadow="hover" v-loading="agentLoading">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="Agent 总数">{{ agentData.total ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="在线 Agent">{{ agentData.active ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="共识协议">PBFT</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProducts, getBatches, getComplaints, getAgents, getBlockchainSummary } from '@/api/admin'
import { Goods, Box, WarningFilled, Monitor } from '@element-plus/icons-vue'

const productCount = ref(0)
const batchCount = ref(0)
const complaintCount = ref(0)
const agentCount = ref(0)
const productLoading = ref(true)
const batchLoading = ref(true)
const complaintLoading = ref(true)
const agentLoading = ref(true)
const blockchainLoading = ref(true)

const blockchainData = reactive({
  overallHealthy: false,
  materialBlocks: 0,
  batchBlocks: 0,
  brokenCount: 0
})

const agentData = reactive({
  total: 0,
  active: 0
})

const cards = [
  { label: '产品总数', value: productCount, icon: Goods, color: '#409eff', loading: productLoading },
  { label: '生产批次', value: batchCount, icon: Box, color: '#67c23a', loading: batchLoading },
  { label: '待处理投诉', value: complaintCount, icon: WarningFilled, color: '#f56c6c', loading: complaintLoading },
  { label: '溯源智能体', value: agentCount, icon: Monitor, color: '#e6a23c', loading: agentLoading }
]

onMounted(async () => {
  const [prods, batches, complaints, agents, blockchain] = await Promise.allSettled([
    getProducts({ keyword: '' }),
    getBatches({}),
    getComplaints(),
    getAgents(),
    getBlockchainSummary()
  ])

  if (prods.status === 'fulfilled') {
    const data = prods.value
    productCount.value = Array.isArray(data) ? data.length : data?.data?.length || data?.total || 0
  }
  if (batches.status === 'fulfilled') {
    const data = batches.value
    batchCount.value = Array.isArray(data) ? data.length : data?.data?.length || data?.total || 0
  }
  if (complaints.status === 'fulfilled') {
    const data = complaints.value
    complaintCount.value = Array.isArray(data) ? data.length : data?.data?.length || data?.total || 0
  }
  if (agents.status === 'fulfilled') {
    const data = agents.value
    const list = data?.agents || (Array.isArray(data) ? data : [])
    agentCount.value = list.length
    agentData.total = list.length
    agentData.active = list.filter(a => a.state === 'ACTIVE').length
  }
  if (blockchain.status === 'fulfilled') {
    const data = blockchain.value
    if (data) {
      blockchainData.overallHealthy = data.overallHealthy || false
      blockchainData.materialBlocks = data.materialChain?.blockCount ?? 0
      blockchainData.batchBlocks = data.batchChains?.totalBlockCount ?? 0
      blockchainData.brokenCount = data.batchChains?.brokenCount ?? 0
    }
  }

  productLoading.value = false
  batchLoading.value = false
  complaintLoading.value = false
  agentLoading.value = false
  blockchainLoading.value = false
})
</script>

<style scoped>
.dashboard {
  min-height: calc(100vh - 140px);
}

.stat-card {
  margin-bottom: 0;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
</style>
