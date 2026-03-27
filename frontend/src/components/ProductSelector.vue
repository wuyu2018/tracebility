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
        :key="product.id"
        :label="formatLabel(product)"
        :value="product.name"
      />
    </el-select>
    <div class="selector-tip">{{ tipText }}</div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
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
  return '请选择或搜索产品'
})

const tipText = computed(() => {
  return '可按产品名称搜索'
})

const formatLabel = (product) => {
  return product.name || ''
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
  const product = productOptions.value.find(p => p.name === value)
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
