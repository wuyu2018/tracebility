<template>
  <section class="page-card">
    <h2 class="title">生产批次创建</h2>
    <el-form :model="form" label-position="top">
      <el-row :gutter="12">
        <el-col :md="12"><el-form-item label="productId"><el-input-number v-model="form.productId" :min="1" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="productionDate"><el-date-picker v-model="form.productionDate" type="date" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="shelfLife"><el-input v-model="form.shelfLife" placeholder="12个月" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="quantity"><el-input-number v-model="form.quantity" :min="0" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="unit"><el-input v-model="form.unit" placeholder="箱" /></el-form-item></el-col>
        <el-col :md="24"><el-form-item label="materialPurchaseIds"><el-input v-model="materialIdsText" placeholder="例如 1,2,3" /></el-form-item></el-col>
      </el-row>
      <el-button type="primary" @click="submit">提交批次</el-button>
    </el-form>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createBatch } from '../../services/api'

const materialIdsText = ref('')
const form = reactive({ productId: 1, productionDate: '', shelfLife: '', quantity: 0, unit: '', materialPurchaseIds: [] })

async function submit() {
  form.materialPurchaseIds = materialIdsText.value.split(',').map((v) => Number(v.trim())).filter((v) => Number.isFinite(v) && v > 0)
  await createBatch(form)
  ElMessage.success('生产批次创建成功')
}
</script>
