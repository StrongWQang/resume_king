import axios from 'axios'

export const imageLoader = {
  // 清理和标准化URL
  cleanUrl(url: string): string {
    if (!url) return ''
    
    // 如果是本地路径，直接返回
    if (url.startsWith('/') && !url.includes('/api/proxy/image')) {
      return url
    }
    
    // 递归解析嵌套的代理URL
    let cleanedUrl = url
    
    // 处理嵌套的代理URL：/api/proxy/image?url=/api/proxy/image?url=...
    while (cleanedUrl.includes('/api/proxy/image?url=')) {
      const match = cleanedUrl.match(/\/api\/proxy\/image\?url=(.+)/)
      if (match) {
        try {
          cleanedUrl = decodeURIComponent(match[1])
        } catch (e) {
          console.warn('URL解码失败:', cleanedUrl)
          break
        }
      } else {
        break
      }
    }
    
    // 确保最终得到的是原始URL
    if (cleanedUrl.startsWith('/api/proxy/image?url=')) {
      const match = cleanedUrl.match(/\/api\/proxy\/image\?url=(.+)/)
      if (match) {
        try {
          cleanedUrl = decodeURIComponent(match[1])
        } catch (e) {
          console.warn('最终URL解码失败:', cleanedUrl)
        }
      }
    }
    
    return cleanedUrl
  },

  // 调试工具：分析URL处理过程
  debugUrl(url: string): void {
    console.log('=== URL处理调试 ===')
    console.log('原始URL:', url)
    console.log('清理后URL:', this.cleanUrl(url))
    console.log('最终代理URL:', this.getProxyImageUrl(url))
    console.log('=== 调试结束 ===')
  },

  getProxyImageUrl(url: string): string {
    if (!url) return ''
    
    // 首先清理URL
    const cleanedUrl = this.cleanUrl(url)
    
    // 本地图片直接返回
    if (cleanedUrl.startsWith('/') && !cleanedUrl.includes('/api/proxy/image')) {
      return cleanedUrl
    }
    
    // 只有 http/https 开头的才走代理
    if (/^https?:\/\//.test(cleanedUrl)) {
      return `/api/proxy/image?url=${encodeURIComponent(cleanedUrl)}`
    }
    
    // 其他情况直接返回清理后的URL
    return cleanedUrl
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
      const proxyUrl = this.getProxyImageUrl(url)
      const response = await fetch(proxyUrl, { 
        method: 'HEAD',
        mode: 'cors',
        credentials: 'omit'
      })
      return response.ok
    } catch {
      return false
    }
  },

  // 参考简历广场的图片错误处理方式
  handleImageError(img: HTMLImageElement, fallbackUrl: string = '/logo-large.jpg'): void {
    const imgUrl = img.src
    
    // 避免无限循环
    if (img.src === fallbackUrl || img.src.includes(fallbackUrl)) {
      return
    }
    
    console.error('图片加载失败:', {
      src: img.src,
      alt: img.alt
    })
    
    // 使用默认图片
    img.src = fallbackUrl
    
    // 添加错误样式
    img.style.border = '1px solid #ff4d4f'
    img.style.backgroundColor = '#fff2f0'
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