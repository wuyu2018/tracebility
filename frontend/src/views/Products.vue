<template>
  <div class="products-page">
    <h1>产品管理</h1>
    <p class="page-desc">管理产品信息和生成防伪二维码</p>
    
    <div class="page-content">
      <el-table :data="products" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="产品名称" />
        <el-table-column prop="specification" label="规格" />
        <el-table-column prop="shelfLife" label="保质期" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small">编辑</el-button>
            <el-button type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProducts } from '../api'

const products = ref([])

onMounted(async () => {
  try {
    const res = await getProducts()
    products.value = res.data || []
  } catch (err) {
    console.error('加载产品列表失败:', err)
  }
})
</script>

<style scoped>
.products-page {
  padding: var(--spacing-lg);
}

.products-page h1 {
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
