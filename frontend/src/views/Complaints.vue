<template>
  <div class="complaints-page">
    <h1>投诉管理</h1>
    <p class="page-desc">查看和处理用户投诉</p>
    
    <div class="page-content">
      <el-table :data="complaints" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="antiFakeCode" label="防伪码" width="150" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="batchNumber" label="批次号" width="150" />
        <el-table-column prop="complaintReason" label="投诉原因" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllComplaints } from '../api'

const complaints = ref([])

onMounted(async () => {
  try {
    const res = await getAllComplaints()
    complaints.value = res.data || []
  } catch (err) {
    console.error('加载投诉列表失败:', err)
  }
})
</script>

<style scoped>
.complaints-page {
  padding: var(--spacing-lg);
}

.complaints-page h1 {
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
}

.page-desc {
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xl);
}

.page-content {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-md);
}
</style>
