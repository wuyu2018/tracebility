<template>
  <div class="purchase-page">
    <div class="purchase-hero">
      <h1>采购联系</h1>
      <p>浏览全部产品，选择您需要的商品进行采购咨询</p>
    </div>

    <div v-if="loading" class="loading-wrap">
      <span class="loading-spinner"></span>
      <p>加载中...</p>
    </div>

    <div v-else-if="products.length" class="product-grid">
      <div
        v-for="p in products"
        :key="p.productId"
        class="product-card"
      >
        <div class="product-image-wrap">
          <img :src="getImageUrl(p)" :alt="p.name" class="product-image" />
        </div>
        <div class="product-body">
          <h3 class="product-name">{{ p.name }}</h3>
          <p v-if="p.specification" class="product-spec">{{ p.specification }}</p>
          <div class="contact-actions">
            <a v-if="p.contactPhone" :href="'tel:' + p.contactPhone" class="btn-contact">
              📞 电话联系
            </a>
            <a v-if="p.contactEmail" :href="'mailto:' + p.contactEmail" class="btn-contact btn-email">
              ✉️ 邮件咨询
            </a>
            <p v-if="!p.contactPhone && !p.contactEmail" class="no-contact">暂无联系方式</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无产品信息</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listAllProducts } from '../services/api'

const products = ref([])
const loading = ref(true)

function getImageUrl(p) {
  if (!p?.imageUrl) return '/img/products/placeholder.svg'
  if (p.imageUrl.startsWith('http')) return p.imageUrl
  return p.imageUrl
}

onMounted(async () => {
  try {
    const list = await listAllProducts()
    products.value = Array.isArray(list) ? list : []
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.purchase-page {
  max-width: 1100px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .purchase-page {
    padding: 0 0.5rem;
  }
}

@media (max-width: 480px) {
  .purchase-page {
    padding: 0 0.25rem;
  }
}

.purchase-hero {
  text-align: center;
  margin-bottom: 2.5rem;
}

@media (max-width: 480px) {
  .purchase-hero {
    margin-bottom: 1.5rem;
  }
}

.purchase-hero h1 {
  font-size: 1.75rem;
  color: var(--color-primary-dark);
  margin-bottom: 0.5rem;
}

@media (max-width: 480px) {
  .purchase-hero h1 {
    font-size: 1.4rem;
  }
  .purchase-hero p {
    font-size: 0.875rem;
  }
}

.purchase-hero p {
  color: var(--color-text-muted);
  font-size: 0.95rem;
}

.loading-wrap {
  text-align: center;
  padding: 4rem;
}

.loading-spinner {
  display: inline-block;
  width: 40px;
  height: 40px;
  border: 3px solid #e8efe9;
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-wrap p {
  margin-top: 1rem;
  color: var(--color-text-muted);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1.5rem;
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 1rem;
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}

.product-card {
  background: var(--color-bg-card);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.25s, transform 0.25s;
  border: 1px solid rgba(45, 90, 61, 0.06);
}

.product-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.product-image-wrap {
  aspect-ratio: 1;
  background: linear-gradient(145deg, #f5f8f6 0%, #eef3f0 100%);
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-body {
  padding: 1.25rem;
}

@media (max-width: 480px) {
  .product-body {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .btn-contact {
    min-height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.product-name {
  font-size: 1.05rem;
  color: var(--color-primary-dark);
  margin-bottom: 0.35rem;
  line-height: 1.4;
}

.product-spec {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-bottom: 1rem;
}

.contact-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.btn-contact {
  display: inline-block;
  padding: 0.5rem 0.75rem;
  background: var(--color-primary);
  color: white !important;
  text-decoration: none;
  font-size: 0.875rem;
  border-radius: 6px;
  text-align: center;
  transition: background 0.2s;
}

.btn-contact:hover {
  background: var(--color-primary-light);
}

.btn-email {
  background: rgba(45, 90, 61, 0.85);
}

.btn-email:hover {
  background: var(--color-primary);
}

.no-contact {
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin: 0;
}

.empty-state {
  text-align: center;
  padding: 4rem;
  color: var(--color-text-muted);
}
</style>
