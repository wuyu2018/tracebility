<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
        <div class="modal">
          <div class="modal-header">
            <h3>联系采购</h3>
            <button type="button" class="btn-close" @click="$emit('close')">×</button>
          </div>
          <div class="modal-body">
            <p v-if="!product" class="muted">请先输入防伪码验证产品</p>
            <template v-else>
              <div class="contact-item" v-if="product.contactPhone">
                <span class="label">联系电话</span>
                <a :href="'tel:' + product.contactPhone">{{ product.contactPhone }}</a>
              </div>
              <div class="contact-item" v-if="product.contactEmail">
                <span class="label">邮箱</span>
                <a :href="'mailto:' + product.contactEmail">{{ product.contactEmail }}</a>
              </div>
              <p v-if="!product.contactPhone && !product.contactEmail" class="muted">暂无联系方式</p>
            </template>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
defineProps({
  show: Boolean,
  product: Object,
})

defineEmits(['close'])
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: max(1rem, env(safe-area-inset-top)) max(1rem, env(safe-area-inset-right)) max(1rem, env(safe-area-inset-bottom)) max(1rem, env(safe-area-inset-left));
}

@media (max-width: 480px) {
  .modal-overlay {
    padding: 0.75rem;
  }
  .btn-close {
    min-width: 44px;
    min-height: 44px;
  }
}

.modal {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  max-width: 400px;
  width: 100%;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  background: var(--color-primary);
  color: white;
}

.modal-header h3 {
  font-size: 1.1rem;
  margin: 0;
}

.btn-close {
  background: none;
  color: white;
  font-size: 1.5rem;
  line-height: 1;
  padding: 0 0.5rem;
  opacity: 0.9;
}

.btn-close:hover {
  opacity: 1;
}

.modal-body {
  padding: 1.5rem;
}

.contact-item {
  margin-bottom: 1rem;
}

.contact-item .label {
  display: block;
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.contact-item a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.contact-item a:hover {
  text-decoration: underline;
}

.muted {
  color: var(--color-text-muted);
  margin: 0;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-active .modal,
.modal-leave-active .modal {
  transition: transform 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal,
.modal-leave-to .modal {
  transform: scale(0.95);
}
</style>
