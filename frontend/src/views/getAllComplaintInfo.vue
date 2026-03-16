<template>
  <div class="complaint-admin-container">
    <!-- 顶部标题和操作区 -->
    <div class="complaint-header">
      <h2>投诉信息管理</h2>
      <div class="action-buttons">
        <el-button type="primary" @click="fetchComplaints" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索投诉内容"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="filterProduct"
            placeholder="按产品名称筛选"
            clearable
            filterable
            @change="handleFilter"
          >
            <el-option
              v-for="product in productOptions"
              :key="product.value"
              :label="product.label"
              :value="product.value"
            />
          </el-select>
        </el-col>
      </el-row>
    </div>

    <!-- 投诉信息表格 -->
    <div class="table-section">
      <el-table
        :data="filteredComplaints"
        v-loading="loading"
        stripe
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />

        <el-table-column prop="complaintId" label="投诉编号" width="120" sortable>
          <template #default="{ row }">
            <el-tag type="info">{{ row.complaintId }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="productName" label="产品名称" width="150" sortable />

        <el-table-column prop="complaintContent" label="投诉内容" minWidth="200">
          <template #default="{ row }">
            <div class="content-truncate" :title="row.complaintContent">
              {{ truncateContent(row.complaintContent, 50) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="complaintTime" label="投诉时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.complaintTime) }}
          </template>
        </el-table-column>

        <!-- 操作列 - 删除按钮 -->
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
              :loading="deletingId === row.complaintId"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section" v-if="filteredComplaints.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalComplaints"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
      center
    >
      <div class="delete-confirm-content">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <p>确定要删除这条投诉信息吗？</p>
        <p class="delete-detail" v-if="selectedComplaint">
          投诉编号：{{ selectedComplaint.complaintId }}<br>
          产品名称：{{ selectedComplaint.productName }}
        </p>
        <p class="delete-warning">此操作不可恢复，请谨慎操作！</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button
            type="danger"
            @click="confirmDelete"
            :loading="deletingId === selectedComplaint?.complaintId"
          >
            确认删除
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Search,
  Delete,
  WarningFilled
} from '@element-plus/icons-vue'

import {
  getAllComplaintInfo,
  listAllProducts,
  deleteComplaintInfo
} from '../services/api'

const loading = ref(false)
const complaints = ref([])
const searchKeyword = ref('')
const filterProduct = ref('')
const productOptions = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const deleteDialogVisible = ref(false)
const selectedComplaint = ref(null)
const deletingId = ref(null)

const filteredComplaints = computed(() => {
  let result = [...complaints.value]

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item =>
      item.complaintContent?.toLowerCase().includes(keyword) ||
      item.productName?.toLowerCase().includes(keyword)
    )
  }

  if (filterProduct.value) {
    result = result.filter(item => item.productName === filterProduct.value)
  }

  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

const totalComplaints = computed(() => {
  let result = [...complaints.value]

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item =>
      item.complaintContent?.toLowerCase().includes(keyword) ||
      item.productName?.toLowerCase().includes(keyword)
    )
  }

  if (filterProduct.value) {
    result = result.filter(item => item.productName === filterProduct.value)
  }

  return result.length
})

onMounted(() => {
  fetchComplaints()
})

const fetchComplaints = async () => {
  loading.value = true
  try {
    let productMap = {}
    try {
      const productsResponse = await listAllProducts()

      let products = []

      if (Array.isArray(productsResponse)) {
        products = productsResponse
      } else if (productsResponse?.data && Array.isArray(productsResponse.data)) {
        products = productsResponse.data
      }

      productMap = products.reduce((map, product) => {
        const productId = product.id
        const productName = product.name || product.productName || `产品${product.id}`
        map[productId] = productName
        return map
      }, {})

      productOptions.value = products.map(product => ({
        value: product.name || product.productName || `产品${product.id}`,
        label: product.name || product.productName || `产品${product.id}`
      }))

      const uniqueOptions = []
      const valueSet = new Set()
      productOptions.value.forEach(option => {
        if (!valueSet.has(option.value)) {
          valueSet.add(option.value)
          uniqueOptions.push(option)
        }
      })
      productOptions.value = uniqueOptions

    } catch (productError) {
      console.warn('获取产品列表失败:', productError)
    }

    const response = await getAllComplaintInfo()
    console.log('投诉列表返回:', response)

    let complaintList = []
    if (Array.isArray(response)) {
      complaintList = response
    } else if (response?.data && Array.isArray(response.data)) {
      complaintList = response.data
    }

    if (complaintList.length > 0) {
      complaints.value = complaintList.map((item, index) => {
        const complaintId = item.id || item.complaintId || `C${Date.now()}-${index}`
        const productId = item.productId || item.product_id

        return {
          complaintId: complaintId,
          productId: productId,
          productName: productMap[productId] || (productId ? `产品${productId}` : '未知产品'),
          complaintContent: item.complaintReason || item.complaint_content || item.complaint_reason || item.complaintContent || '',
          complaintTime: item.complaintTime || item.complaint_time || new Date().toISOString(),
          rawData: item
        }
      })

      const complaintProductNames = [...new Set(complaints.value.map(item => item.productName).filter(Boolean))]
      complaintProductNames.forEach(productName => {
        if (!productOptions.value.some(opt => opt.value === productName)) {
          productOptions.value.push({
            value: productName,
            label: productName
          })
        }
      })

      productOptions.value.sort((a, b) => a.label.localeCompare(b.label))

      ElMessage.success(`加载成功，共 ${complaints.value.length} 条投诉`)
    } else {
      complaints.value = []
      ElMessage.info('暂无投诉数据')
    }
  } catch (error) {
    console.error('获取投诉信息失败:', error)
    ElMessage.error('获取投诉信息失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const handleDelete = (row) => {
  console.log('删除按钮点击:', row)
  selectedComplaint.value = row
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!selectedComplaint.value) return

  const complaint = selectedComplaint.value
  deletingId.value = complaint.complaintId

  try {
    console.log('正在删除投诉，ID:', complaint.complaintId)

    const response = await deleteComplaintInfo(complaint.complaintId)
    console.log('删除响应:', response)

    const index = complaints.value.findIndex(item => item.complaintId === complaint.complaintId)
    if (index !== -1) {
      complaints.value.splice(index, 1)
    }

    ElMessage.success('删除成功')
    deleteDialogVisible.value = false

    if (filteredComplaints.value.length === 0 && currentPage.value > 1) {
      currentPage.value--
    }

    refreshProductOptions()

  } catch (error) {
    console.error('删除投诉失败:', error)

    if (error.response) {
      ElMessage.error('删除失败: ' + (error.response.data || error.response.statusText))
    } else if (error.request) {
      ElMessage.error('删除失败: 服务器无响应')
    } else {
      ElMessage.error('删除失败: ' + error.message)
    }
  } finally {
    deletingId.value = null
    selectedComplaint.value = null
  }
}

const refreshProductOptions = () => {
  const complaintProductNames = [...new Set(complaints.value.map(item => item.productName).filter(Boolean))]
  productOptions.value = productOptions.value.filter(opt =>
    complaintProductNames.includes(opt.value)
  )
  complaintProductNames.forEach(productName => {
    if (!productOptions.value.some(opt => opt.value === productName)) {
      productOptions.value.push({
        value: productName,
        label: productName
      })
    }
  })

  productOptions.value.sort((a, b) => a.label.localeCompare(b.label))
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

const handleSortChange = ({ prop, order }) => {
  if (!order) return

  complaints.value = [...complaints.value].sort((a, b) => {
    let aVal = a[prop] || ''
    let bVal = b[prop] || ''

    if (prop.includes('Time')) {
      aVal = new Date(aVal).getTime()
      bVal = new Date(bVal).getTime()
    }

    if (order === 'ascending') {
      return aVal > bVal ? 1 : -1
    } else {
      return aVal < bVal ? 1 : -1
    }
  })
}

const truncateContent = (content, maxLength) => {
  if (!content) return ''
  return content.length > maxLength
    ? content.substring(0, maxLength) + '...'
    : content
}

const formatDateTime = (dateString) => {
  if (!dateString) return ''
  try {
    return new Date(dateString).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).replace(/\//g, '-')
  } catch {
    return dateString
  }
}
</script>

<style scoped>
.complaint-admin-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.complaint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.complaint-header h2 {
  margin: 0;
  color: #303133;
}

.filter-section {
  background-color: white;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.table-section {
  background-color: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.content-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.el-row {
  margin-bottom: 0;
}

:deep(.el-table th) {
  background-color: #fafafa;
  font-weight: 600;
}

:deep(.el-table .cell) {
  padding: 8px 12px;
}

.delete-confirm-content {
  text-align: center;
  padding: 20px 0;
}

.warning-icon {
  font-size: 48px;
  color: #f56c6c;
  margin-bottom: 16px;
}

.delete-detail {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin: 16px 0;
  font-size: 14px;
  color: #606266;
  text-align: left;
  line-height: 1.8;
}

.delete-warning {
  color: #f56c6c;
  font-size: 13px;
  margin-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>