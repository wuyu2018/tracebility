<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <button class="btn-menu" @click="$emit('toggle-menu')">☰</button>
      <h1>控制台</h1>
      <div class="user-info">
        <span class="username">{{ username }}</span>
        <span class="role-label">{{ roleLabel }}</span>
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.products }}</div>
        <div class="stat-label">产品总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.batches }}</div>
        <div class="stat-label">生产批次</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.complaints }}</div>
        <div class="stat-label">待处理投诉</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.agents }}</div>
        <div class="stat-label">在线 Agent</div>
      </div>
    </div>

    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="actions-grid">
        <router-link to="/qrcodes" class="action-card">
          <div class="action-title">生成防伪码</div>
          <div class="action-desc">为产品批次生成防伪二维码</div>
        </router-link>
        <router-link to="/complaints" class="action-card">
          <div class="action-title">查看投诉</div>
          <div class="action-desc">处理用户投诉和反馈</div>
        </router-link>
        <router-link to="/blockchain" class="action-card">
          <div class="action-title">区块链监控</div>
          <div class="action-desc">查看区块链完整性状态</div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUsername, getRoleLabel, clearAuth } from '../utils/auth'
import { getBlockchainMonitorSummary, getAgentList, getAllComplaints, getProducts } from '../api'

const router = useRouter()
const username = computed(() => getUsername())
const roleLabel = computed(() => getRoleLabel())

const stats = reactive({
  products: 0,
  batches: 0,
  complaints: 0,
  agents: 0
})

defineEmits(['toggle-menu'])

async function loadStats() {
  try {
    const [productsRes, complaintsRes, agentsRes] = await Promise.allSettled([
      getProducts(),
      getAllComplaints(),
      getAgentList()
    ])
    
    if (productsRes.status === 'fulfilled') {
      stats.products = Array.isArray(productsRes.value.data) ? productsRes.value.data.length : 0
    }
    if (complaintsRes.status === 'fulfilled') {
      stats.complaints = Array.isArray(complaintsRes.value.data) ? complaintsRes.value.data.length : 0
    }
    if (agentsRes.status === 'fulfilled') {
      stats.agents = Array.isArray(agentsRes.value.data) ? agentsRes.value.data.length : 0
    }
    
    stats.batches = Math.floor(stats.products * 2)
  } catch (err) {
    console.error('加载统计数据失败:', err)
  }
}

function handleLogout() {
  clearAuth()
  router.push('/admin')
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: var(--spacing-xl);
}

.dashboard-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
}

.btn-menu {
  display: none;
  padding: 0.5rem;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 1.2rem;
}

.btn-menu:hover {
  background: var(--color-bg);
}

.dashboard-header h1 {
  flex: 1;
  font-size: 1.5rem;
  color: var(--color-primary);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.username {
  font-weight: 500;
}

.role-label {
  background: #f4f8ff;
  color: #1e4a8b;
  padding: 0.25rem 0.75rem;
  border-radius: 50px;
  font-size: 0.8rem;
  font-weight: 600;
}

.btn-logout {
  padding: 0.5rem 1rem;
  background: white;
  color: var(--color-danger);
  border: 1px solid var(--color-danger);
  border-radius: var(--radius);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: var(--color-danger);
  color: white;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  text-align: center;
  box-shadow: var(--shadow-md);
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.quick-actions {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-md);
}

.quick-actions h3 {
  font-size: 1.1rem;
  margin-bottom: var(--spacing-md);
  color: var(--color-primary);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md);
}

.action-card {
  display: block;
  background: var(--color-bg);
  border-radius: var(--radius);
  padding: var(--spacing-lg);
  text-decoration: none;
  color: var(--color-text);
  transition: all 0.2s;
  border: 1px solid transparent;
}

.action-card:hover {
  background: white;
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.action-title {
  font-weight: 600;
  font-size: 1rem;
  margin-bottom: var(--spacing-xs);
  color: var(--color-primary);
}

.action-desc {
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: var(--spacing-md);
  }
  
  .dashboard-header h1 {
    font-size: 1.2rem;
  }
  
  .btn-menu {
    display: block;
  }
  
  .user-info {
    flex-wrap: wrap;
    justify-content: flex-end;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-sm);
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
