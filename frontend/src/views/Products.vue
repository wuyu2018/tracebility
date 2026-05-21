<template>
  <section class="page-card">
    <h2 class="title">产品管理</h2>
    <el-form :model="form" label-position="top">
      <el-row :gutter="12">
        <el-col :md="12"><el-form-item label="name"><el-input v-model="form.name" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="specification"><el-input v-model="form.specification" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="shelfLife"><el-input v-model="form.shelfLife" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="imageUrl"><el-input v-model="form.imageUrl" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="contactPhone"><el-input v-model="form.contactPhone" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="contactEmail"><el-input v-model="form.contactEmail" /></el-form-item></el-col>
      </el-row>
      <el-button type="primary" @click="submit">创建产品</el-button>
    </el-form>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createProduct } from '../services/api'

const form = reactive({ name: '', specification: '', shelfLife: '', imageUrl: '', contactPhone: '', contactEmail: '' })

async function submit() {
  await createProduct(form)
  ElMessage.success('产品创建成功')
}
</script>
