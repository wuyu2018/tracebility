<template>
  <section class="page-card manage-page">
    <div class="panel-header">
      <div>
        <h2 class="title">原料采购录入</h2>
        <p class="panel-desc">按后端字段提交采购记录到 `/api/v2/material-purchases`。</p>
      </div>
    </div>
    <el-form :model="form" label-position="top" class="form-grid">
      <el-row :gutter="12">
        <el-col :md="12"><el-form-item label="materialId"><el-input-number v-model="form.materialId" :min="1" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="batchNumber"><el-input v-model="form.batchNumber" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="supplierName"><el-input v-model="form.supplierName" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="producerName"><el-input v-model="form.producerName" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="producerAddress"><el-input v-model="form.producerAddress" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="purchaseDate"><el-date-picker v-model="form.purchaseDate" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="quantity"><el-input-number v-model="form.quantity" :min="0" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="unit"><el-input v-model="form.unit" /></el-form-item></el-col>
      </el-row>
      <div class="form-actions">
        <el-button type="primary" @click="submit">提交采购记录</el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createMaterialPurchase } from '../../services/api'

const form = reactive({
  materialId: 1, batchNumber: '', supplierName: '', producerName: '', producerAddress: '',
  purchaseDate: '', quantity: 0, unit: 'kg'
})

async function submit() {
  await createMaterialPurchase(form)
  ElMessage.success('采购记录提交成功')
}
</script>
