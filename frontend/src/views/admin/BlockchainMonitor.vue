<template>
  <div class="page-container">
    <div class="page-header">
      <h3>区块链监控</h3>
    </div>

    <el-row :gutter="20">
      <el-col :md="12" :sm="24">
        <el-card header="区块链概览" shadow="hover" v-loading="loading">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="链类型">{{ summary.chainType || summary.chain_type || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最新区块哈希">
              <div class="hash-text" :title="summary.latestBlockHash || summary.latest_block_hash">
                {{ truncate(summary.latestBlockHash || summary.latest_block_hash) || '-' }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="默克尔根">
              <div class="hash-text" :title="summary.merkleRoot || summary.merkle_root">
                {{ truncate(summary.merkleRoot || summary.merkle_root) || '-' }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="总交易数">{{ summary.totalTransactions ?? summary.total_transactions ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="锚定记录数">{{ summary.anchorCount ?? summary.anchor_count ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="区块高度">{{ summary.blockHeight ?? summary.block_height ?? '-' }}</el-descriptions-item>
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
  chainType: '',
  latestBlockHash: '',
  merkleRoot: '',
  totalTransactions: null,
  anchorCount: null,
  blockHeight: null
})

function truncate(str) {
  if (!str) return ''
  return str.length > 30 ? str.slice(0, 15) + '...' + str.slice(-15) : str
}

onMounted(async () => {
  try {
    const res = await getBlockchainSummary()
    if (res) {
      summary.chainType = res.chainType || res.chain_type || ''
      summary.latestBlockHash = res.latestBlockHash || res.latest_block_hash || ''
      summary.merkleRoot = res.merkleRoot || res.merkle_root || ''
      summary.totalTransactions = res.totalTransactions ?? res.total_transactions ?? null
      summary.anchorCount = res.anchorCount ?? res.anchor_count ?? null
      summary.blockHeight = res.blockHeight ?? res.block_height ?? null
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
.hash-text { font-family: monospace; font-size: 12px; word-break: break-all; max-width: 300px; cursor: pointer; }
.key-box { padding: 12px 0; }
.key-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.key-value { font-family: monospace; font-size: 11px; word-break: break-all; background: #f5f7fa; padding: 12px; border-radius: 6px; line-height: 1.6; }
</style>
