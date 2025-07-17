import { defineStore } from 'pinia'

interface SettingsState {
  theme: 'light' | 'dark' | 'auto'
  language: 'zh-CN' | 'en-US'
}

export const useSettingsStore = defineStore('settings', {
  state: (): SettingsState => ({
    theme: localStorage.getItem('theme') as 'light' | 'dark' | 'auto' || 'auto',
    language: localStorage.getItem('language') as 'zh-CN' | 'en-US' || 'zh-CN',
  }),

  actions: {
    setTheme(theme: 'light' | 'dark' | 'auto') {
      this.theme = theme
      localStorage.setItem('theme', theme)
      this.applyTheme()
    },

    setLanguage(language: 'zh-CN' | 'en-US') {
      this.language = language
      localStorage.setItem('language', language)
      // 这里可以添加i18n的语言切换逻辑
    },

    applyTheme() {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      const theme = this.theme === 'auto' ? (prefersDark ? 'dark' : 'light') : this.theme
      
      if (theme === 'dark') {
        document.documentElement.classList.add('dark')
        document.documentElement.setAttribute('data-theme', 'dark')
      } else {
        document.documentElement.classList.remove('dark')
        document.documentElement.setAttribute('data-theme', 'light')
      }
    },

    initializeSettings() {
      this.applyTheme()
      
      // 监听系统主题变化
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
        if (this.theme === 'auto') {
          this.applyTheme()
        }
      })
    }
  }
}) 