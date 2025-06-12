import { defineStore } from 'pinia'

interface Component {
  id: string
  type: string
  x: number
  y: number
  width: number
  height: number
  content?: string
  fontFamily?: string
  fontSize?: number
  color?: string
  imageUrl?: string
  aspectRatio?: number
  isResizing?: boolean
  originalWidth?: number
  originalHeight?: number
}

interface ResumeState {
  components: Component[]
  selectedComponentId: string | null
  currentResumeId: string | null
}

export const useResumeStore = defineStore('resume', {
  state: (): ResumeState => ({
    components: [],
    selectedComponentId: null,
    currentResumeId: localStorage.getItem('lastResumeId')
  }),

  getters: {
    selectedComponent: (state) => {
      return state.components.find(c => c.id === state.selectedComponentId) || null
    }
  },

  actions: {
    addComponent(component: Omit<Component, 'id'>) {
      const id = Date.now().toString()
      this.components.push({
        ...component,
        id,
        fontFamily: 'Microsoft YaHei',
        fontSize: 16,
        color: '#000000',
        isResizing: false
      })
      this.selectedComponentId = id
      this.autoSave()
    },

    updateSelectedComponent() {
      const index = this.components.findIndex(c => c.id === this.selectedComponentId)
      if (index !== -1) {
        this.components[index] = { ...this.components[index] }
      }
      this.autoSave()
    },

    updateSelectedComponentPosition(dx: number, dy: number) {
      const component = this.selectedComponent
      if (component) {
        component.x += dx
        component.y += dy
        this.updateSelectedComponent()
      }
    },

    selectComponent(id: string | null) {
      this.selectedComponentId = id
    },

    deleteSelectedComponent() {
      if (this.selectedComponentId) {
        this.components = this.components.filter(c => c.id !== this.selectedComponentId)
        this.selectedComponentId = null
        this.autoSave()
      }
    },

    // 自动保存到本地存储
    autoSave() {
      try {
        // 将当前组件数据保存到 localStorage
        localStorage.setItem('resumeData', JSON.stringify(this.components))
        console.log('自动保存到本地存储成功')
      } catch (error) {
        console.error('自动保存到本地存储失败:', error)
      }
    },

    // 持久化保存到服务器
    async saveResume() {
      try {
        console.log('开始保存简历数据到服务器:', this.components)
        const response = await fetch('/api/resumes', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.components)
        })
        
        if (!response.ok) {
          const errorText = await response.text()
          console.error('保存失败，服务器响应:', errorText)
          throw new Error(`保存失败: ${errorText}`)
        }
        
        const data = await response.text()
        console.log('保存成功，返回数据:', data)
        this.currentResumeId = data
        localStorage.setItem('lastResumeId', data)
        return data
      } catch (error) {
        console.error('保存简历失败，详细错误:', error)
        throw error
      }
    },

    // 从本地存储加载数据
    loadFromLocalStorage() {
      try {
        const savedData = localStorage.getItem('resumeData')
        if (savedData) {
          this.components = JSON.parse(savedData)
          console.log('从本地存储加载数据成功')
        }
      } catch (error) {
        console.error('从本地存储加载数据失败:', error)
      }
    },

    // 加载简历数据
    async loadResume(id: string) {
      try {
        const response = await fetch(`/api/resumes/${id}`)
        if (!response.ok) {
          throw new Error('加载失败')
        }
        const data = await response.json()
        this.setComponents(data)
        this.currentResumeId = id
        localStorage.setItem('lastResumeId', id)
      } catch (error) {
        console.error('加载简历失败:', error)
        throw error
      }
    },

    // 设置组件数据
    setComponents(components: Component[]) {
      this.components = components.map(component => ({
        ...component,
        fontFamily: component.fontFamily || 'Microsoft YaHei',
        fontSize: component.fontSize || 16,
        color: component.color || '#000000'
      }))
      this.selectedComponentId = null
    },

    updateComponentSize(id: string, width: number, height: number) {
      const component = this.components.find(c => c.id === id)
      if (component) {
        component.width = width
        component.height = height
        this.updateSelectedComponent()
      }
    },

    setComponentImage(id: string, imageUrl: string, width: number, height: number) {
      const component = this.components.find(c => c.id === id)
      if (component) {
        component.imageUrl = imageUrl
        component.width = width
        component.height = height
        component.aspectRatio = width / height
        component.originalWidth = width
        component.originalHeight = height
        this.updateSelectedComponent()
      }
    },

    startResizing(id: string) {
      const component = this.components.find(c => c.id === id)
      if (component) {
        component.isResizing = true
        component.originalWidth = component.width
        component.originalHeight = component.height
      }
    },

    stopResizing(id: string) {
      const component = this.components.find(c => c.id === id)
      if (component) {
        component.isResizing = false
      }
    }
  }
})

// 创建简历模板
export const createResumeTemplate = () => {
  return [
    // 姓名和职位
    {
      id: `name-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 40,
      width: 200,
      height: 40,
      content: '张三',
      fontSize: 24,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.2
    },
    {
      id: `title-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 85,
      width: 200,
      height: 30,
      content: '软件工程师',
      fontSize: 16,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#666666',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 分隔线
    {
      id: `divider-${Date.now()}`,
      type: 'divider-solid',
      x: 40,
      y: 125,
      width: 515,
      height: 20,
      color: '#e0e0e0',
      padding: 10
    },
    
    // 联系方式
    {
      id: `contact-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 155,
      width: 515,
      height: 30,
      content: '电话：138-xxxx-xxxx | 邮箱：example@email.com | 地址：北京市朝阳区',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#666666',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 教育背景
    {
      id: `edu-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 195,
      width: 200,
      height: 30,
      content: '教育背景',
      fontSize: 18,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `edu-content-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 235,
      width: 515,
      height: 30,
      content: '北京大学 | 计算机科学与技术 | 本科 | 2018-2022',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 工作经历
    {
      id: `work-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 275,
      width: 200,
      height: 30,
      content: '工作经历',
      fontSize: 18,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `work-company-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 315,
      width: 515,
      height: 30,
      content: 'ABC科技有限公司 | 软件工程师 | 2022.07-至今',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `work-desc-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 355,
      width: 515,
      height: 60,
      content: '• 负责公司核心产品的开发和维护\n• 优化系统性能，提升用户体验\n• 参与技术方案设计和评审',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#666666',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 技能特长
    {
      id: `skills-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 425,
      width: 200,
      height: 30,
      content: '技能特长',
      fontSize: 18,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `skills-content-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 465,
      width: 515,
      height: 30,
      content: '• 精通Java、Python等编程语言\n• 熟悉Spring Boot、Vue.js等主流框架\n• 具备良好的算法和数据结构基础',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#666666',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 项目经验
    {
      id: `project-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 505,
      width: 200,
      height: 30,
      content: '项目经验',
      fontSize: 18,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `project-name-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 545,
      width: 515,
      height: 30,
      content: '企业级微服务平台 | 技术负责人 | 2023.01-2023.06',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `project-desc-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 585,
      width: 515,
      height: 60,
      content: '• 设计并实现微服务架构，提升系统可扩展性\n• 优化数据库性能，提升查询效率\n• 带领团队完成项目交付，获得客户好评',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#666666',
      textAlign: 'left',
      lineHeight: 1.5
    }
  ]
} 