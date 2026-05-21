<template>
  <section class="page-card">
    <h2 class="title">质检报告录入</h2>
    <el-form :model="form" label-position="top">
      <el-row :gutter="12">
        <el-col :md="12"><el-form-item label="batchId"><el-input-number v-model="form.batchId" :min="1" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="sampleName"><el-input v-model="form.sampleName" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="sampleQuantity"><el-input-number v-model="form.sampleQuantity" :min="0" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="sampleSpecification"><el-input v-model="form.sampleSpecification" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="imageUrl"><el-input v-model="form.imageUrl" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="inspectorName"><el-input v-model="form.inspectorName" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="qualified"><el-switch v-model="form.qualified" /></el-form-item></el-col>
        <el-col :md="24"><el-form-item label="failReason"><el-input v-model="form.failReason" type="textarea" /></el-form-item></el-col>
      </el-row>
      <el-button type="primary" @click="submit">提交质检报告</el-button>
    </el-form>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { completeInspection } from '../../services/api'

const form = reactive({
  batchId: 1, sampleName: '', sampleQuantity: 0, sampleSpecification: '', imageUrl: '', inspectorName: '', qualified: true, failReason: ''
})

async function submit() {
  if (form.qualified) form.failReason = ''
  await completeInspection(form)
  ElMessage.success('质检报告提交成功')
}
</script>
