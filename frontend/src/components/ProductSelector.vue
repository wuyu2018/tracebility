<template>
  <div class="product-selector">
    <el-select
      v-model="selectedValue"
      :placeholder="placeholderText"
      filterable
      clearable
      remote
      :remote-method="searchProducts"
      :loading="loading"
      @change="handleChange"
    >
      <el-option
        v-for="product in productOptions"
        :key="role === 'admin' ? product.antiFakeCode : product.id"
        :label="formatLabel(product)"
        :value="role === 'admin' ? product.antiFakeCode : product.id"
      />
    </el-select>
    <div class="selector-tip">{{ tipText }}</div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  role: {
    type: String,
    default: 'consumer',
    validator: (value) => ['admin', 'consumer'].includes(value)
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = ref(props.modelValue)
const productOptions = ref([])
const loading = ref(false)

const API_BASE_URL = '/api'

const placeholderText = computed(() => {
  return props.role === 'admin' ? '请选择或搜索产品（防伪码）' : '请选择或搜索产品名称'
})

const tipText = computed(() => {
  return props.role === 'admin' ? '可按产品名称、批号或防伪码搜索' : '可按产品名称或批号搜索'
})

const formatLabel = (product) => {
  if (props.role === 'admin') {
    return `${product.name || ''} | 批号: ${product.batchNumber || ''} | 防伪码: ${product.antiFakeCode || ''}`
  } else {
    const dateStr = product.productionDate ? ` | 生产日期: ${product.productionDate}` : ''
    return `${product.name || ''} (批号: ${product.batchNumber || ''}${dateStr})`
  }
}

const searchProducts = async (keyword) => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (keyword && keyword.trim()) {
      params.append('keyword', keyword.trim())
    }
    params.append('role', props.role)
    
    const response = await axios.get(`${API_BASE_URL}/products/select?${params.toString()}`)
    productOptions.value = response.data || []
  } catch (error) {
    console.error('搜索产品失败:', error)
    productOptions.value = []
  } finally {
    loading.value = false
  }
}

const handleChange = (value) => {
  const product = productOptions.value.find(p => 
    (props.role === 'admin' ? p.antiFakeCode : p.id) === value
  )
  emit('update:modelValue', value)
  emit('change', product)
}

watch(() => props.modelValue, (newVal) => {
  selectedValue.value = newVal
})

watch(() => props.role, () => {
  selectedValue.value = ''
  productOptions.value = []
})

onMounted(() => {
  searchProducts('')
})
</script>

<style scoped>
.product-selector {
  width: 100%;
}

.selector-tip {
  font-size: 0.85rem;
  color: var(--color-text-muted, #909399);
  margin-top: 0.35rem;
}

:deep(.el-select) {
  width: 100%;
}
</style>
