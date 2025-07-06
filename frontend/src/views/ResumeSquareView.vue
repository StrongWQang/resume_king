<template>
  <div class="resume-square-view">
    <p>这里将展示其他用户分享的简历模板。</p>
    <div v-if="loading">Loading resumes...</div>
    <div v-else-if="error" class="error-message">{{ error }}</div>
    <div v-else class="resume-list">
      <div v-for="resume in resumes" :key="resume.id" class="resume-card">
        <img :src="resume.imageUrl" :alt="resume.title" class="resume-image" />
        <h3>{{ resume.title }}</h3>
        <p>{{ resume.description }}</p>
        <div class="actions">
          <button @click="likeResume(resume.id)" class="like-button">
            👍 点赞
          </button>
          <span class="like-count">{{ resume.likes }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import axios from 'axios';

interface Resume {
  id: number;
  title: string;
  description: string;
  likes: number;
  imageUrl: string;
}

const resumes = ref<Resume[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const fetchResumes = async () => {
  try {
    loading.value = true;
    // 假设后端接口为 /api/resumes
    const response = await axios.get('/api/resumes');
    resumes.value = response.data;
  } catch (err) {
    error.value = 'Failed to fetch resumes. Please try again later.';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const likeResume = (id: number) => {
  const resume = resumes.value.find((r) => r.id === id);
  if (resume) {
    resume.likes++;
  }
};

onMounted(() => {
  fetchResumes();
});
</script>

<style scoped>
.resume-square-view {
  padding: 0;
  text-align: center;
}

h1 {
  margin-top: 20px;
  font-size: 2.5em;
  color: #333;
}

p {
  color: #666;
  margin-bottom: 30px;
}

.resume-image {
  width: 100%;
  height: 200px; /* 调整图片高度 */
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 10px;
}

h3 {
  font-size: 1.2em;
  margin-bottom: 5px;
  color: #333;
}

p {
  font-size: 0.9em;
  color: #777;
  flex-grow: 1; /* 允许描述占据更多空间 */
}

.actions {
  margin-top: 15px;
  display: flex;
  align-items: center;
  justify-content: center; /* 按钮居中 */
  gap: 10px;
  width: 100%;
}

.like-button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 20px; /* 更圆润的按钮 */
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background-color 0.2s ease-in-out;
}

.like-button:hover {
  background-color: #369f6f;
}

.like-count {
  font-weight: bold;
  color: #333;
  font-size: 1.1em;
}

.resume-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.resume-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  width: calc(20% - 20px); /* 调整宽度以适应一行5个，减去gap */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: transform 0.2s ease-in-out;
}

.resume-card:hover {
  transform: translateY(-5px);
}

.resume-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start; /* 从左侧开始排列 */
  gap: 20px;
  margin-top: 20px;
  padding: 0 20px; /* 增加左右内边距 */
}

.resume-image {
  width: 100%;
  height: 300px; /* 固定高度，保持图片比例 */
  object-fit: cover; /* 裁剪图片以适应容器 */
  border-radius: 4px;
  margin-bottom: 10px;
}

.actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.like-button {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.like-button:hover {
  background-color: #369f6f;
}

.like-count {
  font-weight: bold;
  color: #333;
}
</style>