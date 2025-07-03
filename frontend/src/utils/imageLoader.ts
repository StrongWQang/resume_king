import axios from 'axios'

class ImageLoader {
  private static instance: ImageLoader
  private proxyBaseUrl: string = '/api/proxy/image' // 图片代理的基础URL

  private constructor() {}

  public static getInstance(): ImageLoader {
    if (!ImageLoader.instance) {
      ImageLoader.instance = new ImageLoader()
    }
    return ImageLoader.instance
  }

  /**
   * 获取代理后的图片URL
   * @param originalUrl 原始图片URL
   * @returns 代理后的URL
   */
  public getProxyImageUrl(originalUrl: string): string {
    if (!originalUrl) return ''
    
    // 如果已经是代理URL或本地URL，直接返回
    if (originalUrl.startsWith('/') || originalUrl.startsWith(this.proxyBaseUrl)) {
      return originalUrl
    }

    // 对OSS URL进行代理
    if (originalUrl.includes('aliyuncs.com')) {
      return `${this.proxyBaseUrl}?url=${encodeURIComponent(originalUrl)}`
    }

    return originalUrl
  }

  /**
   * 上传图片到服务器
   * @param file 图片文件
   * @returns 上传后的URL
   */
  public async uploadImage(file: File): Promise<string> {
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

  /**
   * 检查图片是否可访问
   * @param url 图片URL
   * @returns 是否可访问
   */
  public async checkImageAccess(url: string): Promise<boolean> {
    try {
      const response = await fetch(this.getProxyImageUrl(url), { method: 'HEAD' })
      return response.ok
    } catch (error) {
      console.error('图片访问检查失败:', error)
      return false
    }
  }

  /**
   * 预加载图片
   * @param url 图片URL
   * @returns Promise<HTMLImageElement>
   */
  public preloadImage(url: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.src = this.getProxyImageUrl(url)
      img.onload = () => resolve(img)
      img.onerror = reject
    })
  }
}

export const imageLoader = ImageLoader.getInstance() 