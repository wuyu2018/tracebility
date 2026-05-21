<template>
  <section class="page-card">
    <h2 class="title">仓储录入</h2>
    <el-form :model="form" label-position="top">
      <el-row :gutter="12">
        <el-col :md="12"><el-form-item label="batchId"><el-input-number v-model="form.batchId" :min="1" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="quantity"><el-input-number v-model="form.quantity" :min="0" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="unit"><el-input v-model="form.unit" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="warehouseLocation"><el-input v-model="form.warehouseLocation" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="storageTime"><el-date-picker v-model="form.storageTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="outboundTime"><el-date-picker v-model="form.outboundTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item></el-col>
      </el-row>
      <el-button type="primary" @click="submit">提交仓储记录</el-button>
    </el-form>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { recordStorage } from '../../services/api'

const form = reactive({ batchId: 1, storageTime: '', outboundTime: '', quantity: 0, unit: '', warehouseLocation: '' })

async function submit() {
  await recordStorage(form)
  ElMessage.success('仓储记录提交成功')
}
</script>
