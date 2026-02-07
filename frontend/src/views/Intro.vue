<template>
  <div class="intro-page">
    <div class="intro-hero">
      <h1>公司与产品介绍</h1>
      <p>了解我们的理念与保障</p>
    </div>

    <div class="intro-content">
      <section v-for="slide in slides" :key="slide.id" class="intro-section">
        <h2>{{ slide.title }}</h2>
        <p>{{ slide.content }}</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { introductionSlides } from '../data/introduction.js'

const slides = introductionSlides
</script>

<style scoped>
.intro-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 2rem 1.5rem 4rem;
  padding-bottom: max(4rem, env(safe-area-inset-bottom));
}

@media (max-width: 768px) {
  .intro-page {
    padding: 1.5rem 1rem 3rem;
  }
}

@media (max-width: 480px) {
  .intro-page {
    padding: 1rem 0.875rem 2.5rem;
  }
}

.intro-hero {
  text-align: center;
  margin-bottom: 2.5rem;
}

.intro-hero h1 {
  font-size: 1.75rem;
  color: var(--color-primary-dark);
  margin-bottom: 0.5rem;
}

@media (max-width: 480px) {
  .intro-hero h1 {
    font-size: 1.4rem;
  }
  .intro-hero p {
    font-size: 0.875rem;
  }
}

.intro-hero p {
  color: var(--color-text-muted);
  font-size: 0.95rem;
}

.intro-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.intro-section {
  background: var(--color-bg-card);
  padding: 1.75rem 2rem;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(45, 90, 61, 0.06);
}

@media (max-width: 480px) {
  .intro-section {
    padding: 1.25rem 1rem;
  }
  .intro-section p {
    font-size: 0.9rem;
  }
}

.intro-section h2 {
  font-size: 1.1rem;
  color: var(--color-primary);
  margin-bottom: 0.75rem;
  font-weight: 600;
}

.intro-section p {
  color: var(--color-text-muted);
  line-height: 1.8;
  margin: 0;
}
</style>
