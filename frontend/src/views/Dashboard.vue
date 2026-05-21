<template>
  <section class="page-card">
    <h2 class="title">后端对接总览</h2>
    <p class="subtitle">核心录入接口已在前端形成提交表单，提交数据结构与后端 API 文档字段保持一致。</p>
    <ul class="dash-list">
      <li v-for="item in items" :key="item">{{ item }}</li>
    </ul>
  </section>
</template>

<script setup>
const items = [
  'POST /api/v2/material-varieties 创建原料品种',
  'POST /api/v2/material-purchases 创建原料采购',
  'POST /api/v2/batches 创建生产批次',
  'POST /api/v2/storage 录入仓储信息',
  'POST /api/v2/transport-sales 录入运输销售',
  'POST /api/v2/inspections 提交质检报告',
]
</script>

<style scoped>
.dash-list {
  margin: 14px 0 0;
  padding-left: 18px;
  color: #334e68;
  line-height: 1.8;
}
</style>
