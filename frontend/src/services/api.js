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
