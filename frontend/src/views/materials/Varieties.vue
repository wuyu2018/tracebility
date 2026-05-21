<template>
  <section class="page-card">
    <h2 class="title">原料品种管理</h2>
    <el-form :model="form" inline>
      <el-form-item label="品种名称"><el-input v-model="form.name" placeholder="如：有机生牛乳" /></el-form-item>
      <el-form-item><el-button type="primary" @click="submit">提交到后端</el-button></el-form-item>
    </el-form>
    <p class="subtitle">提交接口：POST /api/v2/material-varieties，字段：name</p>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createMaterialVariety } from '../../services/api'

const form = reactive({ name: '' })
async function submit() {
  if (!form.name) return
  await createMaterialVariety(form)
  ElMessage.success('原料品种创建成功')
  form.name = ''
}
</script>
