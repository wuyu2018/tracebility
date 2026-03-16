import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export async function verifyAntiFakeCode(antiFakeCode) {
  const { data } = await api.post('/verify', { antiFakeCode })
  return data
}

export async function getPurchaseInfo(antiFakeCode) {
  const { data } = await api.post('/purchase-info', { antiFakeCode })
  return data
}

export async function listAllProducts() {
  const { data } = await api.post('/products/list')
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

export async function createProduct(data) {
  const { data: responseData } = await api.post('/insert/products', data);
  return responseData;
}

export async function deleteComplaintInfo(id) {
  const { data } = await api.delete(`/deleteComplaintInfo/${id}`)
  return data
}

export async function createMaterialPurchase(data) {
  const { data: responseData } = await api.post('/insert/material-purchase', data);
  return responseData;
}

export async function createInspection(data) {
  const { data: responseData } = await api.post('/insert/inspection', data);
  return responseData;
}

export async function createStorage(data) {
  const { data: responseData } = await api.post('/insert/storage', data);
  return responseData;
}

export async function createTransportSale(data) {
  const { data: responseData } = await api.post('/insert/transport-sale', data);
  return responseData;
}
