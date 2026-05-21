<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <router-link to="/">食品溯源系统</router-link>
      </div>
      <nav class="nav">
        <router-link to="/admin" class="admin-link">管理员登录</router-link>
      </nav>
    </div>
  </header>
</template>

<script setup>
</script>

<style scoped>
.header {
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  padding: 0 var(--spacing-md);
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo a {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-primary);
}

.admin-link {
  font-size: 0.9rem;
  color: var(--color-text);
  padding: 0.5rem 1rem;
  border-radius: var(--radius);
  transition: background 0.2s;
}

.admin-link:hover {
  background: var(--color-border-light);
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .header {
    padding: 0 var(--spacing-sm);
  }
  
  .logo a {
    font-size: 1.1rem;
  }
  
  .admin-link {
    font-size: 0.85rem;
    padding: 0.4rem 0.8rem;
  }
}
</style>
