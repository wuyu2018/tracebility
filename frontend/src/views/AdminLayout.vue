<template>
  <section class="container admin-layout">
    <aside class="page-card sidebar">
      <h3>业务菜单</h3>
      <p class="identity">当前登录：{{ username }}（{{ roleLabel }}）</p>
      <router-link v-for="item in nav" :key="item.to" :to="item.to" class="menu-link">
        {{ item.label }}
      </router-link>
    </aside>
    <div class="content">
      <router-view />
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { hasRoutePermission, getRoleLabel, getUsername } from '../utils/auth'

const navAll = [
  { label: '概览', to: '/dashboard', allow: [] },
  { label: '产品管理', to: '/products', allow: ['PRODUCTION'] },
  { label: '原料品种', to: '/materials/varieties', allow: ['PRODUCTION'] },
  { label: '原料采购', to: '/materials/purchases', allow: ['PRODUCTION'] },
  { label: '生产批次', to: '/production/batches', allow: ['PRODUCTION'] },
  { label: '仓储录入', to: '/circulation/storage', allow: ['PRODUCTION'] },
  { label: '运输销售', to: '/circulation/transport', allow: ['CIRCULATION', 'SALES'] },
  { label: '质检录入', to: '/production/inspections', allow: ['PRODUCTION'] },
  { label: '防伪码生成', to: '/qrcodes', allow: ['PRODUCTION'] },
  { label: '投诉管理', to: '/complaints', allow: [] },
  { label: '区块链监控', to: '/blockchain', allow: [] },
  { label: 'Agent监控', to: '/agents', allow: [] },
]

const nav = computed(() => navAll.filter((item) => hasRoutePermission(item.allow)))
const roleLabel = getRoleLabel()
const username = getUsername() || '未知用户'
</script>

<style scoped>
.admin-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 14px;
  padding: 20px 0;
}

.sidebar {
  align-self: start;
  position: sticky;
  top: 88px;
}

.menu-link {
  display: block;
  padding: 10px 12px;
  border-radius: 10px;
  margin-top: 6px;
  background: rgba(255, 255, 255, 0.7);
}

.menu-link.router-link-active {
  background: #2f855a;
  color: #fff;
}

.identity {
  margin: 8px 0 10px;
  color: #486581;
  font-size: 13px;
}

@media (max-width: 900px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
  }
}
</style>
