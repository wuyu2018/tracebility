<template>
  <div class="page-container">
    <div class="page-header">
      <h3>区块链监控</h3>
    </div>

    <el-row :gutter="20">
      <el-col :md="12" :sm="24">
        <el-card header="区块链概览" shadow="hover" v-loading="loading">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总体健康">
              <el-tag :type="summary.overallHealthy ? 'success' : 'danger'" size="small">
                {{ summary.overallHealthy ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="原料链区块数">{{ summary.materialBlocks ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="原料链完整">
              <el-tag :type="summary.materialIntact ? 'success' : 'danger'" size="small">
                {{ summary.materialIntact ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="原料链最近锚定">{{ summary.materialAnchorDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="批次链总数">{{ summary.totalBatches ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="完好批次">{{ summary.intactBatches ?? '-' }} / {{ summary.totalBatches ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="批次链总区块">{{ summary.batchBlocks ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="损坏区块">{{ summary.brokenCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="批次链最近锚定">{{ summary.batchAnchorDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最后更新">{{ summary.lastUpdated || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :md="12" :sm="24">
        <el-card header="公钥信息" shadow="hover" v-loading="pkLoading">
          <div class="key-box">
            <div class="key-label">区块链公钥</div>
            <div class="key-value">{{ publicKey || '未获取' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBlockchainSummary, getBlockchainPublicKey } from '@/api/admin'

const loading = ref(true)
const pkLoading = ref(true)
const publicKey = ref('')

const summary = reactive({
  overallHealthy: false,
  materialBlocks: 0,
  materialIntact: false,
  materialAnchorDate: '',
  totalBatches: 0,
  intactBatches: 0,
  batchBlocks: 0,
  brokenCount: 0,
  batchAnchorDate: '',
  lastUpdated: ''
})

onMounted(async () => {
  try {
    const res = await getBlockchainSummary()
    if (res) {
      summary.overallHealthy = res.overallHealthy || false
      summary.materialBlocks = res.materialChain?.blockCount ?? 0
      summary.materialIntact = res.materialChain?.intact || false
      summary.materialAnchorDate = res.materialChain?.lastAnchorDate || ''
      summary.totalBatches = res.batchChains?.totalBatches ?? 0
      summary.intactBatches = res.batchChains?.intactCount ?? 0
      summary.batchBlocks = res.batchChains?.totalBlockCount ?? 0
      summary.brokenCount = res.batchChains?.brokenCount ?? 0
      summary.batchAnchorDate = res.batchChains?.lastAnchorDate || ''
      summary.lastUpdated = res.lastUpdated || ''
    }
  } finally { loading.value = false }

  try {
    const res = await getBlockchainPublicKey()
    publicKey.value = typeof res === 'string' ? res : res?.publicKey || res?.data || JSON.stringify(res)
  } finally { pkLoading.value = false }
})
</script>

<style scoped>
.page-container { min-height: calc(100vh - 140px); }
.page-header { margin-bottom: 20px; }
.page-header h3 { font-size: 20px; color: #303133; }
.key-box { padding: 12px 0; }
.key-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.key-value { font-family: monospace; font-size: 11px; word-break: break-all; background: #f5f7fa; padding: 12px; border-radius: 6px; line-height: 1.6; }
</style>
