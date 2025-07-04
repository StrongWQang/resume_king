<template>
  <div class="image-uploader">
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleFileChange"
    />
    <div
      class="uploader"
      @click="triggerFileSelect"
    >
      <div v-if="!imageUrl" class="upload-placeholder">
        <el-icon class="upload-icon"><upload-filled /></el-icon>
        <div class="upload-text">点击上传图片</div>
        <div class="upload-tip">支持 jpg、png 格式</div>
      </div>
      <img v-else :src="imageUrl" class="preview-image" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { imageLoader } from '../utils/imageLoader'

const props = defineProps<{
  componentId: string
}>()

const emit = defineEmits<{
  (e: 'image-uploaded', url: string, width: number, height: number): void
}>()

const imageUrl = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)

const triggerFileSelect = () => {
  console.log('ImageUploader: triggerFileSelect called')
  if (fileInputRef.value) {
    console.log('ImageUploader: clicking file input')
    fileInputRef.value.click()
  } else {
    console.error('ImageUploader: fileInputRef is null')
  }
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || !input.files[0]) {
    return
  }

  const file = input.files[0]
  console.log('Before upload:', file)

  // 验证文件
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return
  }

  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await fetch('/api/upload/image', {
      method: 'POST',
      body: formData,
      headers: {
        'Accept': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('Upload failed')
    }

    const data = await response.json()
    if (!data || !data.url) {
      throw new Error('Invalid response format')
    }

    console.log('Upload success:', data)
    
    // 处理图片 URL
    const processedUrl = data.url.startsWith('http') ? data.url : `${window.location.origin}${data.url}`
    console.log('Processed image URL:', processedUrl)
    
    // 使用 Image 对象加载图片
    const img = new Image()
    img.crossOrigin = 'anonymous'  // 添加跨域支持
    
    // 添加超时处理
    const timeout = setTimeout(() => {
      console.error('Image load timeout:', processedUrl)
      ElMessage.error('图片加载超时，请重试')
      img.src = '' // 清除src以触发onerror
    }, 10000) // 10秒超时
    
    img.onload = () => {
      clearTimeout(timeout)
      console.log('Image loaded successfully:', img.width, 'x', img.height)
      imageUrl.value = processedUrl
      emit('image-uploaded', processedUrl, img.width, img.height)
      ElMessage.success('图片上传成功')
    }
    
    img.onerror = (e) => {
      clearTimeout(timeout)
      console.error('Image load error:', e)
      console.error('Failed to load image from URL:', processedUrl)
      
      // 尝试使用代理URL
      const proxyUrl = imageLoader.getProxyImageUrl(processedUrl)
      console.log('Trying proxy URL:', proxyUrl)
      
      const proxyImg = new Image()
      proxyImg.crossOrigin = 'anonymous'
      
      proxyImg.onload = () => {
        console.log('Image loaded successfully through proxy:', proxyImg.width, 'x', proxyImg.height)
        imageUrl.value = proxyUrl
        emit('image-uploaded', proxyUrl, proxyImg.width, proxyImg.height)
        ElMessage.success('图片上传成功')
      }
      
      proxyImg.onerror = (proxyError) => {
        console.error('Proxy image load error:', proxyError)
        ElMessage.error('图片加载失败，请检查服务器配置和图片 URL')
      }
      
      proxyImg.src = proxyUrl
    }
    
    // 添加随机参数防止缓存
    const timestamp = Date.now()
    const random = Math.random()
    img.src = `${processedUrl}?t=${timestamp}&r=${random}`

    // 重置 input 值，允许重复上传相同文件
    input.value = ''
  } catch (error) {
    console.error('Upload error:', error)
    ElMessage.error('图片上传失败：' + (error instanceof Error ? error.message : '未知错误'))
  }
}

onMounted(() => {
  console.log('ImageUploader mounted')
})

defineExpose({
  triggerFileSelect
})
</script>

<style scoped>
.image-uploader {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.uploader {
  width: 100%;
  height: 100%;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.uploader:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c939d;
  padding: 20px;
  cursor: pointer;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 16px;
  margin-bottom: 4px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
</style> 