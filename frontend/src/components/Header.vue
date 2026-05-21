<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <router-link to="/">
          <span class="logo-mark">FS</span>
          <span class="logo-text">食品溯源系统</span>
        </router-link>
      </div>
      <nav class="nav">
        <router-link to="/verify" class="nav-link">产品验证</router-link>
        <router-link to="/complaint" class="nav-link">投诉入口</router-link>
        <router-link to="/admin" class="admin-link">管理员登录</router-link>
      </nav>
    </div>
  </header>
</template>

<script setup>
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.82);
  border-bottom: 1px solid #dbe5ef;
  backdrop-filter: blur(8px);
  padding: 0 var(--spacing-md);
  height: 64px;
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
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
  color: #0f172a;
}

.logo-mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 0.85rem;
  font-weight: 800;
  background: linear-gradient(130deg, #1d4ed8 0%, #0ea5e9 100%);
}

.logo-text {
  font-size: 1.1rem;
  font-weight: 700;
}

.nav {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-link,
.admin-link {
  font-size: 0.9rem;
  color: #334155;
  padding: 0.5rem 1rem;
  border-radius: 10px;
  transition: background 0.2s, color 0.2s;
}

.nav-link:hover,
.admin-link:hover {
  background: #eaf3ff;
  color: #1d4ed8;
}

.admin-link {
  border: 1px solid #c7dfff;
  background: #f5f9ff;
  font-weight: 600;
}

@media (max-width: 768px) {
  .header {
    padding: 0 var(--spacing-sm);
  }

  .nav {
    gap: 0.25rem;
  }
  
  .logo-mark {
    width: 30px;
    height: 30px;
  }

  .logo-text {
    font-size: 0.95rem;
  }

  .nav-link,
  .admin-link {
    font-size: 0.78rem;
    padding: 0.35rem 0.55rem;
  }
}
</style>
