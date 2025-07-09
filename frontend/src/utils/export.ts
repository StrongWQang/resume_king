import { Component } from '../store/resume'

// 从全局对象获取 PDFKit（在 index.html 中通过 CDN 加载）
// const { PDFDocument } = window.PDF as any // 移除原有静态获取

// 声明全局window类型
declare global {
  interface Window {
    PDF: {
      PDFDocument: any;
      blobStream: any;
      BlobStream?: any;
    };
  }
}

// A4纸张尺寸（单位：毫米）
export const A4 = {
  width: 210,
  height: 296.99
}

// 默认编辑区尺寸，实际导出时可传入
export const DEFAULT_EDITOR_SIZE = {
  width: 595,
  height: 842
}

// 创建canvas
const createCanvas = (width: number, height: number): HTMLCanvasElement => {
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  return canvas
}

// 加载图片
const loadImage = (url: string): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    // 设置跨域属性，解决canvas污染问题
    img.crossOrigin = 'anonymous'
    let retryCount = 0
    const maxRetries = 3
    const retryDelay = 1000
    let hasTriedDefault = false

    const tryLoadImage = (src: string) => {
      img.onload = () => {
        console.log('Image loaded successfully:', src)
        resolve(img)
      }

      img.onerror = () => {
        console.error('Image load error:', src)
        if (retryCount < maxRetries && !hasTriedDefault) {
          retryCount++
          console.log(`图片加载失败，正在进行第${retryCount}次重试...`)
          setTimeout(() => tryLoadImage(src), retryDelay)
        } else if (!hasTriedDefault && src !== '/src/template/default.png') {
          // 尝试加载默认图片，只尝试一次
          hasTriedDefault = true
          tryLoadImage('/src/template/default.png')
        } else {
          // 最终失败，返回 img 但不再触发 onerror，避免死循环
          img.onerror = null
          resolve(img)
        }
      }

      img.src = src
    }

    // 确保使用代理URL加载外部图片
    if (url && url.match(/^https?:\/\//)) {
      tryLoadImage(`/api/proxy/image?url=${encodeURIComponent(url)}`)
    } else {
      tryLoadImage(url)
    }
  })
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

// 将组件渲染到canvas上
const renderComponentToCanvas = async (
  ctx: CanvasRenderingContext2D,
  component: Component,
  scale: number = 1
) => {
  const { type, x, y, width, height, content, fontSize, color, textAlign, lineHeight, imageUrl, fontWeight, fontFamily, thickness, padding } = component

  // 文字紧贴边框
  const textPadding = 0

  // 转换坐标和尺寸（全部除以scale）
  const pdfX = x * scale
  const pdfY = y * scale
  const pdfWidth = width * scale
  const pdfHeight = height * scale

  ctx.save()
  ctx.scale(1, 1)

  if (type === 'text-title' || type === 'text-basic') {
    if (content) {
      const verticalPadding = 5 * scale;
      ctx.textAlign = textAlign as CanvasTextAlign || 'left';
      ctx.textBaseline = 'top';
      ctx.fillStyle = color || '#000000';
      const normalizedContent = content.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
      const parser = new DOMParser();
      const docFragment = parser.parseFromString(`<div>${normalizedContent}</div>`, 'text/html').body.firstChild;
      const lineHeightPx = (fontSize || 14) * (lineHeight || 1.5) * scale;
      let currentY = pdfY + verticalPadding;
      // 递归解析节点，生成片段数组
      type TextFragment = { text: string; style: { fontWeight?: number; fontSize?: number; fontFamily?: string; color?: string } };
      function collectFragments(node: ChildNode, style: { fontWeight?: number; fontSize?: number; fontFamily?: string; color?: string }): TextFragment[] {
        if (node.nodeType === 3) {
          return [{ text: node.textContent || '', style }];
        } else if (node.nodeType === 1) {
          let newStyle = { ...style };
          const elem = node as HTMLElement;
          if (elem.tagName === 'B' || elem.tagName === 'STRONG') {
            newStyle.fontWeight = 700;
          }
          let fragments: TextFragment[] = [];
          node.childNodes.forEach((child: ChildNode) => {
            fragments = fragments.concat(collectFragments(child, newStyle));
          });
          return fragments;
        }
        return [];
      }
      // 按行拆分片段
      function splitFragmentsByLine(fragments: TextFragment[]): TextFragment[][] {
        const lines: TextFragment[][] = [[]];
        fragments.forEach(frag => {
          const parts = frag.text.split('\n');
          parts.forEach((part, idx) => {
            if (idx > 0) lines.push([]);
            if (part) lines[lines.length - 1].push({ text: part, style: frag.style });
          });
        });
        // 移除最后一行为空的情况
        return lines.filter(line => line.length > 0);
      }
      // 整体 wrapText
      function wrapFragmentsLine(lineFragments: TextFragment[], maxWidth: number): TextFragment[][] {
        const wrapped: TextFragment[][] = [];
        let currentLine: TextFragment[] = [];
        let currentLineWidth = 0;
        let buffer = '';
        let bufferStyle: TextFragment['style'] | null = null;
        let measureFont = (style: TextFragment['style']) => {
          ctx.font = `${style.fontWeight || 400} ${(style.fontSize || 14) * scale}px ${style.fontFamily || 'Microsoft YaHei'}`;
        };
        for (let i = 0; i < lineFragments.length; i++) {
          const frag = lineFragments[i];
          let text = frag.text;
          let idx = 0;
          while (idx < text.length) {
            let char = text[idx];
            let nextBuffer = (buffer || '') + char;
            measureFont(frag.style);
            const width = ctx.measureText(nextBuffer).width;
            if (currentLineWidth + width > maxWidth && buffer) {
              // 换行
              currentLine.push({ text: buffer, style: bufferStyle! });
              wrapped.push(currentLine);
              currentLine = [];
              currentLineWidth = 0;
              buffer = '';
              bufferStyle = null;
              continue;
            }
            buffer = nextBuffer;
            bufferStyle = frag.style;
            idx++;
          }
          // 如果到片段结尾
          if (buffer) {
            measureFont(bufferStyle!);
            const width = ctx.measureText(buffer).width;
            if (currentLineWidth + width > maxWidth && currentLine.length > 0) {
              wrapped.push(currentLine);
              currentLine = [];
              currentLineWidth = 0;
            }
            currentLine.push({ text: buffer, style: bufferStyle! });
            currentLineWidth += width;
            buffer = '';
            bufferStyle = null;
          }
        }
        if (currentLine.length > 0) wrapped.push(currentLine);
        return wrapped;
      }
      // 生成所有片段
      let fragments: TextFragment[] = [];
      if (docFragment) {
        docFragment.childNodes.forEach((child: ChildNode) => {
          fragments = fragments.concat(collectFragments(child, {
            fontWeight: fontWeight,
            fontSize: fontSize,
            fontFamily: fontFamily,
            color: color
          }));
        });
      }
      // 按行拆分
      const lines = splitFragmentsByLine(fragments);
      const maxWidth = pdfWidth;
      // 每行整体 wrapText
      for (const lineFragments of lines) {
        const wrappedLines = wrapFragmentsLine(lineFragments, maxWidth);
        for (const wrappedLine of wrappedLines) {
          let drawX = pdfX;
          for (const frag of wrappedLine) {
            ctx.font = `${frag.style.fontWeight || 400} ${(frag.style.fontSize || 14) * scale}px ${frag.style.fontFamily || 'Microsoft YaHei'}`;
            ctx.fillStyle = frag.style.color || '#000000';
            ctx.fillText(frag.text, drawX, currentY);
            drawX += ctx.measureText(frag.text).width;
          }
          currentY += lineHeightPx;
        }
      }
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
      ctx.drawImage(img, pdfX, pdfY, finalWidth, finalHeight)
    } catch (error) {
      ctx.fillStyle = '#f0f0f0'
      ctx.fillRect(pdfX, pdfY, pdfWidth, pdfHeight)
      ctx.fillStyle = '#999999'
      ctx.font = `14px Microsoft YaHei`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText('图片加载失败', pdfX + pdfWidth / 2, pdfY + pdfHeight / 2)
    }
  } else if (type === 'divider-solid') {
    ctx.beginPath()
    ctx.strokeStyle = color || '#000000'
    ctx.lineWidth = (thickness || 1) * scale
    ctx.moveTo(pdfX, pdfY + pdfHeight / 2)
    ctx.lineTo(pdfX + pdfWidth, pdfY + pdfHeight / 2)
    ctx.stroke()
  }
  ctx.restore()
}

// 支持自定义DPI和尺寸，保证布局100%还原
export const exportPDF = async (
  components: Component[],
  DPI: number = 4,
  editorSize: { width: number; height: number } = DEFAULT_EDITOR_SIZE
) => {
  console.log('开始导出PDF...', {
    componentsCount: components.length,
    DPI
  })

  try {
    // 等待PDF模块加载完成
    let retryCount = 0;
    const maxRetries = 10;
    const retryInterval = 500;

    while (retryCount < maxRetries) {
      if (typeof window.PDF?.PDFDocument === 'function' && typeof window.PDF?.blobStream === 'function') {
        break;
      }
      await new Promise(resolve => setTimeout(resolve, retryInterval));
      retryCount++;
      console.log(`等待PDF模块加载... 第${retryCount}次尝试`);
    }

    if (retryCount >= maxRetries) {
      throw new Error('PDF模块加载超时，请刷新页面重试');
    }

    // 动态获取 PDFDocument 和 blobStream
    const PDFDocument = window.PDF?.PDFDocument;
    const blobStream = window.PDF?.blobStream;
    if (typeof PDFDocument !== 'function' || typeof blobStream !== 'function') {
      throw new Error('PDF模块未正确加载');
    }

    console.log('开始创建canvas...')

    // 使用更高分辨率渲染 canvas
    const width = editorSize.width
    const height = editorSize.height
    const canvas = createCanvas(width * DPI, height * DPI)
    const ctx = canvas.getContext('2d')

    if (!ctx) {
      throw new Error('无法创建canvas上下文')
    }

    console.log('Canvas创建成功，开始渲染组件...')

    // 设置白色背景
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, width * DPI, height * DPI)
    
    // 预处理所有组件，确保图片URL都使用代理
    const processedComponents = components.map(component => {
      if (component.type === 'image' && component.imageUrl) {
        return {
          ...component,
          imageUrl: component.imageUrl.match(/^https?:\/\//) ? 
            `/api/proxy/image?url=${encodeURIComponent(component.imageUrl)}` : 
            component.imageUrl
        };
      }
      return component;
    });
    
    // 渲染所有组件
    for (const component of processedComponents) {
      console.log('渲染组件:', {
        type: component.type,
        position: { x: component.x, y: component.y }
      })
      await renderComponentToCanvas(ctx, component, DPI)
    }

    console.log('组件渲染完成，开始创建PDF...')

    try {
      // 获取canvas图像数据
      const base64 = canvas.toDataURL('image/jpeg');
      
      // 检查模块加载状态
      console.log('PDF模块加载状态:', {
        PDFDocument: typeof PDFDocument,
        blobStream: typeof blobStream,
        BlobStream: typeof (window as any).BlobStream
      })

      // 创建PDF文档
      const doc = new PDFDocument({
        size: [width, height],
        autoFirstPage: true,
        margin: 0
      })

      console.log('PDF文档创建成功，开始写入数据...')

      // 创建blob流
      if (typeof blobStream !== 'function') {
        throw new Error('blobStream 模块未正确加载')
      }
      const stream = doc.pipe(blobStream())

      // 将canvas图像添加到PDF
      doc.image(base64, 0, 0, { width, height })

      // 完成PDF
      doc.end()

      console.log('PDF数据写入完成，等待生成下载链接...')

      // 当流完成时,创建下载链接
      return new Promise<string>((resolve, reject) => {
        stream.on('finish', function() {
          try {
            console.log('PDF流处理完成，生成下载链接...')
            const url = stream.toBlobURL('application/pdf')
            console.log('PDF导出成功！')
            resolve(url)
          } catch (error: unknown) {
            console.error('生成下载链接失败:', error)
            reject(error)
          }
        })

        stream.on('error', (error: unknown) => {
          console.error('PDF流处理错误:', error)
          reject(error)
        })
      })
    } catch (error) {
      console.error('Canvas导出失败:', error);
      
      // 如果Canvas导出失败，尝试直接使用组件数据创建PDF
      console.log('尝试直接创建PDF...');
      
      // 创建PDF文档
      const doc = new PDFDocument({
        size: [width, height],
        autoFirstPage: true,
        margin: 0
      });
      
      // 创建blob流
      const stream = doc.pipe(blobStream());
      
      // 设置白色背景
      doc.rect(0, 0, width, height).fill('#ffffff');
      
      // 直接在PDF上渲染组件
      for (const component of processedComponents) {
        if (component.type === 'text-title' || component.type === 'text-basic') {
          if (component.content) {
            doc.font('Helvetica')
               .fontSize(component.fontSize || 14)
               .fillColor(component.color || '#000000')
               .text(component.content, component.x, component.y, {
                 width: component.width,
                 align: component.textAlign || 'left'
               });
          }
        } else if (component.type === 'image' && component.imageUrl) {
          try {
            doc.image(component.imageUrl, component.x, component.y, {
              width: component.width,
              height: component.height
            });
          } catch (err) {
            console.error('无法在PDF中渲染图片:', err);
          }
        } else if (component.type === 'divider-solid') {
          doc.moveTo(component.x, component.y + component.height / 2)
             .lineTo(component.x + component.width, component.y + component.height / 2)
             .lineWidth(component.thickness || 1)
             .stroke(component.color || '#000000');
        }
      }
      
      // 完成PDF
      doc.end();
      
      console.log('直接创建PDF完成，等待生成下载链接...');
      
      // 当流完成时,创建下载链接
      return new Promise<string>((resolve, reject) => {
        stream.on('finish', function() {
          try {
            console.log('PDF流处理完成，生成下载链接...');
            const url = stream.toBlobURL('application/pdf');
            console.log('PDF导出成功！');
            resolve(url);
          } catch (error: unknown) {
            console.error('生成下载链接失败:', error);
            reject(error);
          }
        });
        
        stream.on('error', (error: unknown) => {
          console.error('PDF流处理错误:', error);
          reject(error);
        });
      });
    }
  } catch (error: unknown) {
    console.error('PDF导出失败:', error)
    throw error
  }
}
