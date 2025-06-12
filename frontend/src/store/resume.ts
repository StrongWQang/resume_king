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

    // 自动保存
    async autoSave() {
      if (!this.currentResumeId) {
        // 如果没有当前简历ID，先创建一个新简历
        try {
          const id = await this.saveResume()
          this.currentResumeId = id
          localStorage.setItem('lastResumeId', id)
        } catch (error) {
          console.error('自动保存失败:', error)
        }
      } else {
        try {
          await this.saveResume()
        } catch (error) {
          console.error('自动保存失败:', error)
        }
      }
    },

    // 保存简历数据
    async saveResume() {
      try {
        const response = await fetch('/api/resumes', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.components)
        })
        if (!response.ok) {
          throw new Error('保存失败')
        }
        const data = await response.json()
        this.currentResumeId = data.id
        localStorage.setItem('lastResumeId', data.id)
        return data.id
      } catch (error) {
        console.error('保存简历失败:', error)
        throw error
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