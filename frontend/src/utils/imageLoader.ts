import axios from 'axios'

export const imageLoader = {
  getProxyImageUrl(url: string): string {
    if (!url) return ''
    // 已经是代理URL，直接返回
    if (url.startsWith('/api/proxy/image?url=')) return url
    // 本地图片（/uploads/xxx.png、/default-logo.png等）直接返回
    if (url.startsWith('/') && !url.startsWith('/api/proxy/image')) return url
    // 只有 http/https 开头的才走代理
    if (/^https?:\/\//.test(url)) {
      return `/api/proxy/image?url=${encodeURIComponent(url)}`
    }
    // 其他情况直接返回
    return url
  },

  async preloadImage(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.crossOrigin = 'anonymous'
      img.onload = () => resolve()
      img.onerror = () => reject(new Error('Image load failed'))
      img.src = this.getProxyImageUrl(url)
    })
  },

  async checkImageAccess(url: string): Promise<boolean> {
    try {
      const response = await fetch(this.getProxyImageUrl(url), { 
        method: 'HEAD',
        mode: 'cors',
        credentials: 'omit'
      })
      return response.ok
    } catch {
      return false
    }
  },

  async uploadImage(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)

    try {
      const response = await axios.post('/api/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      return response.data.url
    } catch (error) {
      console.error('图片上传失败:', error)
      throw error
    }
  }
} 