<template>
  <section class="container" style="padding:24px 0;">
    <div class="page-card" style="max-width:700px;">
      <h1 class="title">投诉提交</h1>
      <el-form :model="form" label-position="top">
        <el-form-item label="防伪码"><el-input v-model="form.antiFakeCode" /></el-form-item>
        <el-form-item label="产品名"><el-input v-model="form.productName" /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="form.batchNumber" /></el-form-item>
        <el-form-item label="投诉原因"><el-input v-model="form.complaintReason" type="textarea" /></el-form-item>
        <el-button type="primary" @click="submit">提交</el-button>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createComplaint } from '../services/api'

const form = reactive({ antiFakeCode: '', complaintReason: '', productName: '', batchNumber: '' })

async function submit() {
  await createComplaint(form)
  ElMessage.success('投诉已提交')
}
</script>
