import api from '../utils/axios'

// ==================== 认证相关 ====================

export async function storeCaptcha(username, captcha) {
  const { data } = await api.post('/captcha', { username, captcha })
  return data
}

export async function adminLogin(username, password, captcha) {
  const { data } = await api.post('/login', { username, password, captcha })
  return data
}

export async function registerAdmin(username, password, currentPassword, currentAdminUsername) {
  const { data } = await api.post('/admin/register', {
    username, password, currentPassword, currentAdminUsername
  })
  return data
}

// ==================== v1 防伪验证（旧） ====================

export async function verifyAntiFakeCodeGet(code) {
  const { data } = await api.get('/verify', { params: { code } })
  return data
}

export async function traceByBatchNumber(batchNumber) {
  const { data } = await api.get(`/trace/batch/${batchNumber}`)
  return data
}

// ==================== v2 防伪验证 ====================

export async function verifyAntiFakeCodeV2(code) {
  const { data } = await api.get('/v2/trace/verify', { params: { code } })
  return data
}

export async function traceByBatchNumberV2(batchNumber) {
  const { data } = await api.get(`/v2/trace/batch/${batchNumber}`)
  return data
}

// ==================== v2 批次管理 ====================

export async function createBatchV2(payload) {
  const { data } = await api.post('/v2/batches', payload)
  return data
}

// ==================== v2 仓储 ====================

export async function recordStorage(payload) {
  const { data } = await api.post('/v2/storage', payload)
  return data
}

// ==================== v2 检验 ====================

export async function completeInspection(payload) {
  const { data } = await api.post('/v2/inspections', payload)
  return data
}

// ==================== 旧 API（保留兼容） ====================

export async function selectProducts(keyword, role = 'consumer') {
  const params = new URLSearchParams()
  if (keyword) params.append('keyword', keyword)
  params.append('role', role)
  const { data } = await api.get(`/products/select?${params.toString()}`)
  return data
}

export async function createComplaint(data) {
  const { data: responseData } = await api.post('/complaint', data)
  return responseData
}

export async function getAllComplaintInfo() {
  const { data } = await api.get('/getAllComplaintInfo')
  return data
}

export async function deleteComplaintInfo(id) {
  const { data } = await api.delete(`/deleteComplaintInfo/${id}`)
  return data
}

export async function createProduct(data) {
  const { data: responseData } = await api.post('/products', data);
  return responseData;
}

export async function updateProduct(id, data) {
  const { data: responseData } = await api.put(`/products/${id}`, data);
  return responseData;
}

export async function deleteProduct(id) {
  const { data: responseData } = await api.delete(`/products/${id}`);
  return responseData;
}

export async function getProducts() {
  const { data } = await api.get('/products');
  return data;
}

export async function listAllProducts() {
  return getProducts();
}

export async function createMaterialPurchase(data) {
  const { data: responseData } = await api.post('/materials', data);
  return responseData;
}

export async function updateMaterialPurchase(id, data) {
  const { data: responseData } = await api.put(`/materials/${id}`, data);
  return responseData;
}

export async function deleteMaterialPurchase(id) {
  const { data: responseData } = await api.delete(`/materials/${id}`);
  return responseData;
}

export async function getMaterials(materialId) {
  const params = materialId ? `?materialId=${materialId}` : ''
  const { data } = await api.get(`/materials${params}`);
  return data;
}

export async function createBatch(data) {
  const { data: responseData } = await api.post('/batches', data);
  return responseData;
}

export async function getBatches(productId) {
  const params = productId ? `?productId=${productId}` : ''
  const { data } = await api.get(`/batches${params}`);
  return data;
}

export async function getBatch(id) {
  const { data } = await api.get(`/batches/${id}`);
  return data;
}

export async function getBatchByNumber(batchNumber) {
  const { data } = await api.get(`/batches/by-number/${batchNumber}`);
  return data;
}

export async function addInspection(batchId, data) {
  const { data: responseData } = await api.post(`/batches/${batchId}/inspection`, data);
  return responseData;
}

export async function addStorage(batchId, data) {
  const { data: responseData } = await api.post(`/batches/${batchId}/storage`, data);
  return responseData;
}

export async function addTransportSale(batchId, data) {
  const { data: responseData } = await api.post(`/batches/${batchId}/transport-sale`, data);
  return responseData;
}

export async function generateSecurityCodes(batchId, quantity) {
  const { data } = await api.post(`/batches/${batchId}/security-codes`, { quantity });
  return data;
}

export async function getSecurityCodes(batchId) {
  const { data } = await api.get(`/batches/${batchId}/security-codes`);
  return data;
}

export async function exportSecurityCodes(batchId) {
  const { data } = await api.get(`/security-codes/export/${batchId}`);
  return data;
}

export async function generateQrCode(productId) {
  const { data } = await api.post(`/insert/products/${productId}/generate-qrcode`);
  return data;
}

export async function batchGenerateQrCodes(productIds) {
  const { data } = await api.post('/insert/products/batch-generate-qrcode', productIds);
  return data;
}

export async function batchDeleteProducts(productIds) {
  const { data } = await api.post('/insert/products/batch-delete', { productIds });
  return data;
}
