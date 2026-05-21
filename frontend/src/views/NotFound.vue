<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面未找到</p>
    <router-link to="/" class="btn-home">返回首页</router-link>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  background: var(--color-bg);
}

.not-found h1 {
  font-size: 6rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
}

.not-found p {
  font-size: 1.2rem;
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xl);
}

.btn-home {
  padding: 1rem 2rem;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius);
  font-weight: 600;
  transition: background 0.2s;
}

.btn-home:hover {
  background: var(--color-primary-light);
}
</style>
