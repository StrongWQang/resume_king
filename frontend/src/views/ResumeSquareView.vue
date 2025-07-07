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

    <div v-else class="resume-list">
      <div
        v-for="resume in resumes"
        :key="resume.id"
        class="resume-card"
        :data-id="resume.id"
      >
        <div class="resume-image-container">
          <img
            :src="resume.imageUrl"
            :alt="resume.title"
            class="resume-image"
          />
        </div>
        <div class="resume-content">
          <h3 class="resume-title">{{ resume.title }}</h3>
          <p class="resume-description">{{ resume.description }}</p>
          <div class="actions">
            <button
              @click="likeResume(resume.id)"
              class="like-button"
              :class="{ liked: resume.isLiked }"
            >
              <span class="like-icon">👍</span>
              <span>{{ resume.isLiked ? "已点赞" : "点赞" }}</span>
            </button>
            <span class="like-count">💕 {{ resume.like }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import axios from "axios";

interface Resume {
  id: number;
  title: string;
  description: string;
  like: number;
  imageUrl: string;
  isLiked?: boolean;
}

const resumes = ref<Resume[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const fetchResumes = async () => {
  try {
    loading.value = true;
    const response = await axios.get("/api/resumes/templates");
    resumes.value = response.data.map((resume: Resume) => ({
      ...resume,
      isLiked: resume.isLiked || false,
    }));
  } catch (err) {
    error.value = "加载失败，请稍后重试";
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const likeResume = async (id: number) => {
  const resume = resumes.value.find((r) => r.id === id);
  if (resume) {
    try {
      if (!resume.isLiked) {
        // 点赞
        await axios.post(`/api/resumes/${id}/like`);
        resume.isLiked = true;
        resume.like += 1;
        // 添加动画效果
        const likeCountElement = document.querySelector(
          `.resume-card[data-id='${id}'] .like-count`
        );
        if (likeCountElement) {
          likeCountElement.classList.add("animated");
          setTimeout(() => {
            likeCountElement.classList.remove("animated");
          }, 300);
        }
      } else {
        // 取消点赞
        await axios.delete(`/api/resumes/${id}/like`);
        resume.isLiked = false;
        resume.like -= 1;
        // 添加动画效果
        const likeCountElement = document.querySelector(
          `.resume-card[data-id='${id}'] .like-count`
        );
        if (likeCountElement) {
          likeCountElement.classList.add("animated");
          setTimeout(() => {
            likeCountElement.classList.remove("animated");
          }, 300);
        }
      }
    } catch (err) {
      console.error("点赞操作失败:", err);
      // 操作失败时恢复原状态
      resume.isLiked = !resume.isLiked;
      resume.like += resume.isLiked ? 1 : -1;
    }
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
}

.resume-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

.resume-image-container {
  position: relative;
  padding-top: 56.25%; /* 16:9 比例 */
  overflow: hidden;
}

.resume-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.resume-card:hover .resume-image {
  transform: scale(1.05);
}

.resume-content {
  padding: 20px;
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
}

.actions {
  display: flex;
  align-items: center;
  gap: 15px;
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
}
</style>