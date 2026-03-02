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
        <el-form-item label="产品ID" prop="productId">
          <el-input
            v-model.number="complaintForm.productId"
            placeholder="请输入产品ID"
            type="number"
            min="1"
            clearable
          />
          <div class="form-tip">必填项，请输入有效的产品ID</div>
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const complaintFormRef = ref()

const complaintForm = reactive({
  productId: '',
  complaintReason: '',
  complaintTime: ''
})

const complaintRules = {
  productId: [
    { required: true, message: '产品ID不能为空', trigger: 'blur' },
    {
      type: 'number',
      message: '产品ID必须为数字',
      trigger: 'blur',
      transform: value => Number(value)
    },
    {
      validator: (rule, value, callback) => {
        if (value <= 0) {
          callback(new Error('产品ID必须大于0'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  complaintReason: [
    { required: true, message: '投诉原因不能为空', trigger: 'blur' },
    {
      min: 5,
      message: '投诉原因至少5个字符',
      trigger: 'blur'
    },
    {
      max: 500,
      message: '投诉原因不能超过500个字符',
      trigger: 'blur'
    }
  ]
}

const submitting = ref(false)
const errorMessage = ref('')

const API_BASE_URL = 'http://localhost:8080/api'
const COMPLAINT_API = `${API_BASE_URL}/complaint`

const submitComplaint = async () => {
  try {
    const valid = await complaintFormRef.value.validate()
    if (!valid) return

    submitting.value = true
    errorMessage.value = ''

    const requestData = {
      productId: Number(complaintForm.productId),
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
          const errorMessages = data.errors
            .map(err => `${err.field}: ${err.message}`)
            .join('\n')
          errorMessage.value = `验证错误：\n${errorMessages}`
        } else if (data && data.message) {
          errorMessage.value = data.message
        } else {
          errorMessage.value = '请求参数有误，请检查填写内容'
        }
        break

      case 404:
        errorMessage.value = '未找到相关资源，请检查产品ID是否正确'
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
}

const props = defineProps({
  productId: {
    type: Number,
    default: null
  }
})

if (props.productId) {
  complaintForm.productId = props.productId
}
</script>

<style scoped>
.complaint-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.complaint-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
}

.complaint-form {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.error-alert {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .complaint-container {
    padding: 10px;
    margin: 10px;
  }

  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-form-item__label) {
    text-align: left !important;
    padding-right: 10px;
  }
}
</style>