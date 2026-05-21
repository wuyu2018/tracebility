<template>
  <section class="admin-shell">
    <aside class="sidebar">
      <div class="brand">Food Shield Console</div>
      <p class="identity">{{ username }} · {{ roleLabel }}</p>
      <nav class="menu-wrap">
        <router-link v-for="item in nav" :key="item.to" :to="item.to" class="menu-link">
          {{ item.label }}
        </router-link>
      </nav>
    </aside>
    <div class="content-wrap">
      <div class="content-top">
        <h1>后台管理</h1>
        <span>数据录入与追溯管理统一工作台</span>
      </div>
      <main class="content-main">
        <router-view />
      </main>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { hasRoutePermission, getRoleLabel, getUsername } from '../utils/auth'

const navAll = [
  { label: '概览', to: '/manage/dashboard', allow: [] },
  { label: '产品管理', to: '/manage/products', allow: ['PRODUCTION'] },
  { label: '原料品种', to: '/manage/materials/varieties', allow: ['PRODUCTION'] },
  { label: '原料采购', to: '/manage/materials/purchases', allow: ['PRODUCTION'] },
  { label: '生产批次', to: '/manage/production/batches', allow: ['PRODUCTION'] },
  { label: '仓储录入', to: '/manage/circulation/storage', allow: ['PRODUCTION'] },
  { label: '运输销售', to: '/manage/circulation/transport', allow: ['CIRCULATION', 'SALES'] },
  { label: '质检录入', to: '/manage/production/inspections', allow: ['PRODUCTION'] },
  { label: '防伪码生成', to: '/manage/qrcodes', allow: ['PRODUCTION'] },
  { label: '投诉管理', to: '/manage/complaints', allow: [] },
  { label: '区块链监控', to: '/manage/blockchain', allow: [] },
  { label: 'Agent监控', to: '/manage/agents', allow: [] },
]

const nav = computed(() => navAll.filter((item) => hasRoutePermission(item.allow)))
const roleLabel = getRoleLabel()
const username = getUsername() || '未知用户'
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  background: #eef2f6;
}

.sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 20px 14px;
  border-right: 1px solid #d7dee7;
  background: linear-gradient(180deg, #102a43 0%, #1f3a56 100%);
}

.brand {
  color: #f5f7fa;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.identity {
  margin: 8px 0 16px;
  color: rgba(245, 247, 250, 0.82);
  font-size: 13px;
}

.menu-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.menu-link {
  display: block;
  padding: 10px 12px;
  border-radius: 10px;
  color: rgba(245, 247, 250, 0.88);
  transition: background-color 0.2s ease, color 0.2s ease;
}

.menu-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.menu-link.router-link-active {
  background: #1f6f78;
  color: #ffffff;
}

.content-wrap {
  min-width: 0;
  padding: 18px;
}

.content-top {
  background: #ffffff;
  border: 1px solid #d8e1e9;
  border-radius: 14px;
  padding: 14px 18px;
  margin-bottom: 14px;
}

.content-top h1 {
  margin: 0;
  font-size: 20px;
}

.content-top span {
  display: block;
  margin-top: 4px;
  color: #5b6b79;
  font-size: 13px;
}

.content-main {
  min-width: 0;
}

@media (max-width: 900px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    height: auto;
  }
}
</style>
