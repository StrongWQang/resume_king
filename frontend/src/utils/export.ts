import { Component } from '../store/resume'
import { jsPDF } from 'jspdf'

// A4纸张尺寸（单位：毫米）
export const A4 = {
  width: 210,
  height: 296.99
}

// 编辑界面尺寸（像素）
const EDITOR_SIZE = {
  width: 595,  // A4宽度对应的像素值
  height: 842  // A4高度对应的像素值
}

// 将编辑界面坐标转换为PDF坐标
const convertCoordinates = (x: number, y: number, width: number, height: number, scale: number = 1) => {
  return {
    x: (x * scale),
    y: (y * scale),
    width: (width * scale),
    height: (height * scale)
  }
}

// 创建临时canvas元素
const createCanvas = (width: number, height: number) => {
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  return canvas
}

// 加载图片
const loadImage = (url: string): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    
    // 添加重试机制
    let retryCount = 0
    const maxRetries = 3
    const retryDelay = 1000 // 1秒后重试
    
    const tryLoadImage = () => {
      img.onload = () => {
        console.log('Image loaded successfully:', url)
        resolve(img)
      }
      
      img.onerror = (error) => {
        console.error('Image load error:', error)
        if (retryCount < maxRetries) {
          retryCount++
          console.log(`图片加载失败，正在进行第${retryCount}次重试...`)
          // 添加随机参数防止缓存
          const timestamp = Date.now()
          const random = Math.random()
          img.src = `${url}?t=${timestamp}&r=${random}`
        } else {
          console.error(`图片加载失败，已达到最大重试次数: ${url}`)
          // 使用默认图片
          img.src = '/src/template/default.png'
          resolve(img)
        }
      }
      
      // 添加随机参数防止缓存
      const timestamp = Date.now()
      const random = Math.random()
      img.src = `${url}?t=${timestamp}&r=${random}`
    }
    
    tryLoadImage()
  })
}

// 将组件渲染到canvas上
const renderComponentToCanvas = async (
  ctx: CanvasRenderingContext2D,
  component: Component,
  scale: number = 1
) => {
  const { type, x, y, width, height, content, fontSize, color, textAlign, lineHeight, imageUrl } = component
  
  // 转换坐标和尺寸
  const { x: pdfX, y: pdfY, width: pdfWidth, height: pdfHeight } = convertCoordinates(x, y, width, height, scale)
  
  // 转换字体大小
  const pdfFontSize = (fontSize || 14) * scale

  ctx.save()
  ctx.scale(scale, scale)

  if (type === 'text-title' || type === 'text-basic') {
    if (content) {
      ctx.font = `${pdfFontSize}px Microsoft YaHei`
      ctx.fillStyle = color || '#000000'
      ctx.textAlign = textAlign as CanvasTextAlign || 'left'
      ctx.textBaseline = 'top'
      
      const lines = content.split('\n')
      const lineHeightPx = pdfFontSize * (lineHeight || 1.5)
      
      lines.forEach((line, index) => {
        ctx.fillText(line, pdfX / scale, pdfY / scale + index * lineHeightPx)
      })
    }
  } else if (type === 'image' && imageUrl) {
    try {
      const img = await loadImage(imageUrl)
      // 保持图片原始比例
      const aspectRatio = img.width / img.height
      let finalWidth = pdfWidth
      let finalHeight = pdfHeight
      
      if (width / height > aspectRatio) {
        finalWidth = pdfHeight * aspectRatio
      } else {
        finalHeight = pdfWidth / aspectRatio
      }
      
      ctx.drawImage(img, pdfX / scale, pdfY / scale, finalWidth / scale, finalHeight / scale)
    } catch (error) {
      console.error('图片加载失败:', error)
      // 绘制一个占位符
      ctx.fillStyle = '#f0f0f0'
      ctx.fillRect(pdfX / scale, pdfY / scale, pdfWidth / scale, pdfHeight / scale)
      ctx.fillStyle = '#999999'
      ctx.font = '14px Microsoft YaHei'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText('图片加载失败', (pdfX + pdfWidth / 2) / scale, (pdfY + pdfHeight / 2) / scale)
    }
  } else if (type === 'divider-solid') {
    ctx.beginPath()
    ctx.strokeStyle = color || '#000000'
    ctx.lineWidth = 1
    ctx.moveTo(pdfX / scale, (pdfY + pdfHeight / 2) / scale)
    ctx.lineTo((pdfX + pdfWidth) / scale, (pdfY + pdfHeight / 2) / scale)
    ctx.stroke()
  }

  ctx.restore()
}



// 高清导出比例，建议至少 2（越高越清晰，但文件越大）
const EXPORT_SCALE = 2

export const exportPDF = async (components: Component[]) => {
  try {
    // 使用更高分辨率渲染 canvas
    const width = EDITOR_SIZE.width
    const height = EDITOR_SIZE.height
    const canvas = createCanvas(width * EXPORT_SCALE, height * EXPORT_SCALE)
    const ctx = canvas.getContext('2d')

    if (!ctx) {
      throw new Error('无法创建canvas上下文')
    }

    // 先整体放大 canvas context，使得绘制时按比例缩放
    ctx.scale(EXPORT_SCALE, EXPORT_SCALE)

    // 白色背景
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, width, height)

    // 渲染组件（注意此时传入 scale 为 1，因为 ctx 已经 scale 了）
    for (const component of components) {
      await renderComponentToCanvas(ctx, component, 1)
    }

    // 获取高清图片
    const imageData = canvas.toDataURL('image/png', 1.0)

    // 创建 PDF
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'px',
      format: [width, height] // 用实际逻辑尺寸（未缩放的）
    })

    // 添加图片（PDF 会自动缩放 image 到指定尺寸）
    pdf.addImage(imageData, 'PNG', 0, 0, width, height)

    // 输出为 Blob URL
    const pdfBlob = pdf.output('blob')
    const url = URL.createObjectURL(pdfBlob)
    return url
  } catch (error) {
    console.error('PDF导出失败:', error)
    throw error
  }
}
