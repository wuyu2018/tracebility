<template>
  <div class="sidebar" :class="{ 'sidebar-open': isOpen }">
    <div class="sidebar-header">
      <span class="sidebar-title">食品安全溯源系统</span>
    </div>
    
    <nav class="sidebar-nav">
      <div class="nav-group">
        <router-link to="/dashboard" class="nav-item" active-class="active">
          控制台
        </router-link>
      </div>

      <div class="nav-group" v-if="showMenu('products')">
        <div class="nav-item-title" @click="toggleGroup('products')">
          产品管理
          <span class="nav-arrow">{{ expandedGroups.has('products') ? '▲' : '▼' }}</span>
        </div>
        <div class="nav-submenu" v-show="expandedGroups.has('products')">
          <router-link to="/products" class="nav-item nav-subitem">产品列表</router-link>
        </div>
      </div>

      <div class="nav-group" v-if="showMenu('materials')">
        <div class="nav-item-title" @click="toggleGroup('materials')">
          原料管理
          <span class="nav-arrow">{{ expandedGroups.has('materials') ? '▲' : '▼' }}</span>
        </div>
        <div class="nav-submenu" v-show="expandedGroups.has('materials')">
          <router-link to="/materials/varieties" class="nav-item nav-subitem">品种管理</router-link>
          <router-link to="/materials/purchases" class="nav-item nav-subitem">采购录入</router-link>
        </div>
      </div>

      <div class="nav-group" v-if="showMenu('production')">
        <div class="nav-item-title" @click="toggleGroup('production')">
          生产数据
          <span class="nav-arrow">{{ expandedGroups.has('production') ? '▲' : '▼' }}</span>
        </div>
        <div class="nav-submenu" v-show="expandedGroups.has('production')">
          <router-link to="/production/batches" class="nav-item nav-subitem">批次创建</router-link>
          <router-link to="/production/inspections" class="nav-item nav-subitem">质检报告</router-link>
        </div>
      </div>

      <div class="nav-group" v-if="showMenu('circulation')">
        <div class="nav-item-title" @click="toggleGroup('circulation')">
          流通数据
          <span class="nav-arrow">{{ expandedGroups.has('circulation') ? '▲' : '▼' }}</span>
        </div>
        <div class="nav-submenu" v-show="expandedGroups.has('circulation')">
          <router-link to="/circulation/storage" class="nav-item nav-subitem">仓储录入</router-link>
          <router-link to="/circulation/transport" class="nav-item nav-subitem">运输录入</router-link>
        </div>
      </div>

      <div class="nav-group" v-if="showMenu('sales')">
        <div class="nav-item-title" @click="toggleGroup('sales')">
          销售数据
          <span class="nav-arrow">{{ expandedGroups.has('sales') ? '▲' : '▼' }}</span>
        </div>
        <div class="nav-submenu" v-show="expandedGroups.has('sales')">
          <router-link to="/sales/records" class="nav-item nav-subitem">销售录入</router-link>
        </div>
      </div>

      <div class="nav-group" v-if="showMenu('qrcodes')">
        <router-link to="/qrcodes" class="nav-item">
          防伪码管理
        </router-link>
      </div>

      <div class="nav-group" v-if="showMenu('complaints')">
        <router-link to="/complaints" class="nav-item">
          投诉管理
        </router-link>
      </div>

      <div class="nav-group" v-if="showMenu('blockchain')">
        <router-link to="/blockchain" class="nav-item">
          区块链监控
        </router-link>
      </div>

      <div class="nav-group" v-if="showMenu('agents')">
        <router-link to="/agents" class="nav-item">
          Agent 监控
        </router-link>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getRole, getAgentType } from '../utils/auth'

defineProps({
  isOpen: Boolean
})

const emit = defineEmits(['close'])

const expandedGroups = ref(new Set(['products']))

function toggleGroup(group) {
  if (expandedGroups.has(group)) {
    expandedGroups.value.delete(group)
  } else {
    expandedGroups.value.add(group)
  }
  expandedGroups.value = new Set(expandedGroups.value)
}

const role = computed(() => getRole())
const agentType = computed(() => getAgentType())

function showMenu(menu) {
  if (role.value === 'SUPER_ADMIN') return true
  if (!agentType.value) {
    return menu !== 'agents'
  }
  
  const permissions = {
    products: ['PRODUCTION'],
    materials: ['PRODUCTION'],
    production: ['PRODUCTION'],
    circulation: ['CIRCULATION'],
    sales: ['SALES'],
    qrcodes: ['PRODUCTION'],
    complaints: [],
    blockchain: [],
    agents: []
  }
  
  const allowed = permissions[menu] || []
  if (allowed.length === 0) {
    return ['complaints', 'blockchain'].includes(menu)
  }
  
  return allowed.includes(agentType.value)
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 240px;
  background: linear-gradient(180deg, #0f172a 0%, #162342 100%);
  border-right: 1px solid rgba(148, 163, 184, 0.22);
  overflow-y: auto;
  transition: transform 0.3s;
  z-index: 100;
}

.sidebar-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.sidebar-title {
  font-size: 1rem;
  font-weight: 700;
  color: #e2e8f0;
  letter-spacing: 0.02em;
}

.sidebar-nav {
  padding: var(--spacing-sm) 0;
}

.nav-group {
  margin-bottom: 2px;
}

.nav-item {
  display: block;
  padding: var(--spacing-sm) var(--spacing-lg);
  color: #cbd5e1;
  font-size: 0.9rem;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background: rgba(59, 130, 246, 0.16);
  color: #f8fafc;
}

.nav-item.active {
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.35), rgba(14, 165, 233, 0.1));
  color: #ffffff;
  border-left: 3px solid #38bdf8;
}

.nav-item-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-lg);
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  color: #e2e8f0;
  transition: background 0.2s;
}

.nav-item-title:hover {
  background: rgba(59, 130, 246, 0.16);
}

.nav-arrow {
  font-size: 0.75rem;
  color: #94a3b8;
}

.nav-submenu {
  background: rgba(15, 23, 42, 0.42);
}

.nav-subitem {
  padding-left: calc(var(--spacing-lg) + 16px);
  font-size: 0.85rem;
}

@media (max-width: 1024px) {
  .sidebar {
    transform: translateX(-100%);
  }
  
  .sidebar-open {
    transform: translateX(0);
  }
}
</style>
