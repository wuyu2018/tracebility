<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-links">
        <router-link to="/">产品溯源</router-link>
        <a href="#contact">联系我们</a>
        <router-link to="/complaint">投诉入口</router-link>
      </div>
      <div class="footer-text">
        &copy; 2025 食品溯源系统 · 安全可追溯
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.footer {
  background: linear-gradient(180deg, #f8fbff 0%, #f1f6fc 100%);
  border-top: 1px solid #dbe5ef;
  padding: var(--spacing-lg) var(--spacing-md);
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
}

.footer-links {
  display: flex;
  gap: var(--spacing-lg);
}

.footer-links a {
  color: #4b5563;
  font-size: 0.9rem;
}

.footer-links a:hover {
  color: #1d4ed8;
}

.footer-text {
  color: #64748b;
  font-size: 0.85rem;
}

@media (max-width: 768px) {
  .footer {
    padding: var(--spacing-md) var(--spacing-sm);
  }
  
  .footer-links {
    gap: var(--spacing-md);
  }
  
  .footer-links a {
    font-size: 0.85rem;
  }
}
</style>
