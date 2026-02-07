<template>
  <div class="purchase-view">
    <PurchasePage />
  </div>
</template>

<script setup>
import PurchasePage from '../components/PurchasePage.vue'
</script>

<style scoped>
.purchase-view {
  width: 100%;
  padding: 2rem 1.5rem 4rem;
  padding-bottom: max(4rem, env(safe-area-inset-bottom));
}

@media (max-width: 768px) {
  .purchase-view {
    padding: 1.5rem 1rem 3rem;
  }
}

@media (max-width: 480px) {
  .purchase-view {
    padding: 1rem 0.875rem 2.5rem;
  }
}
</style>
