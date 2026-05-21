<template>
  <section class="page-card">
    <h2 class="title">防伪码生成</h2>
    <el-form inline>
      <el-form-item label="batchId"><el-input-number v-model="batchId" :min="1" /></el-form-item>
      <el-form-item label="quantity"><el-input-number v-model="quantity" :min="1" /></el-form-item>
      <el-form-item><el-button type="primary" @click="submit">生成</el-button></el-form-item>
    </el-form>
    <p class="subtitle" v-if="count">本次生成数量：{{ count }}</p>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generateSecurityCodes } from '../services/api'

const batchId = ref(1)
const quantity = ref(10)
const count = ref(0)

async function submit() {
  const data = await generateSecurityCodes(batchId.value, quantity.value)
  count.value = data?.count || 0
  ElMessage.success('防伪码生成成功')
}
</script>
