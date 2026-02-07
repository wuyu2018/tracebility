<template>
  <section class="intro-section">
    <h3 class="intro-title">公司与产品介绍</h3>
    <div class="intro-blocks">
      <div v-for="s in slides" :key="s.id" class="intro-block">
        <h4>{{ s.title }}</h4>
        <p>{{ s.content }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { introductionSlides } from '../data/introduction.js'

defineProps({
  slides: {
    type: Array,
    default: () => introductionSlides,
  },
})
</script>

<style scoped>
.intro-section {
  padding-top: 2rem;
  border-top: 1px solid rgba(45, 90, 61, 0.12);
}

.intro-title {
  font-size: 1rem;
  color: var(--color-primary);
  margin-bottom: 1.25rem;
  font-weight: 600;
}

.intro-blocks {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.intro-block h4 {
  font-size: 0.9rem;
  color: var(--color-primary-dark);
  margin-bottom: 0.35rem;
  font-weight: 500;
}

.intro-block p {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  line-height: 1.65;
  margin: 0;
}

@media (max-width: 480px) {
  .intro-section {
    margin-top: 2rem;
    padding-top: 1.5rem;
  }
  .intro-block p {
    font-size: 0.825rem;
  }
}
</style>
