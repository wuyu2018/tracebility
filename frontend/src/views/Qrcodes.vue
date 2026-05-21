<template>
  <div class="qrcodes-page">
    <h1>防伪码管理</h1>
    <p class="page-desc">生成和管理产品防伪二维码</p>
    
    <div class="page-content">
      <p style="color: var(--color-text-muted);">
        防伪码批量生成和下载功能
      </p>
    </div>
  </div>
</template>

<style scoped>
.qrcodes-page {
  padding: var(--spacing-lg);
}

.qrcodes-page h1 {
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
