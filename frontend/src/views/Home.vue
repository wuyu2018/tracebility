<template>
  <section class="container home">
    <div class="page-card hero">
      <p class="kicker">FOOD TRACEABILITY</p>
      <h1 class="title">后端驱动的前端录入与追溯工作台</h1>
      <p class="subtitle">
        我已按后端接口设计前端流程，覆盖原料品种、采购、批次、仓储、运输销售、质检、投诉与防伪查询。
      </p>
      <div class="actions">
        <router-link class="action primary" to="/verify">立即防伪查询</router-link>
        <router-link class="action" to="/admin">进入后台录入</router-link>
      </div>
    </div>

    <div class="grid">
      <article class="page-card item" v-for="item in modules" :key="item.name">
        <h3>{{ item.name }}</h3>
        <p>{{ item.desc }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
const modules = [
  { name: '原料品种', desc: '对接 /api/v2/material-varieties，维护可用原料字典。' },
  { name: '原料采购', desc: '对接 /api/v2/material-purchases，录入供应批次与数量。' },
  { name: '生产批次', desc: '对接 /api/v2/batches，绑定产品与多条采购记录。' },
  { name: '仓储记录', desc: '对接 /api/v2/storage，提交入库出库与库位信息。' },
  { name: '运输销售', desc: '对接 /api/v2/transport-sales，提交运输公司与收货信息。' },
  { name: '质检报告', desc: '对接 /api/v2/inspections，提交合格结论与不合格原因。' },
]
</script>

<style scoped>
.home {
  padding: 26px 0;
}

.hero {
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.95) 0%, rgba(237, 247, 241, 0.96) 100%);
}

.kicker {
  margin: 0 0 8px;
  color: #2f855a;
  letter-spacing: 0.12em;
  font-size: 12px;
  font-weight: 700;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  padding: 0 16px;
  border-radius: 10px;
  border: 1px solid #cbd5e0;
  background: #fff;
}

.action.primary {
  border: none;
  background: #2f855a;
  color: #fff;
}

.grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.item h3 {
  margin: 0;
}

.item p {
  margin: 8px 0 0;
  color: #52606d;
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
