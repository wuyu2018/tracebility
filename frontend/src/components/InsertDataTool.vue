<template>
  <div class="insert-data-tool">
    <el-card class="tool-card">
      <template #header>
        <div class="card-header">
          <div>
            <h1 class="main-title">📊 数据录入</h1>
            <h2 class="sub-title">数据录入工具</h2>
          </div>
          <div class="header-tips">
            <span class="required-tip">⭑ 请填写完整必填项</span>
            <span class="type-tip">请选择要录入的数据类型</span>
          </div>
        </div>
      </template>

      <!-- 数据类型切换标签页 -->
      <el-tabs v-model="activeEntity" type="border-card" class="entity-tabs">
        <!-- 1. 产品 -->
        <el-tab-pane label="产品" name="product">
          <div class="table-wrapper">
            <table class="data-entry-table">
              <tbody>
                <tr v-for="field in productFields" :key="field.prop">
                  <td class="field-label" :class="{ required: field.required }">
                    {{ field.label }}
                  </td>
                  <td class="field-icon">
                    <el-icon v-if="field.type === 'date'"><Clock /></el-icon>
                    <el-icon v-else><Minus /></el-icon>
                  </td>
                  <td class="field-input">
                    <el-input
                      v-if="field.type === 'text'"
                      v-model="productForm[field.prop]"
                      :placeholder="field.placeholder"
                      :disabled="field.disabled"
                      clearable
                    />
                    <el-input-number
                      v-else-if="field.type === 'number'"
                      v-model="productForm[field.prop]"
                      :min="field.min ?? 0"
                      :max="field.max"
                      :precision="field.precision"
                      :placeholder="field.placeholder"
                      controls-position="right"
                    />
                    <el-date-picker
                      v-else-if="field.type === 'date'"
                      v-model="productForm[field.prop]"
                      type="date"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      placeholder="选择日期"
                    />
                  </td>
                  <td class="field-action">
                    <el-button type="text" size="small" @click="handlePlus(field)">
                      +
                    </el-button>
                  </td>
                  <td class="field-unit">
                    <span v-if="field.unit">{{ field.unit }}</span>
                    <span v-else-if="field.tip" class="field-tip">{{ field.tip }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>

        <!-- 2. 原材料采购 -->
        <el-tab-pane label="原材料采购" name="materialPurchase">
          <div class="table-wrapper">
            <table class="data-entry-table">
              <tbody>
                <tr v-for="field in materialFields" :key="field.prop">
                  <td class="field-label" :class="{ required: field.required }">{{ field.label }}</td>
                  <td class="field-icon"><el-icon><Minus /></el-icon></td>
                  <td class="field-input">
                    <el-input-number
                      v-if="field.type === 'number'"
                      v-model="materialForm[field.prop]"
                      :min="1"
                      controls-position="right"
                    />
                    <el-input v-else v-model="materialForm[field.prop]" :placeholder="field.placeholder" />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit">{{ field.unit || '' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>

        <!-- 3. 检验检测 -->
        <el-tab-pane label="检验检测" name="inspection">
          <div class="table-wrapper">
            <table class="data-entry-table">
              <tbody>
                <tr v-for="field in inspectionFields" :key="field.prop">
                  <td class="field-label" :class="{ required: field.required }">{{ field.label }}</td>
                  <td class="field-icon"><el-icon><Minus /></el-icon></td>
                  <td class="field-input">
                    <el-input-number
                      v-if="field.type === 'number'"
                      v-model="inspectionForm[field.prop]"
                      :min="0"
                      controls-position="right"
                    />
                    <el-input v-else v-model="inspectionForm[field.prop]" :placeholder="field.placeholder" />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit">{{ field.unit || '' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>

        <!-- 4. 仓储 -->
        <el-tab-pane label="仓储" name="storage">
          <div class="table-wrapper">
            <table class="data-entry-table">
              <tbody>
                <tr>
                  <td class="field-label required">产品ID</td>
                  <td class="field-icon"><el-icon><Minus /></el-icon></td>
                  <td class="field-input">
                    <el-input-number
                      v-model="storageForm.productId"
                      :min="1"
                      controls-position="right"
                      placeholder="关联产品ID"
                    />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit"></td>
                </tr>
                <tr>
                  <td class="field-label">入库时间</td>
                  <td class="field-icon"><el-icon><Clock /></el-icon></td>
                  <td class="field-input">
                    <el-date-picker
                      v-model="storageForm.storageTime"
                      type="date"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      placeholder="选择入库日期"
                    />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit"></td>
                </tr>
                <tr>
                  <td class="field-label">出库时间</td>
                  <td class="field-icon"><el-icon><Clock /></el-icon></td>
                  <td class="field-input">
                    <el-date-picker
                      v-model="storageForm.outboundTime"
                      type="date"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      placeholder="选择出库日期"
                    />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit"></td>
                </tr>
                <tr>
                  <td class="field-label">数量</td>
                  <td class="field-icon"><el-icon><Minus /></el-icon></td>
                  <td class="field-input">
                    <el-input-number
                      v-model="storageForm.quantity"
                      :min="0"
                      :precision="2"
                      controls-position="right"
                      placeholder="请输入数量"
                    />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit">单位：{{ storageForm.unit || '千克' }}</td>
                </tr>
                <tr>
                  <td class="field-label">单位</td>
                  <td class="field-icon"><el-icon><Minus /></el-icon></td>
                  <td class="field-input">
                    <el-input
                      v-model="storageForm.unit"
                      placeholder="例：千克、箱、个"
                      clearable
                    />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit"></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>

        <!-- 5. 运输销售 -->
        <el-tab-pane label="运输销售" name="transportSale">
          <div class="table-wrapper">
            <table class="data-entry-table">
              <tbody>
                <tr>
                  <td class="field-label required">产品ID</td>
                  <td class="field-icon"><el-icon><Minus /></el-icon></td>
                  <td class="field-input">
                    <el-input-number
                      v-model="transportForm.productId"
                      :min="1"
                      controls-position="right"
                      placeholder="关联产品ID"
                    />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit"></td>
                </tr>
                <tr>
                  <td class="field-label">环境温度</td>
                  <td class="field-icon"><el-icon><Minus /></el-icon></td>
                  <td class="field-input">
                    <el-input-number
                      v-model="transportForm.environmentTemperature"
                      :min="-50"
                      :max="100"
                      :precision="1"
                      controls-position="right"
                    />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit">单位：℃</td>
                </tr>
                <tr>
                  <td class="field-label">产品温度</td>
                  <td class="field-icon"><el-icon><Minus /></el-icon></td>
                  <td class="field-input">
                    <el-input-number
                      v-model="transportForm.productTemperature"
                      :min="-50"
                      :max="100"
                      :precision="1"
                      controls-position="right"
                    />
                  </td>
                  <td class="field-action"><el-button type="text" size="small">+</el-button></td>
                  <td class="field-unit">单位：℃</td>
                </tr>
                <tr>
                  <td class="field-label">记录时间</td>
                  <td class="field-icon"><el-icon><Clock /></el-icon></td>
                  <td class="field-input">
                    <el-date-picker
                      v-model="transportForm.time"
                      type="date"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      placeholder="选择记录日期"
                    />
                  </td>
                  <td class="field-action"></td>
                  <td class="field-unit"></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 提交/重置按钮区 -->
      <div class="submit-area">
        <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
          提交 {{ currentEntityLabel }} 数据
        </el-button>
        <el-button @click="resetForm" size="large">重置当前表单</el-button>
      </div>

      <!-- 全局操作结果提示 -->
      <el-alert
        v-if="resultMessage"
        :title="resultMessage"
        :type="resultType"
        :closable="true"
        @close="resultMessage = ''"
        class="result-alert"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Minus, Clock } from '@element-plus/icons-vue'
import {
  createProduct,
  createMaterialPurchase,
  createInspection,
  createStorage,
  createTransportSale
} from '../services/api'

// 提交状态、消息
const submitting = ref(false)
const resultMessage = ref('')
const resultType = ref('success')

// 当前激活的标签页
const activeEntity = ref('transportSale')

const entityLabels = {
  product: '产品',
  materialPurchase: '原材料采购',
  inspection: '检验检测',
  storage: '仓储',
  transportSale: '运输销售'
}
const currentEntityLabel = computed(() => entityLabels[activeEntity.value])

// ---------- 各实体表单数据对象 ----------
const productForm = reactive({
  antiFakeCode: '',
  name: '',
  specification: '',
  batchNumber: '',
  productionDate: '',
  shelfLife: '',
  imageUrl: '',
  contactPhone: '',
  contactEmail: ''
})

const materialForm = reactive({
  productId: null,
  materialName: '',
  producerName: '',
  producerAddress: ''
})

const inspectionForm = reactive({
  productId: null,
  sampleName: '',
  sampleQuantity: null,
  sampleSpecification: ''
})

const storageForm = reactive({
  productId: null,
  storageTime: '',
  outboundTime: '',
  quantity: null,
  unit: ''
})

const transportForm = reactive({
  productId: null,
  environmentTemperature: null,
  productTemperature: null,
  time: ''
})

// ---------- 字段配置 ----------
const productFields = [
  { prop: 'antiFakeCode', label: '防伪码', required: true, type: 'text', placeholder: '输入12-20位防伪码', tip: '12-20位' },
  { prop: 'name', label: '产品名称', required: true, type: 'text', placeholder: '例：有机大米' },
  { prop: 'specification', label: '规格', required: false, type: 'text', placeholder: '例：400g、5kg', tip: '请包含单位' },
  { prop: 'batchNumber', label: '批号', required: false, type: 'text', placeholder: '例：BATCH2024001' },
  { prop: 'productionDate', label: '生产日期', required: false, type: 'date' },
  { prop: 'shelfLife', label: '保质期', required: false, type: 'text', placeholder: '例：12个月', tip: '请包含单位' },
  { prop: 'imageUrl', label: '图片URL', required: false, type: 'text', placeholder: 'http://...', tip: '以http开头' },
  { prop: 'contactPhone', label: '联系电话', required: false, type: 'text', placeholder: '手机号' },
  { prop: 'contactEmail', label: '联系邮箱', required: false, type: 'text', placeholder: '邮箱' }
]

const materialFields = [
  { prop: 'productId', label: '产品ID', required: true, type: 'number' },
  { prop: 'materialName', label: '原料名称', required: true, type: 'text', placeholder: '例：优质稻谷' },
  { prop: 'producerName', label: '生产商', required: false, type: 'text', placeholder: '可选' },
  { prop: 'producerAddress', label: '生产商地址', required: false, type: 'text', placeholder: '可选' }
]

const inspectionFields = [
  { prop: 'productId', label: '产品ID', required: true, type: 'number' },
  { prop: 'sampleName', label: '样品名称', required: false, type: 'text' },
  { prop: 'sampleQuantity', label: '样品数量', required: false, type: 'number' },
  { prop: 'sampleSpecification', label: '样品规格', required: false, type: 'text' }
]

const storageFields = [
  { prop: 'productId', label: '产品ID', required: true, type: 'number' },
  { prop: 'storageTime', label: '入库时间', required: false, type: 'date' },
  { prop: 'outboundTime', label: '出库时间', required: false, type: 'date' },
  { prop: 'quantity', label: '数量', required: false, type: 'number', precision: 2 },
  { prop: 'unit', label: '单位', required: false, type: 'text', placeholder: '例：千克' }
]

// ---------- 提交处理 ----------
async function handleSubmit() {
  let isValid = true
  let errorMsg = ''

  if (activeEntity.value === 'transportSale') {
    if (!transportForm.productId) {
      isValid = false
      errorMsg = '产品ID不能为空'
    }
  } else if (activeEntity.value === 'product') {
    if (!productForm.antiFakeCode) { isValid = false; errorMsg = '防伪码不能为空' }
    else if (!productForm.name) { isValid = false; errorMsg = '产品名称不能为空' }
  } else if (activeEntity.value === 'materialPurchase') {
    if (!materialForm.productId) { isValid = false; errorMsg = '产品ID不能为空' }
    else if (!materialForm.materialName) { isValid = false; errorMsg = '原料名称不能为空' }
  } else if (activeEntity.value === 'inspection') {
    if (!inspectionForm.productId) { isValid = false; errorMsg = '产品ID不能为空' }
  } else if (activeEntity.value === 'storage') {
    if (!storageForm.productId) { isValid = false; errorMsg = '产品ID不能为空' }
  }

  if (!isValid) {
    ElMessage.warning(errorMsg || '请填写完整必填项')
    return
  }

  submitting.value = true
  resultMessage.value = ''

  try {
    let response
    const formData = { ...getCurrentForm() }
    let apiPayload

    switch (activeEntity.value) {
      case 'product':
        if (formData.imageUrl && !formData.imageUrl.startsWith('http')) {
          formData.imageUrl = ''
        }
        apiPayload = {
          antiFakeCode: formData.antiFakeCode,
          name: formData.name,
          specification: formData.specification || null,
          batchNumber: formData.batchNumber || null,
          productionDate: formData.productionDate || null,
          shelfLife: formData.shelfLife || null,
          imageUrl: formData.imageUrl || null,
          contactPhone: formData.contactPhone || null,
          contactEmail: formData.contactEmail || null
        }
        response = await createProduct(apiPayload)
        break

      case 'materialPurchase':
        apiPayload = {
          productId: Number(formData.productId),
          materialName: formData.materialName,
          producerName: formData.producerName || null,
          producerAddress: formData.producerAddress || null
        }
        response = await createMaterialPurchase(apiPayload)
        break

      case 'inspection':
        apiPayload = {
          productId: Number(formData.productId),
          sampleName: formData.sampleName || null,
          sampleQuantity: formData.sampleQuantity ? Number(formData.sampleQuantity) : null,
          sampleSpecification: formData.sampleSpecification || null
        }
        response = await createInspection(apiPayload)
        break

      case 'storage':
        apiPayload = {
          productId: Number(formData.productId),
          storageTime: formData.storageTime ? `${formData.storageTime}T00:00:00` : null,
          outboundTime: formData.outboundTime ? `${formData.outboundTime}T00:00:00` : null,
          quantity: formData.quantity !== null && formData.quantity !== ''
                      ? Number(formData.quantity)
                      : null,
          unit: formData.unit || null
        }
        response = await createStorage(apiPayload)
        break

      case 'transportSale':
        apiPayload = {
          productId: Number(formData.productId),
          environmentTemperature: formData.environmentTemperature !== null ? Number(formData.environmentTemperature) : null,
          productTemperature: formData.productTemperature !== null ? Number(formData.productTemperature) : null,
          recordTime: formData.time || null  // 如果后端是 recordTime 而不是 time
        }
        console.log('发送运输销售数据:', apiPayload)
        response = await createTransportSale(apiPayload)
        break

      default:
        ElMessage.error(`未知的实体类型: ${activeEntity.value}`)
        submitting.value = false
        return
    }

    ElMessage.success('提交成功')
    resultMessage.value = '操作成功完成'
    resultType.value = 'success'

    // 提交成功后重置表单
    resetForm()

  } catch (error) {
    console.error('提交失败:', error)
    console.error('错误响应:', error.response?.data)

    // 更详细的错误提示
    let errorMessage = '提交失败'
    if (error.response?.data) {
      if (typeof error.response.data === 'string') {
        errorMessage = error.response.data
      } else if (error.response.data.message) {
        errorMessage = error.response.data.message
      } else if (error.response.data.error) {
        errorMessage = error.response.data.error
      }
    }

    ElMessage.error(`${errorMessage}，请稍后重试`)
    resultMessage.value = errorMessage || '操作失败'
    resultType.value = 'error'
  } finally {
    submitting.value = false
  }
}

// 获取当前激活的表单数据对象
function getCurrentForm() {
  const map = {
    product: productForm,
    materialPurchase: materialForm,
    inspection: inspectionForm,
    storage: storageForm,
    transportSale: transportForm
  }
  return map[activeEntity.value]
}

// 重置当前表单
function resetForm() {
  const form = getCurrentForm()
  Object.keys(form).forEach(key => {
    if (typeof form[key] === 'string') form[key] = ''
    else if (typeof form[key] === 'number') form[key] = null
    else form[key] = null
  })
  ElMessage.success('表单已重置')
}

// “+” 按钮交互
function handlePlus(field) {
  ElMessage.info(`功能开发中: ${field.label} 附加操作`)
}

// 调试辅助：打印当前表单数据
function debugForm() {
  console.log('当前实体:', activeEntity.value)
  console.log('表单数据:', getCurrentForm())
}
</script>

<style scoped>
.insert-data-tool {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.tool-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.main-title {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: #1f2d3d;
  line-height: 1.2;
}
.sub-title {
  margin: 4px 0 0;
  font-size: 1.2rem;
  font-weight: 500;
  color: #3a4a5a;
}
.header-tips {
  text-align: right;
}
.required-tip {
  display: block;
  color: #e6a23c;
  font-size: 0.9rem;
  margin-bottom: 4px;
}
.type-tip {
  color: #909399;
  font-size: 0.9rem;
}
.entity-tabs {
  margin-top: 16px;
}
.table-wrapper {
  overflow-x: auto;
  padding: 8px 0;
}
.data-entry-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.data-entry-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #ebeef5;
  vertical-align: middle;
}
.field-label {
  width: 120px;
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}
.field-label.required::before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
}
.field-icon {
  width: 40px;
  text-align: center;
  color: #909399;
}
.field-input {
  min-width: 200px;
}
.field-action {
  width: 50px;
  text-align: center;
}
.field-unit {
  width: 100px;
  color: #909399;
  font-size: 12px;
  padding-left: 12px;
}
.field-tip {
  color: #909399;
  font-size: 12px;
}
.submit-area {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
.result-alert {
  margin-top: 20px;
}
.el-input-number {
  width: 100%;
}
.el-date-editor {
  width: 100%;
}
@media (prefers-color-scheme: dark) {
  .tool-card {
    background: #1e1e1e;
    border-color: #333;
  }
  .data-entry-table td {
    border-bottom-color: #3a3a3a;
  }
}
</style>