<template>
  <section class="page-card">
    <h2 class="title">区块链监控</h2>
    <el-button @click="load">刷新状态</el-button>
    <pre v-if="json" class="json">{{ json }}</pre>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBlockchainMonitorSummary } from '../services/api'

const json = ref('')

async function load() {
  const data = await getBlockchainMonitorSummary()
  json.value = JSON.stringify(data, null, 2)
}

onMounted(load)
</script>

<style scoped>
.json { white-space: pre-wrap; background:#0f172a; color:#e2e8f0; padding:12px; border-radius:10px; margin-top:12px; }
</style>
