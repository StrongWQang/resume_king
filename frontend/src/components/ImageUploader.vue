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
    
    // 验证图片 URL 是否可访问
    try {
      const imgResponse = await fetch(processedUrl, { method: 'HEAD' })
      if (!imgResponse.ok) {
        throw new Error(`Image URL not accessible: ${imgResponse.status} ${imgResponse.statusText}`)
      }
      console.log('Image URL is accessible')
    } catch (error) {
      console.error('Image URL validation failed:', error)
      ElMessage.warning('图片 URL 可能无法访问，请检查服务器配置')
    }
    
    imageUrl.value = processedUrl

    const img = new Image()
    img.onload = () => {
      console.log('Image loaded successfully:', img.width, 'x', img.height)
      emit('image-uploaded', processedUrl, img.width, img.height)
    }
    img.onerror = (e) => {
      console.error('Image load error:', e)
      console.error('Failed to load image from URL:', processedUrl)
      ElMessage.error('图片加载失败，请检查服务器配置和图片 URL')
    }
    img.src = processedUrl
    ElMessage.success('图片上传成功')

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