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
  background:
    radial-gradient(circle at 0% 0%, rgba(56, 189, 248, 0.08) 0%, transparent 28%),
    radial-gradient(circle at 100% 100%, rgba(16, 185, 129, 0.08) 0%, transparent 34%),
    #f5f8fc;
}

.admin-content {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  background: transparent;
  transition: margin-left 0.3s;
}

.main {
  padding: 1.4rem;
}

@media (max-width: 1024px) {
  .admin-content {
    margin-left: 0;
  }
}
</style>
