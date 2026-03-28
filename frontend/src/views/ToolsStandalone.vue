<template>
  <div class="tools-standalone">
    <!-- 添加标签页切换 -->
    <el-tabs v-model="activeTab" class="tools-tabs">
      <el-tab-pane label="防伪工具" name="security">
        <div class="tools-page">
          <h1>防伪工具</h1>
          <p class="tools-desc">生成防伪验证二维码</p>

          <section class="tool-card">
            <h2>防伪验证二维码</h2>
            <p class="card-desc">扫描此二维码可直达防伪验证页面</p>
            <div class="qr-wrap">
              <canvas ref="qrCanvas" class="qr-canvas"></canvas>
            </div>
            <p class="qr-url">{{ verifyUrl }}</p>
            <button type="button" class="btn-download" @click="downloadQr">下载二维码</button>
          </section>
        </div>
      </el-tab-pane>

      <!-- 防伪码生成管理标签页 -->
      <el-tab-pane label="防伪码生成" name="qrcode">
        <div class="tools-page">
          <h1>防伪码管理</h1>
          <p class="tools-desc">为已录入的产品生成防伪二维码，下载后贴到对应产品包装上</p>

          <section class="tool-card">
            <h2>已生成二维码的产品列表</h2>
            <div class="batch-actions">
              <el-button type="primary" @click="batchGenerateQrCodes" :disabled="!productsWithoutQrCode.length" :loading="batchGenerating">
                批量生成二维码 ({{ productsWithoutQrCode.length }})
              </el-button>
              <el-button type="success" @click="batchDownloadQrCodes" :disabled="!selectedProducts.length">
                批量下载 ({{ selectedProducts.length }})
              </el-button>
              <el-button type="danger" @click="batchDeleteProducts" :disabled="!selectedProducts.length" :loading="batchDeleting">
                批量删除 ({{ selectedProducts.length }})
              </el-button>
            </div>
            <el-table 
              :data="productsWithQrCode" 
              style="width: 100%"
              @selection-change="handleSelectionChange"
              ref="productTable"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="name" label="产品名称" />
              <el-table-column prop="batchNumber" label="批号" />
              <el-table-column prop="antiFakeCode" label="防伪码" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button type="success" size="small" @click="downloadQrCode(scope.row)">下载</el-button>
                  <el-button type="primary" size="small" @click="viewProductDetail(scope.row)">数据总览</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="productsWithoutQrCode.length > 0" class="pending-section">
              <h3>待生成二维码的产品 ({{ productsWithoutQrCode.length }})</h3>
              <div class="batch-actions">
                <el-button type="warning" @click="batchGenerateSelectedQrCodes" :disabled="!selectedPendingProducts.length" :loading="batchGenerating">
                  批量生成选中的二维码 ({{ selectedPendingProducts.length }})
                </el-button>
                <el-button type="info" @click="selectAllPending" :disabled="!productsWithoutQrCode.length">
                  全选
                </el-button>
                <el-button type="info" @click="clearPendingSelection" :disabled="!selectedPendingProducts.length">
                  取消选择
                </el-button>
              </div>
              <el-table 
                :data="productsWithoutQrCode" 
                style="width: 100%"
                @selection-change="handlePendingSelectionChange"
                ref="pendingTable"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="name" label="产品名称" />
                <el-table-column prop="batchNumber" label="批号" />
                <el-table-column prop="antiFakeCode" label="防伪码" />
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button type="warning" size="small" @click="generateQrCodeForProduct(scope.row)">生成</el-button>
                    <el-button type="primary" size="small" @click="viewProductDetail(scope.row)">数据总览</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>
        </div>
      </el-tab-pane>

      <!--投诉管理工具标签页 -->
      <el-tab-pane label="投诉管理" name="complaint">
        <ComplaintAdminTool />
      </el-tab-pane>

      <!--数据录入标签页 -->
      <el-tab-pane label="数据录入" name="insert">
        <InsertDataTool />
      </el-tab-pane>

      <!--管理员管理标签页 -->
      <el-tab-pane label="管理员管理" name="admin">
        <AddAdmin />
      </el-tab-pane>
    </el-tabs>

    <!-- 产品详情弹窗 -->
    <el-dialog
      v-model="productDetailVisible"
      title="管理员数据总览"
      width="90%"
      :close-on-click-modal="false"
      destroy-on-close
      class="product-detail-dialog"
    >
      <ProductDetail />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import QRCode from 'https://cdn.jsdelivr.net/npm/qrcode@1.5.3/+esm'
import ComplaintAdminTool from '/src/views/getAllComplaintInfo.vue'
import AddAdmin from '/src/views/AddAdmin.vue'
import ProductDetail from '../components/ProductDetail.vue'
import { ElMessage, ElMessageBox, ElDialog } from 'element-plus'
import InsertDataTool from '../components/InsertDataTool.vue';
import { listAllProducts, generateQrCode, batchGenerateQrCodes as batchGenerateQrCodesApi, batchDeleteProducts as batchDeleteProductsApi } from '../services/api'

const activeTab = ref('security')
const qrCanvas = ref(null)

// 二维码生成相关
const productList = ref([])

// 批量操作相关
const selectedProducts = ref([])
const productTable = ref(null)
const selectedPendingProducts = ref([])
const pendingTable = ref(null)
const batchGenerating = ref(false)
const batchDeleting = ref(false)

// 产品详情弹窗
const productDetailVisible = ref(false)
const currentProduct = ref(null)

function viewProductDetail(product) {
  currentProduct.value = product
  productDetailVisible.value = true
}

function closeProductDetail() {
  productDetailVisible.value = false
}

function getVerifyUrl() {
  if (typeof window === 'undefined') return ''
  return window.location.origin + '/'
}

const verifyUrl = computed(() => getVerifyUrl())

function drawQr() {
  const url = getVerifyUrl()
  if (!qrCanvas.value || !url) return
  QRCode.toCanvas(qrCanvas.value, url, {
    width: 200,
    margin: 2,
  }, (err) => {
    if (err) console.error(err)
  })
}

function downloadQr() {
  const url = getVerifyUrl()
  if (!url) return
  QRCode.toDataURL(url, { width: 400, margin: 2 }).then((dataUrl) => {
    const a = document.createElement('a')
    a.href = dataUrl
    a.download = '防伪验证二维码.png'
    a.click()
  })
}

async function loadProductList() {
  try {
    const response = await fetch('/api/insert/products/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    const data = await response.json()
    console.log('产品列表原始数据:', data)
    
    if (Array.isArray(data)) {
      productList.value = data
    } else if (data && Array.isArray(data.data)) {
      productList.value = data.data
    } else {
      productList.value = []
    }
    
    console.log('处理后产品列表:', productList.value)
    console.log('有二维码的产品:', productsWithQrCode.value.length)
    console.log('无二维码的产品:', productsWithoutQrCode.value.length)
  } catch (error) {
    console.error('Failed to load product list:', error)
    productList.value = []
  }
}

const productsWithQrCode = computed(() => {
  return productList.value.filter(p => p.antiFakeCode)
})

const productsWithoutQrCode = computed(() => {
  return productList.value.filter(p => !p.antiFakeCode)
})

function handleSelectionChange(selection) {
  selectedProducts.value = selection
}

function handlePendingSelectionChange(selection) {
  selectedPendingProducts.value = selection
}

function selectAllPending() {
  pendingTable.value?.clearSelection()
  productsWithoutQrCode.value.forEach(product => {
    pendingTable.value?.toggleRowSelection(product, true)
  })
}

function clearPendingSelection() {
  pendingTable.value?.clearSelection()
  selectedPendingProducts.value = []
}

function downloadQrCode(row) {
  if (!row.antiFakeCode) {
    ElMessage.warning('该产品暂无防伪码')
    return
  }
  QRCode.toDataURL(row.antiFakeCode, { width: 300, margin: 2 }).then((dataUrl) => {
    const a = document.createElement('a')
    a.href = dataUrl
    a.download = `防伪码_${row.antiFakeCode}.png`
    a.click()
  })
}

async function generateQrCodeForProduct(row) {
  if (!row.id) {
    ElMessage.error('产品ID无效')
    return
  }
  
  // 如果产品没有防伪码，先调用API生成
  if (!row.antiFakeCode) {
    try {
      await generateQrCode(row.id)
      await loadProductList()
      // 找到更新后的产品
      const updatedProduct = productList.value.find(p => p.id === row.id)
      if (updatedProduct && updatedProduct.antiFakeCode) {
        downloadQrCode(updatedProduct)
      }
      ElMessage.success(`产品 ${row.name} 防伪码生成成功`)
    } catch (error) {
      console.error('Generate QR code failed:', error)
      ElMessage.error('防伪码生成失败: ' + (error.message || '请重试'))
    }
  } else {
    // 已有防伪码，直接生成二维码并下载
    downloadQrCode(row)
  }
}

async function batchGenerateSelectedQrCodes() {
  if (!selectedPendingProducts.value.length) {
    ElMessage.info('请先选择要生成二维码的产品')
    return
  }
  
  batchGenerating.value = true
  try {
    const productIds = selectedPendingProducts.value.map(p => p.id).filter(id => id != null)
    console.log('生成选中的二维码产品IDs:', productIds)
    
    if (productIds.length === 0) {
      ElMessage.warning('未找到有效的产品ID')
      return
    }
    
    await batchGenerateQrCodesApi(productIds)
    ElMessage.success(`成功生成 ${productIds.length} 个二维码`)
    selectedPendingProducts.value = []
    await loadProductList()
  } catch (error) {
    console.error('Batch generate failed:', error)
    ElMessage.error('批量生成失败: ' + (error.message || '请重试'))
  } finally {
    batchGenerating.value = false
  }
}

async function batchGenerateQrCodes() {
  if (!productsWithoutQrCode.value.length) {
    ElMessage.info('没有需要生成二维码的产品')
    return
  }
  
  batchGenerating.value = true
  try {
    const productIds = productsWithoutQrCode.value.map(p => p.id).filter(id => id != null)
    console.log('生成二维码产品IDs:', productIds)
    
    if (productIds.length === 0) {
      ElMessage.warning('未找到有效的产品ID')
      return
    }
    
    await batchGenerateQrCodesApi(productIds)
    ElMessage.success(`成功生成 ${productIds.length} 个二维码`)
    await loadProductList()
  } catch (error) {
    console.error('Batch generate failed:', error)
    ElMessage.error('批量生成失败: ' + (error.message || '请重试'))
  } finally {
    batchGenerating.value = false
  }
}

function batchDownloadQrCodes() {
  if (!selectedProducts.value.length) return
  
  selectedProducts.value.forEach((product, index) => {
    if (product.antiFakeCode) {
      setTimeout(() => {
        QRCode.toDataURL(product.antiFakeCode, { width: 300, margin: 2 }).then((dataUrl) => {
          const a = document.createElement('a')
          a.href = dataUrl
          a.download = `防伪码_${product.antiFakeCode}.png`
          a.click()
        })
      }, index * 200)
    }
  })
  ElMessage.success('开始下载二维码')
}

async function batchDeleteProducts() {
  if (!selectedProducts.value.length) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedProducts.value.length} 个产品的防伪码吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    batchDeleting.value = true
    const productIds = selectedProducts.value.map(p => p.id).filter(id => id != null)
    console.log('删除产品IDs:', productIds)
    
    if (productIds.length === 0) {
      ElMessage.warning('未找到有效的产品ID')
      return
    }
    
    await batchDeleteProductsApi(productIds)
    ElMessage.success('批量删除成功')
    selectedProducts.value = []
    await loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Batch delete failed:', error)
      ElMessage.error('批量删除失败: ' + (error.message || '请重试'))
    }
  } finally {
    batchDeleting.value = false
  }
}

onMounted(() => {
  drawQr()
  loadProductList()
})
</script>

<style scoped>
.tools-standalone {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  justify-content: center;
  padding: 2rem 1rem;
}

.tools-standalone :deep(.el-tabs__content) {
  overflow: visible;
}

.tools-standalone :deep(.el-tabs__header) {
  margin-bottom: 1.5rem;
}

.tools-standalone :deep(.el-tabs__item) {
  font-size: 1rem;
  padding: 0 1.25rem;
}

.tools-standalone :deep(.el-tabs__nav) {
  display: flex;
  justify-content: center;
}

@media (max-width: 480px) {
  .tools-standalone {
    padding: 1rem 0.875rem;
  }
}

.tools-page {
  max-width: 600px;
  width: 100%;
  margin: 0 auto;
}

.tools-page h1 {
  font-size: 1.5rem;
  color: var(--color-primary-dark);
  margin-bottom: 0.5rem;
  text-align: center;
}

.tools-desc {
  color: var(--color-text-muted);
  font-size: 0.95rem;
  margin-bottom: 2rem;
  text-align: center;
}

.tool-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(45, 90, 61, 0.06);
  text-align: center;
}

.tool-card h2 {
  font-size: 1.1rem;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.card-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-bottom: 1.25rem;
}

.qr-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.qr-canvas {
  border-radius: var(--radius);
}

.qr-url {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  word-break: break-all;
  margin-bottom: 1rem;
  text-align: center;
}

.btn-download {
  width: 100%;
  padding: 0.75rem;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  border-radius: var(--radius);
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-download:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.batch-actions {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.batch-actions .el-button {
  padding: 0.75rem 1.25rem;
}

.pending-section {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px dashed #e0e6e1;
}

.pending-section h3 {
  margin-bottom: 1rem;
  color: var(--color-text);
  font-size: 1rem;
}
</style>