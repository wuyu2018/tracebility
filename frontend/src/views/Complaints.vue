<template>
  <section class="page-card">
    <h2 class="title">投诉管理</h2>
    <el-button @click="load">刷新列表</el-button>
    <el-table :data="rows" style="margin-top:12px;" v-loading="loading">
      <el-table-column prop="antiFakeCode" label="防伪码" min-width="160" />
      <el-table-column prop="productName" label="产品" min-width="140" />
      <el-table-column prop="batchNumber" label="批次" min-width="160" />
      <el-table-column prop="complaintReason" label="投诉原因" min-width="220" />
    </el-table>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllComplaintInfo } from '../services/api'

const rows = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    rows.value = await getAllComplaintInfo()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
