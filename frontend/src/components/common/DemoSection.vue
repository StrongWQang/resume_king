<template>
  <section id="demo" class="demo-section">
    <div class="demo-content">
      <div class="demo-header">
        <h2 class="demo-title-massive">
          <span class="demo-word">观看</span>
          <span class="demo-word highlight-demo">演示</span>
        </h2>
        <p class="demo-subtitle-enhanced">
          通过视频演示，了解如何使用我们的模板快速创建专业简历
        </p>
      </div>
      
      <div class="demo-showcase">
        <div class="demo-frame-container">
          <div class="demo-frame-wrapper">
            <iframe 
              src="https://scribehow.com/embed/Create_a_Resume_Using__Template__WV9EcHihSPi7YTNpidKy7Q?as=video"
              width="100%" 
              height="100%" 
              allow="fullscreen" 
              frameborder="0"
              class="demo-iframe"
              title="简历制作演示视频"
            />
          </div>
          <div class="demo-overlay-controls">
            <button class="demo-fullscreen-btn" @click="toggleFullscreen">
              <i class="icon-fullscreen"></i>
            </button>
          </div>
        </div>
        
        <div class="demo-features">
          <div class="demo-feature-item" v-for="(feature, index) in demoFeatures" :key="index">
            <div class="demo-feature-icon">
              <i :class="feature.icon"></i>
            </div>
            <div class="demo-feature-content">
              <h4 class="demo-feature-title">{{ feature.title }}</h4>
              <p class="demo-feature-desc">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
      
<!--      <div class="demo-actions">-->
<!--        <button class="demo-cta-button" @click="goToEditor">-->
<!--          <span class="demo-button-text">立即开始制作</span>-->
<!--          <div class="demo-button-arrow">→</div>-->
<!--        </button>-->
<!--      </div>-->
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const demoFeatures = ref([
  {
    icon: 'icon-play',
    title: '实时预览',
    description: '观看简历制作的完整过程，实时看到每个步骤的效果'
  },
  {
    icon: 'icon-template',
    title: '模板应用',
    description: '学习如何选择和应用不同的专业模板'
  },
  {
    icon: 'icon-edit',
    title: '内容编辑',
    description: '了解如何编辑和优化简历内容'
  },
  {
    icon: 'icon-export',
    title: '导出技巧',
    description: '掌握如何导出和分享你的简历'
  }
])

const goToEditor = () => {
  router.push('/editor')
}

const toggleFullscreen = () => {
  const iframe = document.querySelector('.demo-iframe') as HTMLIFrameElement
  if (iframe) {
    if (iframe.requestFullscreen) {
      iframe.requestFullscreen()
    }
  }
}

onMounted(() => {
  // 添加滚动动画
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in')
      }
    })
  })
  
  document.querySelectorAll('.demo-feature-item').forEach(item => {
    observer.observe(item)
  })
})
</script>

<style scoped>
.demo-section {
  padding: 8rem 0;
  background: #0a0a0a;
  position: relative;
  width: 100%;
  margin: 0;
  overflow: hidden;
  left: 0;
  right: 0;
}

.demo-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.demo-header {
  text-align: center;
  margin-bottom: 4rem;
}

.demo-title-massive {
  font-size: 4rem;
  font-weight: 700;
  color: white;
  margin-bottom: 1rem;
  margin-top: 0;
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  overflow: hidden;
}

.demo-word {
  display: inline-block;
  margin-right: 0.5rem;
  opacity: 0.9;
  transform-origin: center bottom;
  will-change: transform;
}

.demo-word.highlight-demo {
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.demo-word.highlight-demo::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(45deg, #10b981, #34d399);
  border-radius: 2px;
}

.demo-subtitle-enhanced {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.7);
  max-width: 600px;
  margin: 0 auto;
  font-weight: 300;
}

.demo-showcase {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 4rem;
  align-items: start;
  margin-bottom: 4rem;
}

.demo-frame-container {
  position: relative;
  width: 100%;
  height: 600px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.demo-frame-container:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.4);
  border-color: rgba(16, 185, 129, 0.3);
}

.demo-frame-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  border-radius: 20px;
  overflow: hidden;
}

.demo-iframe {
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 20px;
  background: #000;
}

.demo-overlay-controls {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}

.demo-fullscreen-btn {
  width: 40px;
  height: 40px;
  background: rgba(16, 185, 129, 0.8);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.demo-fullscreen-btn:hover {
  background: rgba(16, 185, 129, 1);
  transform: scale(1.1);
}

.demo-features {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.demo-feature-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateX(30px);
}

.demo-feature-item:hover {
  transform: translateX(0);
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.3);
}

.demo-feature-item.animate-in {
  opacity: 1;
  transform: translateX(0);
  transition: all 0.6s ease;
}

.demo-feature-item:nth-child(1) { transition-delay: 0.1s; }
.demo-feature-item:nth-child(2) { transition-delay: 0.2s; }
.demo-feature-item:nth-child(3) { transition-delay: 0.3s; }
.demo-feature-item:nth-child(4) { transition-delay: 0.4s; }

.demo-feature-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  background: linear-gradient(45deg, #10b981, #34d399);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.demo-feature-content {
  flex: 1;
  color: white;
}

.demo-feature-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  margin-top: 0;
  color: #10b981;
}

.demo-feature-desc {
  font-size: 0.9rem;
  opacity: 0.8;
  line-height: 1.5;
  margin: 0;
  font-weight: 300;
}

.demo-actions {
  text-align: center;
}

.demo-cta-button {
  display: inline-flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 2.5rem;
  background: linear-gradient(45deg, #10b981, #34d399);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(16, 185, 129, 0.3);
  will-change: transform;
}

.demo-cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(16, 185, 129, 0.4);
  background: linear-gradient(45deg, #059669, #10b981);
}

.demo-button-text {
  position: relative;
  z-index: 1;
}

.demo-button-arrow {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
  position: relative;
  z-index: 1;
}

.demo-cta-button:hover .demo-button-arrow {
  transform: translateX(5px);
}

/* Icons */
.icon-play::before { content: '▶️'; }
.icon-template::before { content: '📄'; }
.icon-edit::before { content: '✏️'; }
.icon-export::before { content: '💾'; }
.icon-fullscreen::before { content: '⛶'; }

/* 响应式设计 */
@media (max-width: 968px) {
  .demo-showcase {
    grid-template-columns: 1fr;
    gap: 3rem;
  }
  
  .demo-frame-container {
    height: 400px;
  }
}

@media (max-width: 768px) {
  .demo-section {
    padding: 4rem 0;
  }
  
  .demo-content {
    padding: 0 1rem;
  }
  
  .demo-title-massive {
    font-size: 3rem;
  }
  
  .demo-frame-container {
    height: 350px;
  }
  
  .demo-showcase {
    gap: 2rem;
  }
  
  .demo-features {
    gap: 1.5rem;
  }
  
  .demo-feature-item {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .demo-title-massive {
    font-size: 2.5rem;
  }
  
  .demo-frame-container {
    height: 300px;
  }
  
  .demo-feature-item {
    flex-direction: column;
    text-align: center;
    gap: 0.5rem;
  }
  
  .demo-feature-icon {
    align-self: center;
  }
}
</style> 