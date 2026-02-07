<template>
  <div class="carousel">
    <div class="carousel-track">
      <Transition name="carousel-fade" mode="out-in">
        <div v-if="slideList.length && slideList[currentIndex]" :key="slideList[currentIndex].id" class="carousel-slide">
          <h3 class="slide-title">{{ slideList[currentIndex].title }}</h3>
          <p class="slide-content">{{ slideList[currentIndex].content }}</p>
        </div>
      </Transition>
    </div>
    <div class="carousel-dots">
      <button
        v-for="(s, i) in slideList"
        :key="s.id"
        type="button"
        class="dot"
        :class="{ active: i === currentIndex }"
        :aria-label="'切换到第' + (i + 1) + '页'"
        @click="goTo(i)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { introductionSlides } from '../data/introduction.js'

const props = defineProps({
  slides: {
    type: Array,
    default: () => introductionSlides,
  },
  interval: {
    type: Number,
    default: 5000,
  },
})

const slideList = computed(() => props.slides || introductionSlides)
const currentIndex = ref(0)
let timer = null

function goTo(i) {
  currentIndex.value = i
  resetTimer()
}

function next() {
  const len = slideList.value.length
  if (len) currentIndex.value = (currentIndex.value + 1) % len
}

function resetTimer() {
  if (timer) clearInterval(timer)
  timer = setInterval(next, props.interval)
}

onMounted(() => {
  resetTimer()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.carousel {
  background: linear-gradient(180deg, rgba(245, 250, 247, 0.9) 0%, rgba(255, 255, 255, 0.6) 100%);
  border-radius: var(--radius-lg);
  padding: 1.75rem 2rem;
  margin-bottom: 2rem;
  border: 1px solid rgba(45, 90, 61, 0.06);
}

@media (max-width: 768px) {
  .carousel {
    padding: 1.5rem 1.25rem;
    margin-bottom: 1.5rem;
  }
}

@media (max-width: 480px) {
  .carousel {
    padding: 1.25rem 1rem;
    margin-bottom: 1.25rem;
  }
}

.carousel-track {
  min-height: 90px;
}

@media (max-width: 480px) {
  .carousel-track {
    min-height: 80px;
  }
}

.carousel-slide {
  text-align: center;
}

.slide-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
  letter-spacing: 0.02em;
}

.slide-content {
  font-size: 0.95rem;
  color: var(--color-text-muted);
  line-height: 1.7;
  max-width: 560px;
  margin: 0 auto;
}

@media (max-width: 480px) {
  .slide-title {
    font-size: 0.95rem;
  }
  .slide-content {
    font-size: 0.875rem;
    line-height: 1.65;
  }
}

.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1rem;
}

.dot {
  width: 6px;
  height: 6px;
  padding: 9px;
  border-radius: 50%;
  background: rgba(45, 90, 61, 0.2);
  background-clip: content-box;
  border: none;
  cursor: pointer;
  transition: background 0.3s, transform 0.2s;
  flex-shrink: 0;
}

.dot:hover {
  background: rgba(45, 90, 61, 0.4);
}

.dot.active {
  background: var(--color-primary);
  background-clip: content-box;
  transform: scale(1.2);
}

.carousel-fade-enter-active,
.carousel-fade-leave-active {
  transition: opacity 0.6s ease;
}

.carousel-fade-enter-from,
.carousel-fade-leave-to {
  opacity: 0;
}
</style>
