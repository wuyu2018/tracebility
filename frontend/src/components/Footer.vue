<template>
  <footer class="footer">
    <div class="footer-inner">
      <p>食品溯源系统 · 保障食品安全 · 让每一口都放心</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--color-primary-dark);
  color: rgba(255, 255, 255, 0.85);
  padding: 1.5rem 2rem;
  padding-bottom: max(1.5rem, env(safe-area-inset-bottom));
  text-align: center;
  margin-top: auto;
}

@media (max-width: 768px) {
  .footer {
    padding: 1.25rem 1rem;
  }
}

@media (max-width: 480px) {
  .footer {
    padding: 1rem 0.875rem;
  }
  .footer p {
    font-size: 0.85rem;
    line-height: 1.5;
  }
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.footer p {
  font-size: 0.9rem;
}
</style>
