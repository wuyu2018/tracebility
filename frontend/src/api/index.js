import api from '../utils/axios'

// ==================== 认证管理 ====================

export async function storeCaptcha(username, captcha) {
  return api.post('/captcha', { username, captcha })
}

export async function login(username, password, captcha) {
  return api.post('/login', { username, password, captcha })
}

export async function registerAdmin(data) {
  return api.post('/admin/register', data)
}

// ==================== 产品管理 ====================

export function getProducts(keyword) {
  return api.get('/products', keyword ? { params: { keyword } } : {})
}

export function getProduct(id) {
  return api.get(`/products/${id}`)
}

export function createProduct(data) {
  return api.post('/products', data)
}

export function updateProduct(id, data) {
  return api.put(`/products/${id}`, data)
}

export function deleteProduct(id) {
  return api.delete(`/products/${id}`)
}

export function selectProducts(keyword, role = 'consumer') {
  return api.get('/products/select', { params: { keyword, role } })
}

export function getProductList() {
  return api.post('/insert/products/list')
}

export function generateProductQrCode(productId) {
  return api.post(`/insert/products/${productId}/generate-qrcode`)
}

export function batchGenerateQrCodes(productIds) {
  return api.post('/insert/products/batch-generate-qrcode', productIds)
}

export function batchDeleteProducts(productIds) {
  return api.post('/insert/products/batch-delete', { productIds })
}

// ==================== 原料品种管理 ====================

export function getMaterialVarieties(activeOnly = true) {
  return api.get('/v2/material-varieties', { params: { activeOnly } })
}

export function createMaterialVariety(data) {
  return api.post('/v2/material-varieties', data)
}

export function updateMaterialVariety(id, data) {
  return api.put(`/v2/material-varieties/${id}`, data)
}

export function deleteMaterialVariety(id) {
  return api.delete(`/v2/material-varieties/${id}`)
}

export function activateMaterialVariety(id) {
  return api.post(`/v2/material-varieties/${id}/activate`)
}

export function deactivateMaterialVariety(id) {
  return api.post(`/v2/material-varieties/${id}/deactivate`)
}

export function getMaterialVariety(id) {
  return api.get(`/v2/material-varieties/${id}`)
}

// ==================== 原料采购管理 ====================

export function getMaterialPurchases(materialId) {
  const params = materialId ? { params: { materialId } } : {}
  return api.get('/v2/material-purchases', params)
}

export function getMaterialPurchase(id) {
  return api.get(`/v2/material-purchases/${id}`)
}

export function createMaterialPurchase(data) {
  return api.post('/v2/material-purchases', data)
}

export function updateMaterialPurchase(id, data) {
  return api.put(`/v2/material-purchases/${id}`, data)
}

export function deleteMaterialPurchase(id) {
  return api.delete(`/v2/material-purchases/${id}`)
}

// ==================== 生产批次管理 ====================

export function getBatches(productId) {
  const params = productId ? { params: { productId } } : {}
  return api.get('/batches', params)
}

export function getBatch(id) {
  return api.get(`/batches/${id}`)
}

export function getBatchByNumber(batchNumber) {
  return api.get(`/batches/by-number/${batchNumber}`)
}

export function createBatch(data) {
  return api.post('/v2/batches', data)
}

// ==================== 仓储管理 ====================

export function createStorage(data) {
  return api.post('/v2/storage', data)
}

export function getStorages() {
  return api.post('/insert/storages')
}

// ==================== 运输销售管理 ====================

export function createTransportSale(data) {
  return api.post('/v2/transport-sales', data)
}

export function getTransportSales() {
  return api.post('/insert/transport-sales')
}

// ==================== 质检管理 ====================

export function createInspection(data) {
  return api.post('/v2/inspections', data)
}

export function getInspections() {
  return api.post('/insert/inspections')
}

// ==================== 防伪码管理 ====================

export function generateSecurityCodes(batchId, quantity) {
  return api.post(`/batches/${batchId}/security-codes`, { quantity })
}

export function getSecurityCodes(batchId) {
  return api.get(`/batches/${batchId}/security-codes`)
}

export function exportSecurityCodes(batchId) {
  return api.get(`/security-codes/export/${batchId}`)
}

// ==================== 产品原料绑定 ====================

export function bindProductMaterial(data) {
  return api.post('/product-materials', data)
}

export function getProductMaterials(productId) {
  return api.get('/product-materials', productId ? { params: { productId } } : {})
}

export function unbindProductMaterial(productId, materialId) {
  return api.delete('/product-materials', { params: { productId, materialId } })
}

export function toggleProductMaterialVisibility(id, visible) {
  return api.patch(`/product-materials/${id}/visibility`, { visible })
}

// ==================== 追溯查询（公开） ====================

export function verifyAntiFakeCode(code) {
  return api.get('/v2/trace/verify', { params: { code } })
}

export function traceByBatchNumber(batchNumber) {
  return api.get(`/v2/trace/batch/${batchNumber}`)
}

// ==================== 投诉管理 ====================

export function submitComplaint(data) {
  return api.post('/complaint', data)
}

export function getAllComplaints() {
  return api.get('/getAllComplaintInfo')
}

export function deleteComplaint(id) {
  return api.delete(`/deleteComplaintInfo/${id}`)
}

export function batchDeleteComplaints(ids) {
  return api.delete('/deleteComplaintInfo/batch', { data: { ids } })
}

// ==================== 区块链监控 ====================

export function getBlockchainPublicKey() {
  return api.get('/blockchain/public-key')
}

export function getBlockchainMonitorSummary() {
  return api.get('/blockchain/monitor/summary')
}

// ==================== Agent 管理 ====================

export function getAgentList() {
  return api.get('/agent/list')
}

export function getAgent(agentId) {
  return api.get(`/agent/${agentId}`)
}

export function getConsensusStatus() {
  return api.get('/agent/consensus/status')
}

export function getReputationList() {
  return api.get('/agent/reputation/list')
}

export function getAgentReputation(agentId) {
  return api.get(`/agent/${agentId}/reputation`)
}
