<template>
  <header class="header-wrap">
    <div class="container header-inner">
      <router-link class="brand" to="/">
        <span class="brand-mark">FS</span>
        <div class="brand-text">
          <strong>Food Shield</strong>
          <small>食品安全追溯协同平台</small>
        </div>
      </router-link>

      <nav class="top-nav">
        <router-link to="/verify">防伪查询</router-link>
        <router-link to="/complaint">投诉提交</router-link>
        <router-link to="/admin">后台登录</router-link>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header-wrap {
  position: sticky;
  top: 0;
  z-index: 12;
  background: linear-gradient(90deg, rgba(17, 24, 39, 0.92) 0%, rgba(27, 72, 52, 0.92) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(6px);
}

.header-inner {
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: #f8fafc;
}

.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(145deg, #f6ad55 0%, #dd6b20 100%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-text small {
  color: rgba(248, 250, 252, 0.8);
  font-size: 12px;
}

.top-nav {
  display: inline-flex;
  gap: 16px;
}

.top-nav a {
  color: rgba(248, 250, 252, 0.88);
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.top-nav a.router-link-active {
  color: #fff;
  background: rgba(255, 255, 255, 0.16);
}

@media (max-width: 768px) {
  .header-inner {
    min-height: 64px;
    flex-wrap: wrap;
    align-items: flex-start;
    padding-top: 10px;
    padding-bottom: 10px;
  }

  .top-nav {
    width: 100%;
    justify-content: space-between;
    gap: 8px;
  }

  .top-nav a {
    flex: 1;
    text-align: center;
    font-size: 13px;
    padding: 8px 4px;
  }
}
</style>
