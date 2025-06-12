<template>
  <div class="image-uploader">
    <div class="upload-area" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
      <input
        type="file"
        ref="fileInput"
        accept="image/*"
        style="display: none"
        @change="handleFileChange"
      />
      <div v-if="!imageUrl" class="upload-placeholder">
        <i class="el-icon-upload"></i>
        <div class="upload-text">点击或拖拽图片到此处上传</div>
      </div>
      <img v-else :src="imageUrl" class="preview-image" @error="handleImageError" />
    </div>
    <div v-if="error" class="error-message">{{ error }}</div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ImageUploader',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      imageUrl: this.value,
      error: '',
      uploading: false
    };
  },
  methods: {
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    async handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        await this.uploadFile(file);
      }
    },
    async handleDrop(event) {
      const file = event.dataTransfer.files[0];
      if (file && file.type.startsWith('image/')) {
        await this.uploadFile(file);
      }
    },
    async uploadFile(file) {
      if (!file.type.startsWith('image/')) {
        this.error = '请上传图片文件';
        return;
      }

      if (file.size > 10 * 1024 * 1024) {
        this.error = '图片大小不能超过10MB';
        return;
      }

      this.error = '';
      this.uploading = true;

      try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await axios.post('/api/upload/image', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        if (response.data && response.data.url) {
          this.imageUrl = response.data.url;
          this.$emit('input', response.data.url);
          this.$emit('upload-success', {
            url: response.data.url,
            fileName: response.data.fileName,
            objectName: response.data.objectName
          });
        }
      } catch (error) {
        console.error('Upload error:', error);
        this.error = '上传失败，请重试';
      } finally {
        this.uploading = false;
      }
    },
    handleImageError() {
      this.error = '图片加载失败';
      this.imageUrl = '';
      this.$emit('input', '');
    }
  },
  watch: {
    value(newVal) {
      this.imageUrl = newVal;
    }
  }
};
</script>

<style scoped>
.image-uploader {
  width: 100%;
  max-width: 300px;
}

.upload-area {
  width: 100%;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
}

.upload-placeholder {
  text-align: center;
  color: #909399;
}

.upload-text {
  margin-top: 10px;
  font-size: 14px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}
</style> 