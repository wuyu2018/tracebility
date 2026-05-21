<template>
  <div class="page-container">
    <div class="page-header">
      <h3>溯源智能体</h3>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :md="6" :sm="12">
        <el-card shadow="hover" v-loading="consensusLoading">
          <div class="stat-mini">
            <div class="stat-mini-label">共识协议</div>
            <div class="stat-mini-value">PBFT</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="agentId" label="Agent ID" width="180" show-overflow-tooltip />
      <el-table-column prop="agentType" label="类型" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ row.status === 'ACTIVE' ? '在线' : row.status || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creditScore" label="信誉分" width="100" />
      <el-table-column prop="registeredAt" label="注册时间" width="180" />
      <el-table-column prop="lastActiveAt" label="最后活跃" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row.agentId)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="Agent 详情" width="500px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="Agent ID">{{ detail.agentId }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.agentType }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
        <el-descriptions-item label="信誉分">{{ detail.creditScore }}</el-descriptions-item>
        <el-descriptions-item label="证书序列号">{{ detail.certificateSerial || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公钥">
          <div style="font-family:monospace;font-size:11px;word-break:break-all;max-width:350px">
            {{ detail.publicKey || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detail.registeredAt }}</el-descriptions-item>
        <el-descriptions-item label="最后活跃">{{ detail.lastActiveAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getAgents, getAgent, getReputationList } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const consensusLoading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)

async function fetchList() {
  loading.value = true
  try {
    const res = await getAgents()
    list.value = Array.isArray(res) ? res : res?.data || res?.records || []
  } finally { loading.value = false }
}

async function showDetail(agentId) {
  detailVisible.value = true
  detail.value = null
  try {
    const res = await getAgent(agentId)
    detail.value = res?.data || res
  } catch { /* */ }
}

fetchList()
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
.stat-mini { text-align: center; }
.stat-mini-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-mini-value { font-size: 24px; font-weight: 700; color: #409eff; }
</style>
