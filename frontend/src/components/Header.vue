<template>
  <header class="header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <span class="logo-icon">🌾</span>
        <span class="logo-text">食品溯源</span>
      </router-link>
      <nav class="nav">
        <router-link to="/purchase" class="btn-purchase">采购联系</router-link>
        <div class="dropdown" ref="dropdownRef">
          <button class="dropdown-trigger" @click="toggleDropdown" :class="{ active: isOpen }">
            更多服务
            <span class="dropdown-arrow">▼</span>
          </button>
          <transition name="dropdown">
            <ul v-show="isOpen" class="dropdown-menu">
              <li>
                <button type="button" class="dropdown-item" @click="goToIntro">
                  <span class="item-icon">📋</span>
                  <span>公司与产品介绍</span>
                </button>
              </li>
            </ul>
          </transition>
        </div>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isOpen = ref(false)
const dropdownRef = ref(null)

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const goToPurchase = () => {
  isOpen.value = false
  router.push('/purchase')
}

const goToIntro = () => {
  isOpen.value = false
  router.push('/intro')
}

const handleClickOutside = (e) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, var(--color-primary-dark) 0%, var(--color-primary) 100%);
  color: white;
  padding: max(1rem, env(safe-area-inset-top)) 2rem 1rem;
  box-shadow: var(--shadow-md);
}

@media (max-width: 768px) {
  .header {
    padding: max(0.875rem, env(safe-area-inset-top)) 1rem 0.875rem;
  }
}

@media (max-width: 480px) {
  .header {
    padding: max(0.75rem, env(safe-area-inset-top)) 0.875rem 0.75rem;
  }
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 480px) {
  .header-inner {
    gap: 0.5rem;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: white;
  font-weight: 600;
  font-size: 1.25rem;
}

@media (max-width: 480px) {
  .logo {
    font-size: 1.1rem;
  }
  .logo-icon {
    font-size: 1.35rem;
  }
}

.logo-icon {
  font-size: 1.5rem;
}

.nav {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-purchase {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: var(--radius);
  font-size: 0.95rem;
  text-decoration: none;
  transition: background 0.2s;
  min-height: 44px;
  display: flex;
  align-items: center;
}

.btn-purchase:hover {
  background: rgba(255, 255, 255, 0.3);
}

@media (max-width: 480px) {
  .btn-purchase {
    padding: 0.5rem 0.75rem;
    font-size: 0.9rem;
  }
}

.dropdown-trigger {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: var(--radius);
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  transition: background 0.2s;
  min-height: 44px;
  min-width: 44px;
  justify-content: center;
}

@media (max-width: 480px) {
  .dropdown-trigger {
    padding: 0.6rem 0.875rem;
    font-size: 0.9rem;
  }
}

.dropdown-trigger:hover,
.dropdown-trigger.active {
  background: rgba(255, 255, 255, 0.25);
}

.dropdown-arrow {
  font-size: 0.6rem;
  transition: transform 0.2s;
}

.dropdown-trigger.active .dropdown-arrow {
  transform: rotate(180deg);
}

.dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  background: var(--color-bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  list-style: none;
  min-width: 280px;
  max-width: min(280px, calc(100vw - 2rem));
  overflow: hidden;
  z-index: 100;
}

@media (max-width: 480px) {
  .dropdown-menu {
    left: 0.5rem;
    right: 0.5rem;
    min-width: auto;
    max-width: none;
  }
}

.dropdown-item {
  width: 100%;
  padding: 0.75rem 1.25rem;
  text-align: left;
  color: var(--color-text);
  background: none;
  font-size: 0.95rem;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-height: 48px;
}

@media (max-width: 480px) {
  .dropdown-item {
    padding: 1rem 1.25rem;
    min-height: 52px;
  }
}

.dropdown-item:hover {
  background: var(--color-bg);
}

.item-icon {
  font-size: 1.1rem;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
