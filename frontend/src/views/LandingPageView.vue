<template>
  <div class="landing-page">
    <!-- 加载动画 -->
    <div class="loading-overlay" id="loadingOverlay">
      <div class="loading-content">
        <div class="loading-logo">
          <div class="logo-circle"></div>
          <div class="logo-text">Resume King</div>
        </div>
        <div class="loading-progress">
          <div class="progress-bar"></div>
        </div>
      </div>
    </div>
    
    <!-- 导航栏 -->
    <nav class="storytelling-nav">
      <div class="nav-container">
        <div class="nav-logo">
          <img src="/logo-large.jpg" alt="Resume King Logo" class="nav-logo-img" />
          <span class="nav-logo-text">Resume King</span>
        </div>
        <div class="nav-menu">
          <a href="#story" class="nav-link" @click="smoothScrollTo('story', $event)">故事</a>
          <a href="#projects" class="nav-link" @click="smoothScrollTo('projects', $event)">项目</a>
          <a href="#features" class="nav-link" @click="smoothScrollTo('features', $event)">功能</a>
          <button class="nav-btn nav-btn-secondary" @click="goToAuthor">关于作者</button>
          <button class="nav-btn nav-btn-primary" @click="goToEditor">开始制作</button>
        </div>
      </div>
    </nav>
    
    <!-- Hero 区域 - 超大文字震撼式登场 -->
    <section class="hero-storytelling">
      <div class="hero-background">
        <div class="floating-elements">
          <div class="float-element" v-for="n in 30" :key="n"></div>
        </div>
        <div class="hero-particles">
          <div class="particle" v-for="n in 50" :key="n"></div>
        </div>
      </div>
      
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-text">免费       开源       自由</span>
          <div class="badge-glow"></div>
        </div>
        
        <h1 class="hero-title-massive">
          <div class="title-line">
            <span class="title-word-massive">创造</span>
          </div>
          <div class="title-line">
            <span class="title-word-massive">属于你的</span>
          </div>
          <div class="title-line highlight-line">
            <span class="title-word-massive highlight-massive">顶级简历</span>
          </div>
        </h1>
        
        <p class="hero-description-enhanced">
          <span class="description-line">一个简历制作网站旨在通过免费和自由的体验来吸引您的受众。</span>
          <span class="description-line">专注于精心策划的体验和贴心，讲述沉浸式的职业故事。</span>
        </p>
        
        <div class="hero-actions">
          <button class="cta-button-enhanced" @click="goToEditor">
            <span class="button-text">开始您的故事</span>
            <div class="button-arrow">→</div>
            <div class="button-ripple"></div>
          </button>
        </div>
      </div>
      
      <div class="scroll-hint">
        <div class="scroll-text">向下滚动探索</div>
        <div class="scroll-line"></div>
      </div>
    </section>

    <!-- 故事介绍区域 - 大文字冲击 -->
    <section id="story" class="story-section">
      <div class="story-content">
        <div class="story-text">
          <h2 class="story-title-massive">为什么选择<br><span class="highlight-text">数字化简历？</span></h2>
          <p class="story-description-enhanced">
            在数字时代，传统简历已经不能满足现代职场的需求。我们的平台让您能够创建
            <strong>互动性强、视觉吸引力突出</strong>的数字简历，
            通过动画效果和精心设计的布局来讲述您的职业故事。
          </p>
        </div>
        <div class="story-visual">
          <div class="visual-card">
            <div class="card-content">
              <div class="card-icon">📄</div>
              <h3>传统简历</h3>
              <p>静态、单调、难以突出</p>
            </div>
          </div>
          <div class="transform-arrow">→</div>
          <div class="visual-card featured">
            <div class="card-content">
              <div class="card-icon">✨</div>
              <h3>数字简历</h3>
              <p>动态、交互、引人注目</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 项目展示区域 - 大标题冲击 -->
    <section id="projects" class="projects-section">
      <div class="section-header">
        <h2 class="section-title-massive">
          <span class="title-word-animate">我们引以为豪的</span>
          <span class="title-word-animate highlight-word">项目</span>
        </h2>
      </div>
      
      <div class="projects-grid">
        <div class="project-item" v-for="(project, index) in projects" :key="index">
          <div class="project-visual">
            <div class="project-image">
              <div class="project-placeholder">{{ project.icon }}</div>
            </div>
            <div class="project-overlay">
              <button class="project-btn" @click="goToEditor">查看项目</button>
            </div>
          </div>
          <div class="project-content">
            <h3 class="project-title">{{ project.title }}</h3>
            <p class="project-description">{{ project.description }}</p>
            <a href="#" class="project-link" @click="goToEditor">
              <span>开始制作</span>
              <div class="link-arrow">→</div>
            </a>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特色区域 - 震撼式大文字 -->
    <section id="features" class="features-storytelling">
      <div class="features-content">
        <div class="features-header">
          <h2 class="features-title-massive">
            <span class="features-word">强大功能</span>
            <span class="features-word highlight-features">无限可能</span>
          </h2>
          <p class="features-subtitle-enhanced">体验我们为您精心打造的专业工具</p>
        </div>
        
        <div class="features-showcase">
          <div class="feature-highlight" v-for="(feature, index) in features" :key="index">
            <div class="feature-visual">
              <div class="feature-icon-large">
                <i :class="feature.icon"></i>
              </div>
            </div>
            <div class="feature-content">
              <h3 class="feature-name-massive">{{ feature.title }}</h3>
              <p class="feature-desc-enhanced">{{ feature.description }}</p>
              <div class="feature-benefits">
                <div class="benefit-item" v-for="benefit in feature.benefits" :key="benefit">
                  <div class="benefit-check">✓</div>
                  <span>{{ benefit }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计数据区域 - 大数字冲击 -->
    <section class="stats-storytelling">
      <div class="stats-content">
        <h2 class="stats-title-massive">
          <span class="stats-word">数字告诉我们的</span>
          <span class="stats-word highlight-stats">故事</span>
        </h2>
        <div class="stats-grid">
          <div class="stat-card" v-for="(stat, index) in stats" :key="index">
            <div class="stat-visual">
              <div class="stat-circle">
                <div class="stat-number-massive">{{ stat.number }}</div>
              </div>
            </div>
            <div class="stat-label-enhanced">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 行动号召区域 - 超大文字震撼 -->
    <section class="cta-storytelling">
      <div class="cta-content">
        <h2 class="cta-title-massive">
          <span class="cta-word">准备开始您的</span>
          <span class="cta-word highlight-cta">数字故事</span>
          <span class="cta-word">了吗？</span>
        </h2>
        <p class="cta-subtitle-enhanced">
          加入数万名用户的选择，让我们帮您打造令人印象深刻的数字简历
        </p>
        <button class="cta-main-button-enhanced" @click="goToEditor">
          <span class="main-button-text">开始创作</span>
          <div class="button-glow-enhanced"></div>
          <div class="button-waves"></div>
        </button>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="storytelling-footer">
      <div class="footer-content">
        <div class="footer-logo">
          <img src="/logo-large.jpg" alt="Resume King Logo" />
          <span>Resume King</span>
        </div>
        <div class="footer-links">
          <a href="#" @click="goToAuthor">关于作者</a>
          <a href="#" @click="goToGitHub">开源项目</a>
          <a href="#" @click="goToEditor">开始制作</a>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2025 Resume King  开源   免费   自由</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { ScrollToPlugin } from 'gsap/ScrollToPlugin'
import { TextPlugin } from 'gsap/TextPlugin'

gsap.registerPlugin(ScrollTrigger, ScrollToPlugin, TextPlugin)

const router = useRouter()

const projects = ref([
  {
    icon: '🎨',
    title: '创意简历设计',
    description: '通过交互式设计和动画效果，为您的简历注入活力'
  },
  {
    icon: '💼',
    title: '专业简历模板',
    description: '多种行业专属模板，满足不同职业需求'
  },
  {
    icon: '🚀',
    title: 'AI智能优化',
    description: '基于AI技术，智能优化简历内容和结构'
  },
  {
    icon: '📊',
    title: '数据分析面板',
    description: '实时分析简历效果，提供优化建议'
  }
])

const features = ref([
  {
    icon: 'icon-ai',
    title: 'AI智能优化',
    description: '基于AI技术，自动优化简历内容，提升面试通过率',
    benefits: ['智能内容建议', '关键词优化', '格式自动调整']
  },
  {
    icon: 'icon-template',
    title: '多样模板',
    description: '提供多种专业模板，适合不同行业和职位需求',
    benefits: ['50+专业模板', '行业定制', '实时预览']
  },
  {
    icon: 'icon-export',
    title: '一键导出',
    description: '支持PDF、Word等多种格式导出，满足不同需求',
    benefits: ['多格式支持', '高清导出', '快速下载']
  },
  {
    icon: 'icon-cloud',
    title: '云端存储',
    description: '简历云端保存，随时随地访问和编辑您的简历',
    benefits: ['云端同步', '版本控制', '安全备份']
  }
])

const stats = ref([
  { number: '50K+', label: '用户选择' },
  { number: '100+', label: '专业模板' },
  { number: '99%', label: '满意度' },
  { number: '24/7', label: '在线服务' }
])

const goToEditor = () => {
  router.push('/editor')
}

const goToGitHub = () => {
  window.open('https://github.com/resume-king/resume_king', '_blank')
}

const goToAuthor = () => {
  window.open('https://github.com/StrongWQang', '_blank')
}

const smoothScrollTo = (targetId: string, event: Event) => {
  event.preventDefault()
  const targetElement = document.getElementById(targetId)
  if (targetElement) {
    gsap.to(window, {
      duration: 1.5,
      scrollTo: {
        y: targetElement,
        offsetY: 80
      },
      ease: 'power3.inOut'
    })
  }
}

let cleanupNavScrollEffect: (() => void) | null = null

onMounted(() => {
  // 确保body样式正确应用
  document.body.style.margin = '0'
  document.body.style.padding = '0'
  document.body.style.overflowX = 'hidden'
  document.body.style.background = '#0a0a0a'
  document.body.style.width = '100%'
  document.documentElement.style.margin = '0'
  document.documentElement.style.padding = '0'
  document.documentElement.style.overflowX = 'hidden'
  document.documentElement.style.background = '#0a0a0a'
  document.documentElement.style.width = '100%'
  
  // 加载动画
  const loadingOverlay = document.getElementById('loadingOverlay')
  
  // 2秒后隐藏加载动画并开始页面动画
  setTimeout(() => {
    if (loadingOverlay) {
      gsap.to(loadingOverlay, {
        opacity: 0,
        duration: 0.5,
        ease: 'power2.out',
        onComplete: () => {
          loadingOverlay.style.display = 'none'
          
          // 开始页面动画
          initAnimations()
          
          // 浮动元素动画
          animateFloatingElements()
          
          // 导航栏滚动效果
          cleanupNavScrollEffect = setupNavScrollEffect()
          
          // 磁性效果
          setupMagneticEffect()
          
          // 3D变换动画
          setup3DAnimations()
          
          // 文字动画
          setupTextAnimations()
          
          // 专业的交互动画
          setupAdvancedAnimations()
          
          // 鼠标跟随效果
          setupMouseFollowEffect()
        }
      })
    }
  }, 2000)
})

onUnmounted(() => {
  if (cleanupNavScrollEffect) {
    cleanupNavScrollEffect()
  }
})

const initAnimations = () => {
  // 导航栏动画
  gsap.from('.storytelling-nav', {
    y: -100,
    opacity: 0,
    duration: 1,
    ease: 'power3.out'
  })
  
  // 页面加载动画 - 超震撼大文字登场
  const tl = gsap.timeline({ delay: 0.3 })
  
  // Hero 徽章动画 - 更震撼的登场
  tl.from('.hero-badge', {
    y: 50,
    opacity: 0,
    scale: 0.5,
    rotationX: 90,
    duration: 1.2,
    ease: 'back.out(2.5)'
  })
  
  // 超大标题动画 - 每个字都有震撼的3D效果
  .from('.title-word-massive', {
    y: 200,
    opacity: 0,
    rotationX: 90,
    rotationY: 45,
    scale: 0.3,
    transformOrigin: 'center bottom',
    duration: 1.8,
    stagger: 0.3,
    ease: 'power4.out'
  }, '-=0.5')
  
  // 标题高亮词更震撼的效果
  .from('.highlight-massive', {
    backgroundPosition: '200% center',
    duration: 2,
    ease: 'power2.out'
  }, '-=1')
  
  // 描述文字 - 逐行震撼出现
  .from('.description-line', {
    y: 50,
    opacity: 0,
    rotationX: 45,
    duration: 1,
    stagger: 0.3,
    ease: 'power3.out'
  }, '-=0.8')
  
  // 按钮震撼登场
  .from('.cta-button-enhanced', {
    y: 60,
    opacity: 0,
    scale: 0.5,
    rotationX: 90,
    duration: 1.2,
    ease: 'back.out(2)'
  }, '-=0.5')
  
  // 滚动提示动画
  .from('.scroll-hint', {
    y: 30,
    opacity: 0,
    duration: 0.8,
    ease: 'power2.out'
  }, '-=0.3')

  // 滚动触发动画 - 超震撼的大标题出现
  
  // 故事标题震撼登场
  ScrollTrigger.batch('.story-title-massive', {
    onEnter: (elements) => {
      gsap.from(elements, {
        y: 150,
        opacity: 0,
        rotationX: 90,
        scale: 0.5,
        duration: 1.5,
        ease: 'power4.out'
      })
    },
    start: 'top 80%'
  })
  
  // 项目标题震撼登场
  ScrollTrigger.batch('.section-title-massive', {
    onEnter: (elements) => {
      gsap.from('.title-word-animate', {
        y: 200,
        opacity: 0,
        rotationX: 90,
        rotationY: 45,
        scale: 0.3,
        duration: 1.8,
        stagger: 0.4,
        ease: 'power4.out'
      })
    },
    start: 'top 80%'
  })
  
  // 功能标题震撼登场
  ScrollTrigger.batch('.features-title-massive', {
    onEnter: (elements) => {
      gsap.from('.features-word', {
        y: 180,
        opacity: 0,
        rotationX: 90,
        rotationY: -45,
        scale: 0.4,
        duration: 1.6,
        stagger: 0.3,
        ease: 'power4.out'
      })
    },
    start: 'top 80%'
  })
  
  // 统计标题震撼登场
  ScrollTrigger.batch('.stats-title-massive', {
    onEnter: (elements) => {
      gsap.from('.stats-word', {
        y: 200,
        opacity: 0,
        rotationX: 90,
        rotationY: 30,
        scale: 0.3,
        duration: 1.8,
        stagger: 0.5,
        ease: 'power4.out'
      })
    },
    start: 'top 80%'
  })
  
  // CTA标题震撼登场
  ScrollTrigger.batch('.cta-title-massive', {
    onEnter: (elements) => {
      gsap.from('.cta-word', {
        y: 220,
        opacity: 0,
        rotationX: 90,
        rotationY: -60,
        scale: 0.2,
        duration: 2,
        stagger: 0.4,
        ease: 'power4.out'
      })
    },
    start: 'top 80%'
  })

  ScrollTrigger.batch('.visual-card', {
    onEnter: (elements) => {
      gsap.from(elements, {
        y: 80,
        opacity: 0,
        rotationY: 60,
        scale: 0.6,
        duration: 1.2,
        stagger: 0.2,
        ease: 'back.out(2)'
      })
    },
    start: 'top 80%'
  })

  ScrollTrigger.batch('.project-item', {
    onEnter: (elements) => {
      gsap.from(elements, {
        y: 120,
        opacity: 0,
        rotationX: 45,
        scale: 0.6,
        duration: 1.4,
        stagger: 0.3,
        ease: 'back.out(2)'
      })
    },
    start: 'top 80%'
  })

  ScrollTrigger.batch('.feature-highlight', {
    onEnter: (elements) => {
      gsap.from(elements, {
        y: 150,
        opacity: 0,
        rotationX: 60,
        duration: 1.6,
        stagger: 0.4,
        ease: 'power3.out'
      })
    },
    start: 'top 80%'
  })

  ScrollTrigger.batch('.stat-card', {
    onEnter: (elements) => {
      gsap.from(elements, {
        scale: 0.3,
        opacity: 0,
        rotationX: 180,
        rotationY: 180,
        duration: 1.5,
        stagger: 0.2,
        ease: 'back.out(2)'
      })
    },
    start: 'top 80%'
  })

  // 数字计数动画
  ScrollTrigger.create({
    trigger: '.stats-storytelling',
    start: 'top 80%',
    onEnter: () => {
      document.querySelectorAll('.stat-number-massive').forEach((el, index) => {
        const finalText = el.textContent
        const isNumber = /^\d+/.test(finalText)
        
        if (isNumber) {
          const number = parseInt(finalText)
          gsap.to({ val: 0 }, {
            val: number,
            duration: 2,
            ease: 'power2.out',
            delay: index * 0.2,
            onUpdate: function() {
              el.textContent = Math.floor(this.targets()[0].val) + (finalText.includes('K') ? 'K+' : finalText.includes('%') ? '%' : '')
            }
          })
        }
      })
    }
  })

  // 按钮悬停动画
  document.querySelectorAll('.btn').forEach(btn => {
    btn.addEventListener('mouseenter', () => {
      gsap.to(btn, {
        scale: 1.05,
        duration: 0.3,
        ease: 'power2.out'
      })
    })
    
    btn.addEventListener('mouseleave', () => {
      gsap.to(btn, {
        scale: 1,
        duration: 0.3,
        ease: 'power2.out'
      })
    })
  })
}

const animateFloatingElements = () => {
  const floatingElements = document.querySelectorAll('.float-element')
  
  floatingElements.forEach((element, index) => {
    gsap.set(element, {
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      scale: Math.random() * 0.5 + 0.5,
      opacity: Math.random() * 0.5 + 0.2
    })
    
    gsap.to(element, {
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      duration: Math.random() * 15 + 10,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut'
    })
  })
}



const setupNavScrollEffect = () => {
  let lastScrollY = 0
  
  const handleScroll = () => {
    const currentScrollY = window.scrollY
    const nav = document.querySelector('.landing-nav')
    
    if (nav) {
      if (currentScrollY > lastScrollY && currentScrollY > 100) {
        // 向下滚动时隐藏导航栏
        gsap.to(nav, {
          y: -100,
          duration: 0.3,
          ease: 'power2.out'
        })
      } else {
        // 向上滚动时显示导航栏
        gsap.to(nav, {
          y: 0,
          duration: 0.3,
          ease: 'power2.out'
        })
      }
      
      // 改变导航栏的背景透明度
      if (currentScrollY > 50) {
        gsap.to(nav, {
          backgroundColor: 'rgba(255, 255, 255, 0.98)',
          duration: 0.3,
          ease: 'power2.out'
        })
      } else {
        gsap.to(nav, {
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          duration: 0.3,
          ease: 'power2.out'
        })
      }
    }
    
    lastScrollY = currentScrollY
  }
  
  window.addEventListener('scroll', handleScroll)
  
  // 在组件销毁时移除事件监听器
  return () => {
    window.removeEventListener('scroll', handleScroll)
  }
}

// 磁性效果
const setupMagneticEffect = () => {
  const magneticElements = document.querySelectorAll('.btn, .feature-card, .nav-btn')
  
  magneticElements.forEach(element => {
    element.addEventListener('mousemove', (e) => {
      const rect = element.getBoundingClientRect()
      const x = e.clientX - rect.left - rect.width / 2
      const y = e.clientY - rect.top - rect.height / 2
      
      gsap.to(element, {
        x: x * 0.2,
        y: y * 0.2,
        duration: 0.3,
        ease: 'power2.out'
      })
    })
    
    element.addEventListener('mouseleave', () => {
      gsap.to(element, {
        x: 0,
        y: 0,
        duration: 0.5,
        ease: 'elastic.out(1, 0.3)'
      })
    })
  })
}

// 3D变换动画
const setup3DAnimations = () => {
  // 功能卡片的3D效果
  const featureCards = document.querySelectorAll('.feature-card')
  
  featureCards.forEach(card => {
    card.addEventListener('mouseenter', () => {
      gsap.to(card, {
        rotationX: 5,
        rotationY: 5,
        z: 50,
        duration: 0.5,
        ease: 'power2.out'
      })
    })
    
    card.addEventListener('mouseleave', () => {
      gsap.to(card, {
        rotationX: 0,
        rotationY: 0,
        z: 0,
        duration: 0.5,
        ease: 'power2.out'
      })
    })
    
    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      
      const rotationX = (y - rect.height / 2) / rect.height * -20
      const rotationY = (x - rect.width / 2) / rect.width * 20
      
      gsap.to(card, {
        rotationX: rotationX,
        rotationY: rotationY,
        duration: 0.3,
        ease: 'power2.out'
      })
    })
  })
  
  // 统计数据的3D效果
  const statItems = document.querySelectorAll('.stat-item')
  
  statItems.forEach(item => {
    item.addEventListener('mouseenter', () => {
      gsap.to(item, {
        rotationX: 10,
        z: 30,
        duration: 0.4,
        ease: 'back.out(1.7)'
      })
    })
    
    item.addEventListener('mouseleave', () => {
      gsap.to(item, {
        rotationX: 0,
        z: 0,
        duration: 0.4,
        ease: 'back.out(1.7)'
      })
    })
  })
}

// 文字动画
const setupTextAnimations = () => {
  // 为标题添加分割动画
  const titleLines = document.querySelectorAll('.title-line')
  
  titleLines.forEach(line => {
    const text = line.textContent
    line.innerHTML = ''
    
    // 分割字符
    for (let i = 0; i < text.length; i++) {
      const char = text[i]
      const span = document.createElement('span')
      span.textContent = char === ' ' ? '\u00A0' : char
      span.style.display = 'inline-block'
      line.appendChild(span)
    }
    
    // 添加悬停动画
    line.addEventListener('mouseenter', () => {
      gsap.to(line.children, {
        y: -10,
        rotationX: 360,
        duration: 0.6,
        stagger: 0.05,
        ease: 'back.out(1.7)'
      })
    })
    
    line.addEventListener('mouseleave', () => {
      gsap.to(line.children, {
        y: 0,
        rotationX: 0,
        duration: 0.6,
        stagger: 0.05,
        ease: 'back.out(1.7)'
      })
    })
  })
  
  // 为功能标题添加文字动画
  const featureTitles = document.querySelectorAll('.feature-title')
  
  featureTitles.forEach(title => {
    title.addEventListener('mouseenter', () => {
      gsap.to(title, {
        color: '#10b981',
        scale: 1.1,
        duration: 0.3,
        ease: 'power2.out'
      })
    })
    
    title.addEventListener('mouseleave', () => {
      gsap.to(title, {
        color: '#2c3e50',
        scale: 1,
        duration: 0.3,
        ease: 'power2.out'
      })
    })
  })
}

// 高级动画效果
const setupAdvancedAnimations = () => {
  // 为功能图标添加浮动动画
  const featureIcons = document.querySelectorAll('.feature-icon-large')
  
  featureIcons.forEach((icon, index) => {
    gsap.to(icon, {
      y: Math.sin(index) * 15,
      duration: 3 + Math.random() * 2,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut',
      delay: index * 0.3
    })
  })
  
  // 为导航logo添加旋转动画
  const navLogo = document.querySelector('.nav-logo-img')
  if (navLogo) {
    gsap.to(navLogo, {
      rotation: 360,
      duration: 15,
      repeat: -1,
      ease: 'none'
    })
  }
  
  // 为统计圆圈添加脉冲动画
  const statCircles = document.querySelectorAll('.stat-circle')
  statCircles.forEach((circle, index) => {
    gsap.to(circle, {
      scale: 1.05,
      duration: 2,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut',
      delay: index * 0.2
    })
  })
  
  // 为主要CTA按钮添加呼吸动画
  const ctaButton = document.querySelector('.cta-main-button')
  if (ctaButton) {
    gsap.to(ctaButton, {
      boxShadow: '0 12px 48px rgba(16, 185, 129, 0.5)',
      duration: 1.5,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut'
    })
  }
  
  // 为滚动线条添加动画
  const scrollLine = document.querySelector('.scroll-line')
  if (scrollLine) {
    gsap.to(scrollLine, {
      scaleY: 1.5,
      duration: 1.5,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut'
    })
  }
  
  // 为视觉卡片添加轻微的摇摆动画
  const visualCards = document.querySelectorAll('.visual-card')
  visualCards.forEach((card, index) => {
    gsap.to(card, {
      rotationY: 3,
      duration: 4,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut',
      delay: index * 0.5
    })
  })
}

// 鼠标跟随效果
const setupMouseFollowEffect = () => {
  let mouseX = 0
  let mouseY = 0
  
  const handleMouseMove = (e: MouseEvent) => {
    mouseX = e.clientX
    mouseY = e.clientY
    
    // 浮动元素跟随鼠标轻微移动
    const floatingElements = document.querySelectorAll('.float-element')
    floatingElements.forEach((element, index) => {
      const speed = 0.01 + index * 0.005
      const x = (mouseX - window.innerWidth / 2) * speed
      const y = (mouseY - window.innerHeight / 2) * speed
      
      gsap.to(element, {
        x: x,
        y: y,
        duration: 2,
        ease: 'power2.out'
      })
    })
    
    // 导航栏元素轻微倾斜
    const nav = document.querySelector('.storytelling-nav')
    if (nav) {
      const tiltX = (mouseX - window.innerWidth / 2) / window.innerWidth * 1
      const tiltY = (mouseY - window.innerHeight / 2) / window.innerHeight * 1
      
      gsap.to(nav, {
        rotationX: tiltY,
        rotationY: tiltX,
        duration: 1,
        ease: 'power2.out'
      })
    }
    
    // 功能图标跟随鼠标移动
    const featureIcons = document.querySelectorAll('.feature-icon-large')
    featureIcons.forEach((icon, index) => {
      const speed = 0.03 + index * 0.01
      const x = (mouseX - window.innerWidth / 2) * speed
      const y = (mouseY - window.innerHeight / 2) * speed
      
      gsap.to(icon, {
        x: x,
        y: y,
        duration: 1.5,
        ease: 'power2.out'
      })
    })
  }
  
  window.addEventListener('mousemove', handleMouseMove)
  
  return () => {
    window.removeEventListener('mousemove', handleMouseMove)
  }
}
</script>

<style scoped>
/* Global Styles - 彻底去掉所有白边 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  margin: 0;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
  background: #0a0a0a;
}

body {
  margin: 0;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
  background: #0a0a0a;
  position: relative;
}

.landing-page {
  min-height: 100vh;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  background: #0a0a0a;
  color: #ffffff;
  opacity: 0;
  animation: fadeIn 0.5s ease-out 2.5s forwards;
  position: relative;
  left: 0;
  right: 0;
  top: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Loading Animation */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
  color: white;
}

.loading-logo {
  margin-bottom: 2rem;
}

.logo-circle {
  width: 80px;
  height: 80px;
  border: 2px solid rgba(16, 185, 129, 0.3);
  border-top: 2px solid #10b981;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: 3px;
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.loading-progress {
  width: 200px;
  height: 2px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1px;
  overflow: hidden;
  margin: 0 auto;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #34d399);
  border-radius: 1px;
  animation: progress 2s ease-in-out;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes progress {
  0% { width: 0%; }
  100% { width: 100%; }
}

/* Storytelling Navigation */
.storytelling-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(10, 10, 10, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  transform-style: preserve-3d;
  perspective: 1000px;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.nav-logo-img {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: 2px solid rgba(16, 185, 129, 0.3);
  transition: border-color 0.3s ease;
}

.nav-logo-img:hover {
  border-color: #10b981;
}

.nav-logo-text {
  font-size: 1.6rem;
  font-weight: 700;
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 2.5rem;
}

.nav-link {
  color: #ffffff;
  text-decoration: none;
  font-weight: 500;
  font-size: 1rem;
  transition: all 0.3s ease;
  position: relative;
  padding: 0.5rem 0;
}

.nav-link:hover {
  color: #10b981;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(45deg, #10b981, #34d399);
  transition: width 0.3s ease;
}

.nav-link:hover::after {
  width: 100%;
}

.nav-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  font-size: 0.9rem;
  position: relative;
  overflow: hidden;
  will-change: transform;
}

.nav-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.nav-btn:hover::before {
  left: 100%;
}

.nav-btn-primary {
  background: linear-gradient(45deg, #10b981, #34d399);
  color: white;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
}

.nav-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
  background: linear-gradient(45deg, #059669, #10b981);
}

.nav-btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
  backdrop-filter: blur(10px);
}

.nav-btn-secondary:hover {
  background: rgba(16, 185, 129, 0.1);
  border-color: #10b981;
  transform: translateY(-2px);
  color: #34d399;
}

/* Hero Storytelling Section */
.hero-storytelling {
  position: relative;
  height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  overflow: hidden;
  padding: 0;
  margin: 0;
  left: 0;
  right: 0;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  transform-style: preserve-3d;
  perspective: 1000px;
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.float-element {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(16, 185, 129, 0.3);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.float-element:nth-child(2n) {
  background: rgba(52, 211, 153, 0.2);
  animation-delay: 1s;
}

.float-element:nth-child(3n) {
  background: rgba(110, 231, 183, 0.2);
  animation-delay: 2s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
    opacity: 1;
  }
}

.hero-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.particle {
  position: absolute;
  width: 10px;
  height: 10px;
  background: rgba(16, 185, 129, 0.3);
  border-radius: 50%;
  animation: particle-float 8s ease-in-out infinite;
}

.particle:nth-child(2n) {
  background: rgba(52, 211, 153, 0.2);
  animation-delay: 1s;
}

.particle:nth-child(3n) {
  background: rgba(110, 231, 183, 0.2);
  animation-delay: 2s;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0px) translateX(0px) scale(0.5);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-30px) translateX(20px) scale(0.8);
    opacity: 1;
  }
}


.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  max-width: 900px;
  padding: 0 2rem;
  margin: 0 auto;
}

.hero-badge {
  display: inline-block;
  padding: 0.5rem 1rem;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 20px;
  color: #10b981;
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 2rem;
  letter-spacing: 1px;
  text-transform: uppercase;
  position: relative;
  overflow: hidden;
}

.badge-text {
  position: relative;
  z-index: 1;
}

.badge-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.3), transparent 70%);
  border-radius: 50%;
  opacity: 0.5;
  animation: pulse-glow 3s infinite;
  z-index: 0;
}

@keyframes pulse-glow {
  0% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.3;
  }
  100% {
    transform: scale(1);
    opacity: 0.5;
  }
}

.hero-title-massive {
  font-size: 6rem;
  font-weight: 900;
  margin-bottom: 2rem;
  margin-top: 0;
  line-height: 1.1;
  letter-spacing: -0.02em;
  position: relative;
  overflow: hidden;
}

.title-line {
  display: inline-block;
  margin-bottom: -0.5em; /* Adjust for line height */
}

.title-word-massive {
  display: inline-block;
  margin-right: 0.5rem;
  opacity: 0.9;
  transform-origin: center bottom;
  will-change: transform;
}

.title-word-massive.highlight-massive {
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.title-word-massive.highlight-massive::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(45deg, #10b981, #34d399);
  border-radius: 2px;
}

.hero-description-enhanced {
  font-size: 1.3rem;
  margin-bottom: 3rem;
  margin-top: 0;
  opacity: 0.8;
  line-height: 1.6;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
  font-weight: 300;
  position: relative;
  overflow: hidden;
}

.description-line {
  display: inline-block;
  margin-bottom: 0.5em; /* Adjust for line height */
}

.hero-actions {
  display: flex;
  justify-content: center;
  margin-bottom: 4rem;
}



.cta-button-enhanced {
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

.cta-button-enhanced:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(16, 185, 129, 0.4);
  background: linear-gradient(45deg, #059669, #10b981);
}

.button-text {
  position: relative;
  z-index: 1;
}

.button-arrow {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
  position: relative;
  z-index: 1;
}

.cta-button-enhanced:hover .button-arrow {
  transform: translateX(5px);
}

.button-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  opacity: 0;
  transform: scale(0);
  transition: transform 0.6s ease-out, opacity 0.6s ease-out;
  pointer-events: none;
  z-index: 0;
}

.cta-button-enhanced:active .button-ripple {
  transform: scale(10);
  opacity: 0.6;
  transition: transform 0.6s ease-out, opacity 0.6s ease-out;
}

.scroll-hint {
  position: absolute;
  bottom: 50px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
}

.scroll-text {
  font-size: 0.9rem;
  margin-bottom: 1rem;
  font-weight: 300;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.scroll-line {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 auto;
  animation: scrollPulse 2s ease-in-out infinite;
}

@keyframes scrollPulse {
  0%, 100% {
    opacity: 0.3;
    transform: scaleY(1);
  }
  50% {
    opacity: 1;
    transform: scaleY(1.5);
  }
}

/* Story Section */
.story-section {
  padding: 8rem 0;
  background: #111111;
  position: relative;
  width: 100%;
  margin: 0;
  overflow: hidden;
  left: 0;
  right: 0;
}

.story-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
}

.story-text {
  color: white;
}

.story-title-massive {
  font-size: 4rem;
  font-weight: 700;
  margin-bottom: 2rem;
  margin-top: 0;
  line-height: 1.1;
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  overflow: hidden;
}

.story-description-enhanced {
  font-size: 1.2rem;
  line-height: 1.7;
  opacity: 0.9;
  font-weight: 300;
  position: relative;
  overflow: hidden;
  margin: 0;
}

.story-description-enhanced strong {
  color: #10b981;
  font-weight: 600;
}

.highlight-text {
  background: linear-gradient(45deg, #10b981, #34d399, #6ee7b7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 300% 300%;
  animation: gradient-shift 3s ease infinite;
  font-weight: 700;
  position: relative;
  display: inline-block;
}

.highlight-text::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, #10b981, #34d399, #6ee7b7);
  opacity: 0.2;
  filter: blur(20px);
  z-index: -1;
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes gradient-shift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.story-visual {
  display: flex;
  align-items: center;
  gap: 2rem;
  width: 100%;
  flex-wrap: wrap;
}

.visual-card {
  flex: 1;
  min-width: 200px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: auto;
  min-height: 200px;
}

.visual-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.visual-card.featured {
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.3);
}

.card-content {
  color: white;
}

.card-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.card-content h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  margin-top: 0;
}

.card-content p {
  font-size: 0.9rem;
  opacity: 0.7;
  margin: 0;
}

.transform-arrow {
  font-size: 2rem;
  color: #10b981;
  font-weight: bold;
}

/* Projects Section */
.projects-section {
  padding: 8rem 0;
  background: #0a0a0a;
  position: relative;
  width: 100%;
  margin: 0;
  overflow: hidden;
  left: 0;
  right: 0;
}

.section-header {
  text-align: center;
  margin-bottom: 4rem;
  padding: 0 2rem;
}

.section-title-massive {
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

.title-word-animate {
  display: inline-block;
  margin-right: 0.5rem;
  opacity: 0.9;
  transform-origin: center bottom;
  will-change: transform;
}

.title-word-animate.highlight-word {
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.title-word-animate.highlight-word::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(45deg, #10b981, #34d399);
  border-radius: 2px;
}

.projects-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  width: 100%;
  box-sizing: border-box;
}

.project-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  width: 100%;
  height: auto;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.project-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(16, 185, 129, 0.3);
}

.project-visual {
  position: relative;
  height: 200px;
  overflow: hidden;
  width: 100%;
  flex-shrink: 0;
}

.project-image {
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, #10b981, #34d399);
  display: flex;
  align-items: center;
  justify-content: center;
}

.project-placeholder {
  font-size: 3rem;
  color: white;
}

.project-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.project-item:hover .project-overlay {
  opacity: 1;
}

.project-btn {
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 25px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.project-btn:hover {
  background: rgba(16, 185, 129, 0.2);
  border-color: #10b981;
}

.project-content {
  padding: 2rem;
  color: white;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.project-title {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 1rem;
  margin-top: 0;
}

.project-description {
  font-size: 0.95rem;
  line-height: 1.6;
  opacity: 0.8;
  margin-bottom: 1.5rem;
  margin-top: 0;
}

.project-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #10b981;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.project-link:hover {
  color: #34d399;
}

.link-arrow {
  transition: transform 0.3s ease;
}

.project-link:hover .link-arrow {
  transform: translateX(3px);
}

/* Features Storytelling Section */
.features-storytelling {
  padding: 8rem 0;
  background: #111111;
  position: relative;
  width: 100%;
  margin: 0;
  overflow: hidden;
  left: 0;
  right: 0;
}

.features-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.features-header {
  text-align: center;
  margin-bottom: 4rem;
}

.features-title-massive {
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

.features-word {
  display: inline-block;
  margin-right: 0.5rem;
  opacity: 0.9;
  transform-origin: center bottom;
  will-change: transform;
}

.features-word.highlight-features {
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.features-word.highlight-features::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(45deg, #10b981, #34d399);
  border-radius: 2px;
}

.features-subtitle-enhanced {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.7);
  max-width: 600px;
  margin: 0 auto;
  font-weight: 300;
}

.features-showcase {
  display: grid;
  grid-template-columns: 1fr;
  gap: 4rem;
}

.feature-highlight {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 3rem;
  align-items: center;
  padding: 3rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  transition: all 0.3s ease;
}

.feature-highlight:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(16, 185, 129, 0.3);
}

.feature-highlight:nth-child(even) {
  grid-template-columns: 2fr 1fr;
}

.feature-highlight:nth-child(even) .feature-visual {
  order: 2;
}

.feature-highlight:nth-child(even) .feature-content {
  order: 1;
}

.feature-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-icon-large {
  width: 120px;
  height: 120px;
  background: linear-gradient(45deg, #10b981, #34d399);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: white;
  box-shadow: 0 8px 32px rgba(16, 185, 129, 0.3);
  position: relative;
  overflow: hidden;
  animation: pulse 3s ease-in-out infinite;
}

.feature-icon-large::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 3s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%) translateY(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) translateY(100%) rotate(45deg);
  }
}

.feature-content {
  color: white;
}

.feature-name-massive {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 1rem;
  margin-top: 0;
  color: #10b981;
}

.feature-desc-enhanced {
  font-size: 1.1rem;
  line-height: 1.6;
  opacity: 0.9;
  margin-bottom: 2rem;
  margin-top: 0;
  font-weight: 300;
}

.feature-benefits {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
  opacity: 0.8;
}

.benefit-check {
  width: 20px;
  height: 20px;
  background: #10b981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.8rem;
  font-weight: bold;
}

/* Stats Storytelling Section */
.stats-storytelling {
  padding: 8rem 0;
  background: #0a0a0a;
  position: relative;
  width: 100%;
  margin: 0;
  overflow: hidden;
  left: 0;
  right: 0;
}

.stats-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  text-align: center;
}

.stats-title-massive {
  font-size: 4rem;
  font-weight: 700;
  color: white;
  margin-bottom: 4rem;
  margin-top: 0;
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  overflow: hidden;
}

.stats-word {
  display: inline-block;
  margin-right: 0.5rem;
  opacity: 0.9;
  transform-origin: center bottom;
  will-change: transform;
}

.stats-word.highlight-stats {
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.stats-word.highlight-stats::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(45deg, #10b981, #34d399);
  border-radius: 2px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 3rem;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 3rem 2rem;
  text-align: center;
  transition: all 0.3s ease;
  transform-style: preserve-3d;
  perspective: 1000px;
  cursor: pointer;
  will-change: transform;
}

.stat-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(16, 185, 129, 0.3);
}

.stat-visual {
  margin-bottom: 1.5rem;
}

.stat-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(45deg, #10b981, #34d399);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(16, 185, 129, 0.3);
}

.stat-circle::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 3s infinite;
}

.stat-number-massive {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  position: relative;
  z-index: 1;
}

.stat-label-enhanced {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 300;
  margin: 0;
}

/* CTA Storytelling Section */
.cta-storytelling {
  padding: 8rem 0;
  background: #111111;
  text-align: center;
  position: relative;
  width: 100%;
  margin: 0;
  overflow: hidden;
  left: 0;
  right: 0;
}

.cta-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 2rem;
  color: white;
}

.cta-title-massive {
  font-size: 4rem;
  font-weight: 700;
  margin-bottom: 2rem;
  margin-top: 0;
  line-height: 1.2;
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  overflow: hidden;
}

.cta-word {
  display: inline-block;
  margin-right: 0.5rem;
  opacity: 0.9;
  transform-origin: center bottom;
  will-change: transform;
}

.cta-word.highlight-cta {
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.cta-word.highlight-cta::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(45deg, #10b981, #34d399);
  border-radius: 2px;
}

.cta-subtitle-enhanced {
  font-size: 1.3rem;
  margin-bottom: 3rem;
  margin-top: 0;
  opacity: 0.8;
  line-height: 1.6;
  font-weight: 300;
}

.cta-main-button-enhanced {
  display: inline-flex;
  align-items: center;
  gap: 1rem;
  padding: 1.2rem 3rem;
  background: linear-gradient(45deg, #10b981, #34d399);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 1.2rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(16, 185, 129, 0.3);
  will-change: transform;
}

.cta-main-button-enhanced:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(16, 185, 129, 0.4);
  background: linear-gradient(45deg, #059669, #10b981);
}

.main-button-text {
  position: relative;
  z-index: 1;
}

.button-glow-enhanced {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: glow 2s ease-in-out infinite;
  z-index: 0;
}

.button-waves {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100px; /* Adjust height for waves */
  background: linear-gradient(to top, rgba(16, 185, 129, 0.1), transparent);
  z-index: 0;
}

@keyframes glow {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

/* Footer */
.storytelling-footer {
  background: #0a0a0a;
  padding: 4rem 0 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  width: 100%;
  margin: 0;
  overflow: hidden;
  left: 0;
  right: 0;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.footer-logo img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.footer-logo span {
  font-size: 1.4rem;
  font-weight: 700;
  background: linear-gradient(45deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-links {
  display: flex;
  gap: 2rem;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #10b981;
}

.footer-bottom {
  text-align: center;
  padding-top: 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
}

/* Icons */
.icon-ai::before { content: '🤖'; }
.icon-template::before { content: '📄'; }
.icon-export::before { content: '💾'; }
.icon-cloud::before { content: '☁️'; }

/* Responsive Design */
@media (max-width: 768px) {
  body {
    overflow-x: hidden;
  }
  
  .landing-page {
    overflow-x: hidden;
    width: 100%;
  }
  
  .hero-title-massive {
    font-size: 3rem;
  }
  
  .hero-storytelling {
    padding: 0;
    width: 100%;
  }
  
  .hero-content {
    padding: 0 1rem;
    width: 100%;
    max-width: 100%;
  }
  
  .story-content {
    grid-template-columns: 1fr;
    gap: 2rem;
    padding: 0 1rem;
  }
  
  .story-visual {
    flex-direction: column;
    gap: 1rem;
    width: 100%;
  }
  
  .visual-card {
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
  }
  
  .transform-arrow {
    transform: rotate(90deg);
    margin: 1rem 0;
  }
  
  .projects-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    padding: 0 1rem;
  }
  
  .project-item {
    max-width: 400px;
    margin: 0 auto;
  }
  
  .feature-highlight {
    grid-template-columns: 1fr !important;
    gap: 2rem;
  }
  
  .feature-highlight .feature-visual {
    order: 1 !important;
  }
  
  .feature-highlight .feature-content {
    order: 2 !important;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .cta-title-massive {
    font-size: 3rem;
  }
  
  .nav-container {
    padding: 0 1rem;
  }
  
  .features-content,
  .stats-content,
  .cta-content,
  .footer-content,
  .section-header {
    padding: 0 1rem;
  }
  
  .feature-highlight {
    padding: 2rem 1rem;
    gap: 2rem;
  }
  
  .stats-grid {
    gap: 2rem;
  }
  
  .nav-menu {
    gap: 1rem;
  }
  
  .nav-link {
    display: none;
  }
  
  .nav-btn {
    padding: 0.4rem 1rem;
    font-size: 0.8rem;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 2rem;
  }
  
  .footer-links {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  body {
    overflow-x: hidden;
  }
  
  .landing-page {
    overflow-x: hidden;
    width: 100%;
  }
  
  .hero-title-massive {
    font-size: 2.5rem;
  }
  
  .hero-badge {
    font-size: 0.8rem;
  }
  
  .hero-description-enhanced {
    font-size: 1.1rem;
  }
  
  .story-title-massive {
    font-size: 2.5rem;
  }
  
  .features-title-massive {
    font-size: 2.5rem;
  }
  
  .stats-title-massive {
    font-size: 2.5rem;
  }
  
  .cta-title-massive {
    font-size: 2rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .story-content {
    padding: 0 0.5rem;
  }
  
  .projects-grid {
    padding: 0 0.5rem;
  }
  
  .visual-card {
    padding: 1.5rem;
  }
  
  .project-content {
    padding: 1.5rem;
  }
}
</style> 