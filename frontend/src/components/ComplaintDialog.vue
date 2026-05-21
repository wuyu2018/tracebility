<template>
  <div class="complaint-dialog">
    <div class="complaint-overlay" @click.self="$emit('close')"></div>
    <div class="complaint-modal">
      <div class="complaint-header">
        <h3>提交投诉</h3>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>
      
      <div class="complaint-body">
        <div class="info-section">
          <div class="info-item">
            <span class="label">防伪码</span>
            <span class="value">{{ complaintData.antiFakeCode }}</span>
          </div>
          <div class="info-item">
            <span class="label">产品</span>
            <span class="value">{{ complaintData.productName }}</span>
          </div>
          <div class="info-item" v-if="complaintData.batchNumber">
            <span class="label">批次号</span>
            <span class="value">{{ complaintData.batchNumber }}</span>
          </div>
        </div>

        <el-form :model="form" label-width="80px" class="complaint-form">
          <el-form-item label="投诉原因" required>
            <el-radio-group v-model="form.reason">
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
              :rows="4"
              placeholder="请详细描述您的问题（选填）"
            />
          </el-form-item>
        </el-form>
      </div>

      <div class="complaint-footer">
        <button class="btn-cancel" @click="$emit('close')">取消</button>
        <button class="btn-submit" @click="handleSubmit" :disabled="submitting">
          {{ submitting ? '提交中...' : '提交投诉' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitComplaint } from '../api'

const props = defineProps({
  complaintData: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'submitted'])

const form = reactive({
  reason: 'quality_issue',
  description: ''
})

const submitting = ref(false)

async function handleSubmit() {
  if (!form.reason) {
    ElMessage.warning('请选择投诉原因')
    return
  }

  submitting.value = true
  try {
    await submitComplaint({
      antiFakeCode: props.complaintData.antiFakeCode,
      complaintReason: form.reason,
      productName: props.complaintData.productName,
      batchNumber: props.complaintData.batchNumber || '',
      description: form.description
    })

    ElMessage.success('投诉提交成功')
    emit('submitted')
  } catch (err) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.complaint-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
}

.complaint-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.complaint-modal {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-xl);
}

.complaint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.complaint-header h3 {
  font-size: 1.1rem;
  font-weight: 600;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: var(--color-text-muted);
  padding: 0.25rem;
}

.btn-close:hover {
  color: var(--color-text);
}

.complaint-body {
  padding: var(--spacing-lg);
}

.info-section {
  background: var(--color-bg);
  border-radius: var(--radius);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: var(--spacing-xs) 0;
}

.info-item .label {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.info-item .value {
  font-weight: 500;
}

.complaint-form {
  margin-top: var(--spacing-md);
}

.complaint-footer {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
  justify-content: flex-end;
}

.btn-cancel, .btn-submit {
  padding: 0.75rem 1.5rem;
  border-radius: var(--radius);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.btn-cancel:hover {
  background: var(--color-border-light);
}

.btn-submit {
  background: var(--color-primary);
  border: none;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: var(--color-primary-light);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .complaint-modal {
    width: 95%;
  }
  
  .complaint-header, .complaint-body, .complaint-footer {
    padding: var(--spacing-md);
  }
}
</style>
