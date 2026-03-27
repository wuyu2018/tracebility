<template>
  <div class="complaint-container">
    <el-card class="complaint-card" shadow="always">
      <template #header>
        <div class="card-header">
          <h2>产品投诉</h2>
        </div>
      </template>

      <el-form
        ref="complaintFormRef"
        :model="complaintForm"
        :rules="complaintRules"
        label-width="120px"
        class="complaint-form"
        @submit.prevent="submitComplaint"
      >
        <el-form-item label="选择产品" prop="productId">
          <ProductSelector
            v-model="complaintForm.productId"
            role="consumer"
            @change="handleProductChange"
          />
          <div class="form-tip">必填项，请选择产品</div>
        </el-form-item>

        <el-form-item label="投诉原因" prop="complaintReason">
          <el-input
            v-model="complaintForm.complaintReason"
            type="textarea"
            :rows="5"
            placeholder="请输入详细的投诉原因"
            maxlength="500"
            show-word-limit
            clearable
          />
          <div class="form-tip">必填项，请详细描述投诉原因</div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            @click="submitComplaint"
          >
            {{ submitting ? '提交中...' : '提交投诉' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 错误提示 -->
      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="true"
        @close="errorMessage = ''"
        class="error-alert"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import ProductSelector from '@/components/ProductSelector.vue'

const complaintFormRef = ref()
const complaintForm = reactive({
  productId: '',
  complaintReason: '',
  complaintTime: ''
})
const complaintRules = {
  productId: [
    { required: true, message: '请选择产品', trigger: 'change' }
  ],
  complaintReason: [
    { required: true, message: '投诉原因不能为空', trigger: 'blur' }
  ]
}
const submitting = ref(false)
const errorMessage = ref('')
const API_BASE_URL = '/api'
const COMPLAINT_API = `${API_BASE_URL}/complaint`

const handleProductChange = (product) => {
  if (product) {
    complaintForm.productId = product.id
  } else {
    complaintForm.productId = ''
  }
}

const submitComplaint = async () => {
  try {
    const valid = await complaintFormRef.value.validate()
    if (!valid) return

    submitting.value = true
    errorMessage.value = ''

    const requestData = {
      productId: complaintForm.productId,
      complaintReason: complaintForm.complaintReason.trim()
    }

    if (complaintForm.complaintTime) {
      requestData.complaintTime = complaintForm.complaintTime
    }

    const response = await axios.post(COMPLAINT_API, requestData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (response.status === 200 || response.status === 201) {
      ElMessage.success({
        message: '投诉提交成功！',
        duration: 3000
      })

      await ElMessageBox.alert(
        `投诉已成功提交！\n投诉ID: ${response.data.id || 'N/A'}\n处理时间: ${new Date().toLocaleString()}`,
        '提交成功',
        {
          confirmButtonText: '确定',
          type: 'success'
        }
      )

      resetForm()
    }
  } catch (error) {
    handleApiError(error)
  } finally {
    submitting.value = false
  }
}

const handleApiError = (error) => {
  if (error.response) {
    const { status, data } = error.response

    switch (status) {
      case 400:
        if (data && data.errors) {
          try {
            if (typeof data.errors === 'object' && !Array.isArray(data.errors) && data.errors !== null) {
              const errorMessages = Object.entries(data.errors)
                .map(([field, message]) => `${field}: ${message}`)
                .join('\n')
              errorMessage.value = `验证错误：\n${errorMessages}`
            } else if (Array.isArray(data.errors)) {
              const errorMessages = data.errors
                .map(err => `${err.field || err}: ${err.message || err}`)
                .join('\n')
              errorMessage.value = `验证错误：\n${errorMessages}`
            } else {
              errorMessage.value = data.message || '请求参数有误，请检查填写内容'
            }
          } catch (e) {
            errorMessage.value = data.message || '请求参数有误，请检查填写内容'
          }
        } else if (data && data.message) {
          errorMessage.value = data.message
        } else {
          errorMessage.value = '请求参数有误，请检查填写内容'
        }
        break

      case 404:
        errorMessage.value = '未找到相关资源，请检查产品名称是否正确'
        break

      case 409:
        errorMessage.value = '该产品已存在投诉记录'
        break

      case 500:
        errorMessage.value = '服务器内部错误，请稍后重试'
        break

      default:
        errorMessage.value = `请求失败：${status} ${data?.message || error.message || '未知错误'}`
    }
  } else if (error.request) {
    errorMessage.value = '服务器无响应，请检查网络连接'
  } else {
    errorMessage.value = `请求配置错误：${error.message || '未知配置错误'}`
  }
  ElMessage.error({
    message: errorMessage.value,
    duration: 5000
  })
}

const resetForm = () => {
  if (complaintFormRef.value) {
    complaintFormRef.value.resetFields()
  }
  errorMessage.value = ''
  complaintForm.complaintTime = ''
  complaintForm.productId = ''
}


</script>

<style scoped>
.complaint-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem 1.5rem 4rem;
  padding-bottom: max(4rem, env(safe-area-inset-bottom));
}

@media (max-width: 768px) {
  .complaint-container {
    padding: 1.5rem 1rem 3rem;
  }
}

@media (max-width: 480px) {
  .complaint-container {
    padding: 1rem 0.875rem 2.5rem;
  }
}

.complaint-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

:deep(.el-card__header) {
  background: var(--color-primary);
  color: white;
  padding: 1.25rem 1.5rem;
}

:deep(.el-card__body) {
  padding: 1.5rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: white;
}

.complaint-form {
  margin-top: 1.25rem;
}

.form-tip {
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin-top: 0.35rem;
}

.error-alert {
  margin-top: 1.25rem;
}

@media (max-width: 768px) {
  .complaint-container {
    padding: 1rem;
  }

  :deep(.el-form-item) {
    margin-bottom: 1.25rem;
  }

  :deep(.el-form-item__label) {
    text-align: left !important;
    padding-right: 0.5rem;
  }
}
</style>