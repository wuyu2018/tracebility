<template>
  <div class="complaint-page">
    <h1>我要投诉</h1>
    <p class="page-desc">如果您对产品有疑问或投诉，请填写以下信息</p>
    
    <el-form :model="form" label-width="100px" class="complaint-form">
      <el-form-item label="防伪码" required>
        <el-input v-model="form.antiFakeCode" placeholder="请输入产品防伪码" />
      </el-form-item>
      
      <el-form-item label="产品名称" required>
        <el-input v-model="form.productName" placeholder="请输入产品名称" />
      </el-form-item>
      
      <el-form-item label="批次号">
        <el-input v-model="form.batchNumber" placeholder="请输入批次号（选填）" />
      </el-form-item>
      
      <el-form-item label="投诉原因" required>
        <el-radio-group v-model="form.complaintReason">
          <el-radio label="product_expired">产品已过期</el-radio>
          <el-radio label="package_damaged">包装破损</el-radio>
          <el-radio label="quality_issue">质量问题</el-radio>
          <el-radio label="code_abnormal">防伪码异常</el-radio>
          <el-radio label="other">其他</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="补充说明">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          placeholder="请详细描述您的问题"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交投诉
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitComplaint } from '../api'

const form = reactive({
  antiFakeCode: '',
  productName: '',
  batchNumber: '',
  complaintReason: 'quality_issue',
  description: ''
})

const submitting = ref(false)

async function handleSubmit() {
  if (!form.antiFakeCode || !form.productName) {
    ElMessage.warning('请填写防伪码和产品名称')
    return
  }
  
  submitting.value = true
  try {
    await submitComplaint(form)
    ElMessage.success('投诉提交成功')
    form.antiFakeCode = ''
    form.productName = ''
    form.batchNumber = ''
    form.description = ''
  } catch (err) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.complaint-page {
  max-width: 600px;
  margin: 0 auto;
  padding: var(--spacing-2xl) var(--spacing-lg);
}

.complaint-page h1 {
  text-align: center;
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
}

.page-desc {
  text-align: center;
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xl);
}

.complaint-form {
  background: var(--color-bg-card);
  padding: var(--spacing-xl);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}
</style>
