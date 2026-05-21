<template>
  <div class="agents-page">
    <h1>Agent 监控</h1>
    <p class="page-desc">查看多智能体系统状态</p>
    
    <div class="page-content">
      <el-table :data="agents" style="width: 100%">
        <el-table-column prop="agentId" label="Agent ID" />
        <el-table-column prop="agentType" label="类型" width="120">
          <template #default="scope">
            <span>{{ translateType(scope.row.agentType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="state" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.state === 'ACTIVE' ? 'success' : 'info'">
              {{ scope.row.state }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信誉分" width="100" />
        <el-table-column prop="authorized" label="授权" width="80">
          <template #default="scope">
            {{ scope.row.authorized ? '✓' : '✗' }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAgentList } from '../api'

const agents = ref([])

function translateType(type) {
  const map = {
    PRODUCTION: '生产方',
    CIRCULATION: '流通方',
    SALES: '销售方',
    CA: '证书机构'
  }
  return map[type] || type
}

onMounted(async () => {
  try {
    const res = await getAgentList()
    agents.value = res.data || []
  } catch (err) {
    console.error('加载 Agent 列表失败:', err)
  }
})
</script>

<style scoped>
.agents-page {
  padding: var(--spacing-lg);
}

.agents-page h1 {
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
}

.page-desc {
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xl);
}

.page-content {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-md);
}
</style>
