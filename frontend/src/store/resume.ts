import { defineStore } from 'pinia'

export interface Component {
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
  textAlign?: string
  lineHeight?: number
  padding?: number
  thickness?: number
  fontWeight?: number
}

interface ResumeState {
  components: Component[]
  selectedComponentId: string | null
  currentResumeId: string | null
  history: Component[][] // 历史记录
  historyIndex: number // 当前历史记录索引
}

export const useResumeStore = defineStore('resume', {
  state: (): ResumeState => ({
    components: [],
    selectedComponentId: null,
    currentResumeId: localStorage.getItem('lastResumeId') || null,
    history: [[]], // 初始状态
    historyIndex: 0
  }),

  getters: {
    selectedComponent: (state) => {
      return state.components.find(c => c.id === state.selectedComponentId) || null
    }
  },

  actions: {
    // 添加历史记录
    addHistory() {
      // 如果当前不在历史记录的最后，删除当前位置之后的所有记录
      this.history = this.history.slice(0, this.historyIndex + 1)
      // 添加新的历史记录
      this.history.push(JSON.parse(JSON.stringify(this.components)))
      // 更新历史记录索引
      this.historyIndex = this.history.length - 1
      // 限制历史记录数量，最多保存50条
      if (this.history.length > 50) {
        this.history.shift()
        this.historyIndex--
      }
    },

    // 撤销
    undo() {
      if (this.historyIndex > 0) {
        this.historyIndex--
        this.components = JSON.parse(JSON.stringify(this.history[this.historyIndex]))
        this.selectedComponentId = null
      }
    },

    // 重做
    redo() {
      if (this.historyIndex < this.history.length - 1) {
        this.historyIndex++
        this.components = JSON.parse(JSON.stringify(this.history[this.historyIndex]))
        this.selectedComponentId = null
      }
    },

    // 修改现有的 actions，在修改组件状态时添加历史记录
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
      this.addHistory()
      this.autoSave()
    },

    updateSelectedComponent() {
      const index = this.components.findIndex(c => c.id === this.selectedComponentId)
      if (index !== -1) {
        this.components[index] = { ...this.components[index] }
        this.addHistory()
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
        this.addHistory()
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
        // 深拷贝组件数据，避免Proxy对象序列化问题
        const componentsToSave = JSON.parse(JSON.stringify(this.components))
        console.log('开始保存简历数据到服务器:', componentsToSave)
        
        const response = await fetch('/api/resumes', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(componentsToSave)
        })
        
        if (!response.ok) {
          const errorText = await response.text()
          console.error('保存失败，服务器响应:', errorText)
          throw new Error(`保存失败: ${errorText}`)
        }
        
        // 直接获取响应文本，避免JSON.parse时的数字精度问题
        const idString = await response.text()
        console.log('保存成功，返回数据:', idString)
        
        // 直接使用字符串形式的ID
        this.currentResumeId = idString
        localStorage.setItem('lastResumeId', idString)
        
        return idString
      } catch (error) {
        console.error('保存简历失败:', error)
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
        // 确保使用字符串形式的ID
        const response = await fetch(`/api/resumes/${id}`)
        if (!response.ok) {
          throw new Error('加载失败')
        }
        const data = await response.json()
        this.setComponents(data)
        this.currentResumeId = id // 保存为字符串
        localStorage.setItem('lastResumeId', id)
      } catch (error) {
        console.error('加载简历失败:', error)
        throw error
      }
    },

    setComponents(components: Component[]) {
      this.components = components.map(component => ({
        ...component,
        fontFamily: component.fontFamily || 'Microsoft YaHei',
        fontSize: component.fontSize || 16,
        color: component.color || '#000000'
      }))
      this.selectedComponentId = null
      this.addHistory()
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

// 简约现代风格简历模板
export const createModernResumeTemplate = () => {
  return [
    // 姓名和职位
    {
      id: `name-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 40,
      width: 300,
      height: 50,
      content: '李四',
      fontSize: 32,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 700,
      color: '#2c3e50',
      textAlign: 'left',
      lineHeight: 1.2
    },
    {
      id: `title-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 95,
      width: 300,
      height: 30,
      content: '全栈开发工程师',
      fontSize: 18,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 500,
      color: '#34495e',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 联系方式
    {
      id: `contact-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 135,
      width: 515,
      height: 30,
      content: '📱 139-xxxx-xxxx | 📧 lisi@email.com | 📍 上海市浦东新区',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#7f8c8d',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 分隔线
    {
      id: `divider-${Date.now()}`,
      type: 'divider-solid',
      x: 40,
      y: 175,
      width: 515,
      height: 2,
      color: '#3498db',
      padding: 0
    },
    
    // 技能特长
    {
      id: `skills-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 195,
      width: 200,
      height: 30,
      content: '专业技能',
      fontSize: 20,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#2c3e50',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `skills-content-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 235,
      width: 515,
      height: 80,
      content: '• 前端：Vue.js、React、TypeScript、Webpack\n• 后端：Node.js、Python、Java、Spring Boot\n• 数据库：MySQL、MongoDB、Redis\n• 其他：Docker、Git、CI/CD、微服务架构',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#34495e',
      textAlign: 'left',
      lineHeight: 1.6
    },
    
    // 工作经历
    {
      id: `work-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 325,
      width: 200,
      height: 30,
      content: '工作经历',
      fontSize: 20,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#2c3e50',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `work-company-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 365,
      width: 515,
      height: 30,
      content: '科技互联网公司 | 高级全栈工程师 | 2021.03-至今',
      fontSize: 16,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#34495e',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `work-desc-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 405,
      width: 515,
      height: 80,
      content: '• 主导公司核心业务系统的架构设计和开发\n• 优化系统性能，将页面加载时间减少50%\n• 设计并实现微服务架构，提升系统可扩展性\n• 带领5人团队完成多个重要项目交付',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#34495e',
      textAlign: 'left',
      lineHeight: 1.6
    }
  ]
}

// 创意设计风格简历模板
export const createCreativeResumeTemplate = () => {
  return [
    // 姓名和职位
    {
      id: `name-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 40,
      width: 300,
      height: 50,
      content: '王五',
      fontSize: 36,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 800,
      color: '#e74c3c',
      textAlign: 'left',
      lineHeight: 1.2
    },
    {
      id: `title-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 95,
      width: 300,
      height: 30,
      content: 'UI/UX 设计师',
      fontSize: 20,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 500,
      color: '#2c3e50',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 个人简介
    {
      id: `intro-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 135,
      width: 515,
      height: 60,
      content: '富有创造力的UI/UX设计师，5年设计经验，专注于打造优秀的用户体验。擅长将复杂问题简单化，通过设计解决用户痛点。',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#7f8c8d',
      textAlign: 'left',
      lineHeight: 1.6
    },
    
    // 联系方式
    {
      id: `contact-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 205,
      width: 515,
      height: 30,
      content: '📱 137-xxxx-xxxx | 📧 wangwu@email.com | 📍 广州市天河区',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#7f8c8d',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 技能特长
    {
      id: `skills-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 245,
      width: 200,
      height: 30,
      content: '设计技能',
      fontSize: 20,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#e74c3c',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `skills-content-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 285,
      width: 515,
      height: 80,
      content: '• 设计工具：Figma、Sketch、Adobe XD、Photoshop\n• 交互设计：用户研究、原型设计、可用性测试\n• 视觉设计：品牌设计、UI设计、动效设计\n• 其他：HTML/CSS、设计系统、响应式设计',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#2c3e50',
      textAlign: 'left',
      lineHeight: 1.6
    },
    
    // 项目经验
    {
      id: `project-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 375,
      width: 200,
      height: 30,
      content: '项目经验',
      fontSize: 20,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#e74c3c',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `project-name-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 415,
      width: 515,
      height: 30,
      content: '企业级SaaS平台重设计 | 主设计师 | 2023.01-2023.06',
      fontSize: 16,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#2c3e50',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `project-desc-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 455,
      width: 515,
      height: 80,
      content: '• 主导产品整体视觉风格和交互体验的重设计\n• 建立统一的设计系统，提升团队协作效率\n• 通过用户研究优化产品流程，提升转化率30%\n• 设计并实现产品动效系统，提升用户体验',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#2c3e50',
      textAlign: 'left',
      lineHeight: 1.6
    }
  ]
}

// 专业商务风格简历模板
export const createProfessionalResumeTemplate = () => {
  return [
    // 姓名和职位
    {
      id: `name-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 40,
      width: 300,
      height: 50,
      content: '赵六',
      fontSize: 28,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 700,
      color: '#1a1a1a',
      textAlign: 'left',
      lineHeight: 1.2
    },
    {
      id: `title-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 95,
      width: 300,
      height: 30,
      content: '产品经理',
      fontSize: 18,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 500,
      color: '#333333',
      textAlign: 'left',
      lineHeight: 1.5
    },
    
    // 联系方式
    {
      id: `contact-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 135,
      width: 515,
      height: 30,
      content: '电话：136-xxxx-xxxx | 邮箱：zhaoliu@email.com | 地址：深圳市南山区',
      fontSize: 14,
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
      y: 175,
      width: 515,
      height: 1,
      color: '#cccccc',
      padding: 0
    },
    
    // 工作经历
    {
      id: `work-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 195,
      width: 200,
      height: 30,
      content: '工作经历',
      fontSize: 18,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#1a1a1a',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `work-company-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 235,
      width: 515,
      height: 30,
      content: '互联网科技公司 | 高级产品经理 | 2020.07-至今',
      fontSize: 16,
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
      y: 275,
      width: 515,
      height: 80,
      content: '• 负责公司核心产品的规划和迭代，带领团队完成多个重要版本\n• 通过数据分析优化产品策略，提升用户留存率40%\n• 协调设计、开发、测试等跨部门资源，确保项目按时交付\n• 建立产品运营体系，推动产品商业化进程',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#666666',
      textAlign: 'left',
      lineHeight: 1.6
    },
    
    // 项目经验
    {
      id: `project-title-${Date.now()}`,
      type: 'text-title',
      x: 40,
      y: 365,
      width: 200,
      height: 30,
      content: '项目经验',
      fontSize: 18,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 600,
      color: '#1a1a1a',
      textAlign: 'left',
      lineHeight: 1.5
    },
    {
      id: `project-name-${Date.now()}`,
      type: 'text-basic',
      x: 40,
      y: 405,
      width: 515,
      height: 30,
      content: '企业数字化转型项目 | 项目负责人 | 2022.03-2022.12',
      fontSize: 16,
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
      y: 445,
      width: 515,
      height: 80,
      content: '• 主导企业数字化转型项目，实现业务流程数字化\n• 设计并实施数据中台，提升数据利用效率\n• 推动组织变革，建立敏捷开发体系\n• 项目成功交付，获得客户高度认可',
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      fontWeight: 400,
      color: '#666666',
      textAlign: 'left',
      lineHeight: 1.6
    }
  ]
} 