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
}

interface ResumeState {
  components: Component[]
  selectedComponentId: string | null
}

export const useResumeStore = defineStore('resume', {
  state: (): ResumeState => ({
    components: [],
    selectedComponentId: null
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
        color: '#000000'
      })
      this.selectedComponentId = id
    },

    updateSelectedComponent() {
      // 触发画布重新渲染
      this.components = [...this.components]
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
        const data = await response.json()
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
        const data = await response.json()
        this.components = data
        this.selectedComponentId = null
      } catch (error) {
        console.error('加载简历失败:', error)
        throw error
      }
    }
  }
}) 