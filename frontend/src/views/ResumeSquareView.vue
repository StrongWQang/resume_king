<template>
  <div class="resume-square-view">
    <h1 class="page-title">简历广场</h1>
    <p class="subtitle">发现优秀的简历模板，获取灵感</p>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载简历模板...</p>
    </div>

    <div v-else-if="error" class="error-message">
      <i class="error-icon">⚠️</i>
      <p>{{ error }}</p>
    </div>

    <template v-else>
      <!-- 发布简历按钮 -->
      <div class="publish-section">
        <el-button
          type="primary"
          size="large"
          @click="showPublishDialog"
          class="publish-button"
          icon="Plus"
        >
          发布我的简历模板
        </el-button>
      </div>

      <!-- 简历列表 -->
      <div class="resume-list">
        <div
          v-for="resume in resumes"
          :key="resume.id"
          class="resume-card"
          :data-id="resume.idString"
        >
          <div class="resume-image-container" @click="openPreview(resume)">
            <!-- 使用类似LeftPanel.vue的渲染方式 -->
            <div class="resume-preview-content">
              <!-- 添加一个缩放容器来保持简历比例并填充整个区域 -->
              <div 
                v-for="component in resume.components" 
                :key="component.id"
                class="resume-preview-component"
                :style="getComponentStyle(component, 0.6)"
              >
                <template v-if="component.type === 'text-title' || component.type === 'text-basic'">
                  {{ component.content }}
                </template>
                <template v-else-if="component.type === 'divider-solid'">
                  <div 
                    class="divider"
                    :style="{
                      width: '100%',
                      height: component.thickness + 'px',
                      backgroundColor: component.color,
                      margin: (component.padding * 0.15) + 'px 0'
                    }"
                  ></div>
                </template>
                <template v-else-if="component.type === 'image'">
                  <img 
                    :src="component.imageUrl" 
                    :alt="component.alt || ''"
                    class="preview-image"
                    :style="{
                      width: '100%',
                      height: '100%',
                      objectFit: component.objectFit || 'contain'
                    }"
                    @error="handleImageError"
                  />
                </template>
              </div>
            </div>
            <div class="resume-overlay">
              <span class="click-to-preview">点击预览</span>
            </div>
          </div>
          <div class="resume-content">
            <h3 class="resume-title">{{ resume.title }}</h3>
            <p class="resume-description">{{ resume.description }}</p>
            <div class="actions">
              <button
                @click="likeResume(resume)"
                class="like-button"
                :class="{ liked: resume.isLiked }"
              >
                <span class="like-icon">👍</span>
                <span>{{ resume.isLiked ? "已点赞" : "点赞" }}</span>
              </button>
              <span class="like-count">💕 {{ resume.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
    
    <!-- 分页控件 -->
    <div v-if="!loading && !error && totalPages > 0" class="pagination-container">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalItems"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 预览弹窗 -->
    <div v-if="previewVisible" class="preview-modal-overlay" @click.self="closePreview">
      <div class="preview-modal">
        <div class="preview-modal-header">
          <h3>{{ selectedResume?.title || '简历预览' }}</h3>
          <button class="close-button" @click="closePreview">×</button>
        </div>
        <div class="preview-modal-body">
          <div class="preview-modal-resume">
            <div 
              v-for="component in selectedResume?.components" 
              :key="component.id"
              class="resume-preview-component"
              :style="getComponentStyle(component, 0.8)"
            >
              <template v-if="component.type === 'text-title' || component.type === 'text-basic'">
                {{ component.content }}
              </template>
              <template v-else-if="component.type === 'divider-solid'">
                <div 
                  class="divider"
                  :style="{
                    width: '100%',
                    height: component.thickness + 'px',
                    backgroundColor: component.color,
                    margin: (component.padding * 0.4) + 'px 0'
                  }"
                ></div>
              </template>
              <template v-else-if="component.type === 'image'">
                <img 
                  :src="component.imageUrl" 
                  :alt="component.alt || ''"
                  class="preview-image"
                  :style="{
                    width: '100%',
                    height: '100%',
                    objectFit: component.objectFit || 'contain'
                  }"
                  @error="handleImageError"
                />
              </template>
            </div>
          </div>
        </div>
        <div class="preview-modal-footer">
          <el-tooltip content="将此模板应用到您的简历编辑器中" placement="top" effect="light">
            <button class="apply-button" @click="applyTemplate">
              <i class="apply-icon">✓</i>
              立即应用
            </button>
          </el-tooltip>
          <button class="close-preview-button" @click="closePreview">退出预览</button>
        </div>
      </div>
    </div>

    <!-- 发布简历弹窗 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布简历模板"
      width="50%"
      :before-close="handlePublishDialogClose"
    >
      <div class="publish-dialog-content">
        <el-form ref="publishFormRef" :model="publishForm" :rules="publishFormRules" label-width="100px">
          <el-form-item label="简历ID" prop="resumeId">
            <el-input
              v-model="publishForm.resumeId"
              placeholder="请输入要发布的简历ID"
              @blur="validateResumeId"
            />
          </el-form-item>
          <el-form-item label="模板标题" prop="title">
            <el-input
              v-model="publishForm.title"
              placeholder="请输入简历模板的标题"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="模板描述" prop="description">
            <el-input
              v-model="publishForm.description"
              type="textarea"
              placeholder="请描述这个简历模板的特点和适用场景"
              rows="4"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="previewResume" :disabled="!publishForm.resumeId">
            预览简历
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 预览简历弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="预览简历"
      width="80%"
      :before-close="handlePreviewDialogClose"
    >
      <div class="preview-dialog-content">
        <div v-if="previewLoading" class="preview-loading">
          <el-icon class="is-loading"><loading /></el-icon>
          <p>正在加载简历数据...</p>
        </div>
        <div v-else-if="previewError" class="preview-error">
          <el-icon><warning /></el-icon>
          <p>{{ previewError }}</p>
        </div>
        <div v-else class="preview-resume">
          <div class="preview-resume-content">
            <div 
              v-for="component in previewComponents" 
              :key="component.id"
              class="resume-preview-component"
              :style="getComponentStyle(component, 0.8)"
            >
              <template v-if="component.type === 'text-title' || component.type === 'text-basic'">
                {{ component.content }}
              </template>
              <template v-else-if="component.type === 'divider-solid'">
                <div 
                  class="divider"
                  :style="{
                    width: '100%',
                    height: component.thickness + 'px',
                    backgroundColor: component.color,
                    margin: (component.padding * 0.4) + 'px 0'
                  }"
                ></div>
              </template>
              <template v-else-if="component.type === 'image'">
                <img 
                  :src="component.imageUrl" 
                  :alt="component.alt || ''"
                  class="preview-image"
                  :style="{
                    width: '100%',
                    height: '100%',
                    objectFit: component.objectFit || 'contain'
                  }"
                  @error="handleImageError"
                />
              </template>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewDialogVisible = false">取消</el-button>
          <el-button type="success" @click="confirmPublish" :loading="publishLoading">
            确认发布
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import axios from "axios";
import { useResumeStore, Component } from "../store/resume";
import { ElMessage, ElMessageBox, ElPagination } from 'element-plus';
import { useRouter } from 'vue-router';
import { Plus, Loading, Warning } from '@element-plus/icons-vue';

const store = useResumeStore();
const router = useRouter();

// 修改Resume接口定义
interface Resume {
  id: string;  // 使用string类型
  idString: string; // 后端返回的字符串形式ID
  title: string;
  description: string;
  like: number;
  imageUrl: string;
  isLiked?: boolean;
  components?: Component[];
}

const resumes = ref<Resume[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const imageLoadErrors = ref(new Set());
const previewVisible = ref(false);
const selectedResume = ref<Resume | null>(null);

// 简历原始尺寸 (A4纸的像素尺寸，假设为210mm x 297mm，以96dpi计算)
const RESUME_WIDTH = 794;  // A4宽度
const RESUME_HEIGHT = 1123; // A4高度

// 分页相关状态
const currentPage = ref(1);
const pageSize = ref(12);
const totalItems = ref(0);
const totalPages = ref(0);

// 发布简历相关状态
const publishDialogVisible = ref(false);
const previewDialogVisible = ref(false);
const publishForm = ref({
  resumeId: '',
  title: '',
  description: ''
});

// 表单验证规则
const publishFormRules = {
  resumeId: [
    { required: true, message: '请输入简历ID', trigger: 'blur' },
    { 
      pattern: /^\d{18,19}$/, 
      message: '请输入正确的简历ID（18-19位数字）', 
      trigger: 'blur' 
    }
  ],
  title: [
    { required: true, message: '请输入模板标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入模板描述', trigger: 'blur' },
    { min: 10, max: 200, message: '描述长度应在 10 到 200 个字符', trigger: 'blur' }
  ]
};
const publishFormRef = ref(null);
const previewLoading = ref(false);
const previewError = ref('');
const previewComponents = ref([]);
const publishLoading = ref(false);

// 动态计算组件样式
const getComponentStyle = (component: Component, scaleFactor: number) => {
  return {
    position: 'absolute',
    left: (component.x * scaleFactor) + 'px',
    top: (component.y * scaleFactor) + 'px',
    width: (component.width * scaleFactor) + 'px',
    height: (component.height * scaleFactor) + 'px',
    color: component.color || '#000000',
    fontSize: (component.fontSize * scaleFactor) + 'px',
    textAlign: component.textAlign || 'left',
    fontFamily: component.fontFamily || 'Arial, sans-serif',
    lineHeight: component.lineHeight || 'normal',
    fontWeight: component.fontWeight || 'normal',
    overflow: 'hidden',
    textOverflow: 'ellipsis'
  };
};

// 处理简历组件数据，确保所有必要的属性都存在
const processComponents = (components: Component[]) => {
  return components.map(component => ({
    ...component,
    // 设置默认值，确保渲染不会出错
    x: component.x || 0,
    y: component.y || 0,
    width: component.width || 100,
    height: component.height || 20,
    fontSize: component.fontSize || 12,
    color: component.color || '#000000',
    textAlign: component.textAlign || 'left',
  }));
};

const fetchResumes = async (page = 1) => {
  try {
    loading.value = true;
    currentPage.value = page;
    
    // 使用分页API获取简历模板
    const response = await axios.get("/api/resume-square/templates", {
      params: {
        page: page,
        size: pageSize.value
      }
    });
    
    if (!response.data || !response.data.templates) {
      throw new Error("Invalid response format");
    }
    
    // 更新分页信息
    totalItems.value = response.data.totalItems || 0;
    totalPages.value = response.data.totalPages || 0;
    currentPage.value = response.data.currentPage || 1;
    
    // 获取每个简历的组件数据
    const resumesWithComponents = await Promise.all(
      response.data.templates.map(async (resume: Resume) => {
        if (!resume || !resume.idString) {
          console.error("Invalid resume data:", resume);
          return null;
        }
        
        try {
          // 获取简历的组件数据
          const componentResponse = await axios.get(`/api/resumes/${resume.idString}`);
          return {
            ...resume,
            isLiked: resume.isLiked || false,
            components: processComponents(componentResponse.data || [])
          };
        } catch (err) {
          console.error(`获取简历组件失败: ${resume.idString}`, err);
          return {
            ...resume,
            isLiked: resume.isLiked || false,
            components: []
          };
        }
      })
    );
    
    // 过滤掉无效的简历数据
    resumes.value = resumesWithComponents.filter(resume => resume !== null) as Resume[];
  } catch (err) {
    console.error("加载简历列表失败:", err);
    error.value = "加载失败，请稍后重试";
  } finally {
    loading.value = false;
  }
};

// 处理页码变化
const handlePageChange = (page: number) => {
  fetchResumes(page);
};

const handleTemplateClick = (resume: Resume) => {
  // 使用idString而不是id
  router.push(`/editor?template=${resume.idString}`);
};

const likeResume = async (resume: Resume) => {
  try {
    if (!resume.isLiked) {
      // 使用idString而不是id
      await axios.post(`/api/resume-square/${resume.idString}/like`);
      resume.isLiked = true;
      resume.likeCount += 1;
      // 添加动画效果
      const likeCountElement = document.querySelector(
        `.resume-card[data-id='${resume.idString}'] .like-count`
      );
      if (likeCountElement) {
        likeCountElement.classList.add("animated");
        setTimeout(() => {
          likeCountElement.classList.remove("animated");
        }, 300);
      }
    }
  } catch (error) {
    console.error('点赞失败:', error);
  }
};

// 打开预览
const openPreview = (resume: Resume) => {
  selectedResume.value = resume;
  previewVisible.value = true;
  // 防止滚动
  document.body.style.overflow = 'hidden';
};

// 关闭预览
const closePreview = () => {
  previewVisible.value = false;
  // 恢复滚动
  document.body.style.overflow = '';
};

// 应用模板
const applyTemplate = async () => {
  if (!selectedResume.value) return;
  
  // 检查是否已有内容
  if (store.components.length > 0) {
    try {
      await ElMessageBox.confirm(
        '当前简历已有内容，使用模板将覆盖现有内容，是否继续？',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
      );
    } catch {
      // 用户点击取消
      return;
    }
  }
  
  try {
    // 使用idString而不是id
    const templateId = selectedResume.value.idString;
    
    // 从后端获取最新的模板数据
    const response = await axios.get(`/api/resumes/${templateId}`);
    const templateData = response.data;
    
    // 处理组件数据中的图片URL
    const processedComponents = templateData.map(component => {
      if (component.type === 'image' && component.imageUrl) {
        // 确保图片URL是正确的
        return {
          ...component,
          imageUrl: component.imageUrl
        };
      }
      return component;
    });
    
    // 设置到简历中
    store.setComponents(processedComponents);
    
    // 自动保存到本地存储
    store.autoSave();
    
    ElMessage.success('已应用简历模板');
    closePreview();
    
    // 导航到简历编辑页面，并传递模板ID
    router.push(`/?template=${templateId}`);
  } catch (error) {
    console.error('应用模板失败:', error);
    ElMessage.error('应用模板失败，请重试');
  }
};

// 处理图片加载错误
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement;
  const imgUrl = img.src;
  
  if (imageLoadErrors.value.has(imgUrl)) {
    return;
  }
  
  imageLoadErrors.value.add(imgUrl);
  
  console.error('图片加载失败:', {
    src: img.src,
    alt: img.alt
  });
  
  // 使用默认图片
  img.src = '/src/assets/default-logo.png';
  
  // 添加错误样式
  img.style.border = '1px solid #ff4d4f';
  img.style.backgroundColor = '#fff2f0';
};

// 发布简历相关方法
const showPublishDialog = () => {
  publishDialogVisible.value = true;
  // 重置表单
  publishForm.value = {
    resumeId: '',
    title: '',
    description: ''
  };
  if (publishFormRef.value) {
    publishFormRef.value.resetFields();
  }
};

const handlePublishDialogClose = (done) => {
  ElMessageBox.confirm('确认关闭？未保存的内容将丢失。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    done();
  }).catch(() => {
    // 用户点击取消
  });
};

const validateResumeId = async () => {
  if (!publishForm.value.resumeId) return;
  
  try {
    // 使用字符串形式的ID
    const response = await axios.get(`/api/resumes/${publishForm.value.resumeId}`);
    if (response.data) {
      ElMessage.success('简历ID验证成功');
    }
  } catch (error) {
    ElMessage.error('简历ID不存在，请检查后重新输入');
    publishForm.value.resumeId = '';
  }
};

const previewResume = async () => {
  if (!publishFormRef.value) return;
  
  try {
    await publishFormRef.value.validateField('resumeId');
  } catch (error) {
    return;
  }
  
  previewLoading.value = true;
  previewError.value = '';
  previewComponents.value = [];
  
  try {
    // 使用字符串形式的ID
    const response = await axios.get(`/api/resumes/${publishForm.value.resumeId}`);
    previewComponents.value = processComponents(response.data || []);
    previewDialogVisible.value = true;
    publishDialogVisible.value = false;
  } catch (error) {
    previewError.value = '加载简历数据失败：' + (error.response?.data?.message || error.message);
  } finally {
    previewLoading.value = false;
  }
};

const handlePreviewDialogClose = (done) => {
  previewDialogVisible.value = false;
  publishDialogVisible.value = true;
  done && done();
};

const confirmPublish = async () => {
  if (!publishFormRef.value) return;
  
  try {
    await publishFormRef.value.validate();
  } catch (error) {
    previewDialogVisible.value = false;
    publishDialogVisible.value = true;
    return;
  }
  
  publishLoading.value = true;
  
  try {
    const response = await axios.post('/api/resume-publish/submit', {
      // 使用字符串形式的ID
      resumeId: publishForm.value.resumeId,
      title: publishForm.value.title,
      description: publishForm.value.description,
      userId: null // 暂时不设置用户ID
    });
    
    if (response.data.success) {
      ElMessage.success('简历模板发布申请提交成功！请等待管理员审核。');
      previewDialogVisible.value = false;
      
      // 显示申请详情
      ElMessageBox.alert(
        `申请ID：${response.data.requestId}\n` +
        '您的简历模板发布申请已提交，管理员将在1-3个工作日内完成审核。\n' +
        '审核通过后，您的简历模板将出现在简历广场中。',
        '申请提交成功',
        {
          confirmButtonText: '确定',
          type: 'success'
        }
      );
    } else {
      throw new Error(response.data.message || '发布申请失败');
    }
  } catch (error) {
    console.error('发布申请失败:', error);
    ElMessage.error('发布申请失败：' + (error.response?.data?.message || error.message));
  } finally {
    publishLoading.value = false;
  }
};

onMounted(() => {
  fetchResumes();
});
</script>

<style scoped>
.resume-square-view {
  padding: 20px;
  max-width: 1440px;
  margin: 0 auto;
}

.page-title {
  font-size: 2.5em;
  color: #2c3e50;
  margin-bottom: 10px;
}

.subtitle {
  color: #666;
  font-size: 1.2em;
  margin-bottom: 40px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #42b983;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-message {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #dc3545;
  padding: 20px;
  background-color: #fff3f3;
  border-radius: 8px;
  margin: 20px 0;
}

.error-icon {
  font-size: 24px;
  margin-right: 10px;
}

.resume-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  padding: 20px 0;
}

.resume-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.resume-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

.resume-image-container {
  position: relative;
  padding-top: 141.42%; /* A4纸比例 (1:1.414) */
  overflow: hidden;
  background-color: #f5f5f5;
  border-bottom: 1px solid #eaeaea;
  flex-shrink: 0;
  cursor: pointer;
}

.resume-preview-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: white;
  overflow: hidden;
}

/* 添加缩放容器来保持简历比例并填充整个区域 */
.resume-preview-scaling-container {
  position: relative;
  width: 100%;
  height: 100%;
  /* 确保内容不溢出 */
  overflow: hidden;
  /* 设置背景色 */
  background-color: white;
  /* 添加阴影效果，增强立体感 */
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) inset;
}

.resume-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.resume-image-container:hover .resume-overlay {
  opacity: 1;
}

.click-to-preview {
  background-color: rgba(66, 185, 131, 0.9);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.resume-preview-component {
  position: absolute;
  white-space: pre-wrap;
  word-break: break-word;
  transform-origin: top left;
}

.divider {
  width: 100%;
  background-color: currentColor;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 2px;
  background-color: #f5f5f5;
}

.resume-content {
  padding: 20px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.resume-title {
  font-size: 1.3em;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 600;
}

.resume-description {
  color: #666;
  font-size: 0.95em;
  line-height: 1.5;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex-grow: 1;
}

.actions {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: auto;
}

.like-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background-color: #42b983;
  color: white;
  font-size: 0.9em;
  cursor: pointer;
  transition: all 0.2s ease;
}

.like-button:hover {
  background-color: #3aa876;
  transform: scale(1.05);
}

.like-button:active {
  transform: scale(0.98);
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2);
}

.like-button.liked {
  background-color: #2c3e50;
}

.like-icon {
  font-size: 1.1em;
}

.like-count {
  font-weight: 700; /* 更粗的字体 */
  color: #e74c3c; /* 醒目的红色 */
  font-size: 1.2em; /* 稍大一点的字体 */
  margin-left: 10px; /* 调整与点赞按钮的距离 */
  padding: 4px 8px; /* 增加内边距 */
  background-color: #ffebee; /* 浅红色背景 */
  border-radius: 12px; /* 圆角 */
  transition: transform 0.2s ease-in-out, background-color 0.2s ease; /* 添加过渡效果 */
}

.like-count.animated {
  transform: scale(1.2); /* 动画效果：放大 */
}

/* 预览弹窗样式 */
.preview-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.preview-modal {
  width: 80%;
  max-width: 900px;
  max-height: 90vh;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.preview-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #eaeaea;
  background-color: #f9f9f9;
}

.preview-modal-header h3 {
  margin: 0;
  font-size: 1.5em;
  color: #2c3e50;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  color: #909399;
  cursor: pointer;
  transition: color 0.2s ease;
}

.close-button:hover {
  color: #2c3e50;
}

.preview-modal-body {
  padding: 24px;
  overflow-y: auto;
  flex-grow: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-modal-resume {
  position: relative;
  width: 450px;
  height: 636px; /* A4比例 */
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  border: 1px solid #eaeaea;
  overflow: hidden;
}

.preview-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 16px 24px;
  border-top: 1px solid #eaeaea;
  background-color: #f9f9f9;
}

.apply-button {
  padding: 12px 28px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  letter-spacing: 0.5px;
}

.apply-button:hover {
  background-color: #3aa876;
  transform: scale(1.03);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.apply-button:active {
  transform: scale(0.98);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.apply-button .apply-icon {
  font-size: 1.2em;
  font-style: normal;
  font-weight: bold;
}

.close-preview-button {
  padding: 10px 24px;
  background-color: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-preview-button:hover {
  background-color: #ebeef5;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin: 40px 0;
  padding: 20px 0;
}

.el-pagination {
  --el-pagination-bg-color: #f5f7fa;
  --el-pagination-button-color: #409eff;
  --el-pagination-hover-color: #42b983;
}

@media (max-width: 768px) {
  .resume-list {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
  }

  .page-title {
    font-size: 2em;
  }

  .subtitle {
    font-size: 1em;
  }
  
  .preview-modal {
    width: 95%;
  }
  
  .preview-modal-resume {
    width: 300px;
    height: 424px;
  }
}

/* 发布简历相关样式 */
.publish-section {
  display: flex;
  justify-content: center;
  margin: 40px 0;
  padding: 30px 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
  border: 2px dashed #42b983;
}

.publish-button {
  font-size: 18px;
  padding: 15px 40px;
  border-radius: 25px;
  background: linear-gradient(135deg, #42b983 0%, #369970 100%);
  border: none;
  box-shadow: 0 8px 16px rgba(66, 185, 131, 0.3);
  transition: all 0.3s ease;
}

.publish-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(66, 185, 131, 0.4);
  background: linear-gradient(135deg, #369970 0%, #2d8a5e 100%);
}

.publish-dialog-content {
  padding: 20px 0;
}

.preview-dialog-content {
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-loading,
.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
}

.preview-loading .el-icon {
  font-size: 32px;
  color: #42b983;
  margin-bottom: 16px;
}

.preview-error .el-icon {
  font-size: 32px;
  color: #f56c6c;
  margin-bottom: 16px;
}

.preview-resume {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 20px;
}

.preview-resume-content {
  position: relative;
  width: 450px;
  height: 636px; /* A4比例 */
  background-color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #eaeaea;
  border-radius: 8px;
  overflow: hidden;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  border-radius: 6px;
  font-weight: 500;
  padding: 10px 20px;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-dialog) {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 20px 24px 16px;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #42b983;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #42b983;
  box-shadow: 0 0 0 1px rgba(66, 185, 131, 0.2);
}

:deep(.el-textarea__inner) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-textarea__inner:hover) {
  border-color: #42b983;
}

:deep(.el-textarea__inner:focus) {
  border-color: #42b983;
  box-shadow: 0 0 0 1px rgba(66, 185, 131, 0.2);
}

:deep(.el-button--primary) {
  background-color: #42b983;
  border-color: #42b983;
}

:deep(.el-button--primary:hover) {
  background-color: #369970;
  border-color: #369970;
}

:deep(.el-button--success) {
  background-color: #67c23a;
  border-color: #67c23a;
}

:deep(.el-button--success:hover) {
  background-color: #5daf34;
  border-color: #5daf34;
}
</style>