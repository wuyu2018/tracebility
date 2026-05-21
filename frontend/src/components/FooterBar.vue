<template>
  <footer class="footer-wrap">
    <div class="container footer-inner">
      <p>Food Shield Traceability Platform</p>
      <span>全链路记录：原料 -> 生产 -> 仓储 -> 运输销售 -> 质检</span>
    </div>
  </footer>
</template>

<style scoped>
.footer-wrap {
  margin-top: 28px;
  border-top: 1px solid rgba(82, 96, 109, 0.2);
  background: rgba(255, 255, 255, 0.52);
}

.footer-inner {
  min-height: 60px;
  padding: 12px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #486581;
  font-size: 13px;
}

.footer-inner p {
  margin: 0;
  font-weight: 600;
}

.footer-inner span {
  text-align: right;
}

@media (max-width: 768px) {
  .footer-inner {
    min-height: auto;
    flex-direction: column;
    align-items: flex-start;
  }

  .footer-inner span {
    text-align: left;
  }
}
</style>
