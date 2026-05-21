<template>
  <div class="admin-layout">
    <AdminSidebar :is-open="sidebarOpen" />
    
    <div class="admin-content" :class="{ 'sidebar-open': sidebarOpen }">
      <main class="main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AdminSidebar from '../components/AdminSidebar.vue'

const sidebarOpen = ref(false)

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-content {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  background: var(--color-bg);
  transition: margin-left 0.3s;
}

.main {
  padding: var(--spacing-xl);
}

@media (max-width: 1024px) {
  .admin-content {
    margin-left: 0;
  }
}
</style>
