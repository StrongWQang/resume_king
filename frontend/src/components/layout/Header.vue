<template>
  <header class="header">
    <!-- 左侧区域：Logo和导航按钮 -->
    <div class="header-left">
      <div class="logo" @click="handleLogoClick" title="点击回到首页">
        <img src="/logo-large.jpg" alt="Resume King Logo" class="logo-image" />
        <span class="logo-text">Resume_King 简历王</span>
      </div>
      
      <!-- 导航按钮组 -->
      <nav class="nav-menu">
        <el-button
          type="info"
          @click="handleResumeSquareClick"
          class="nav-button"
          icon="Grid"
        >简历广场</el-button>
      </nav>
    </div>

    <!-- 右侧区域：操作按钮 -->
    <div class="header-right">
      <!-- 简历ID显示区域 -->
      <div v-if="store.currentResumeId" class="resume-id-area">
        <el-tag type="info" effect="plain">
          简历ID: {{ store.currentResumeId }}
          <el-icon
            class="copy-icon"
            @click="copyResumeId"
          ><copy-document /></el-icon>
        </el-tag>
      </div>

      <!-- 操作按钮组 -->
      <div class="action-toolbar">
        <!-- 文件操作组 -->
        <div class="action-group">
          <div class="group-label">文件</div>
          <div class="group-buttons">
            <el-button
              type="primary"
              @click="handleSave"
              :loading="saveLoading"
              :disabled="saveLoading"
              icon="Document"
            >保存</el-button>
            
            <el-button 
              type="danger" 
              @click="handleClear"
              icon="Delete"
            >清空</el-button>
          </div>
        </div>
        
        <!-- 导入导出组 -->
        <div class="action-group">
          <div class="group-label">导入/导出</div>
          <div class="group-buttons">
            <el-dropdown @command="handleImportCommand">
              <el-button type="primary">
                导入
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="load">根据ID加载简历</el-dropdown-item>
                  <el-dropdown-item command="import">导入模板JSON</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <el-dropdown @command="handleExportCommand">
              <el-button type="success">
                导出
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="pdf-settings">导出PDF</el-dropdown-item>
                  <el-dropdown-item command="template">导出模板JSON</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

      <!-- 用户登录/个人中心 -->
        <div v-if="!isUserLoggedIn" class="user-actions">
          <el-button @click="userLoginVisible = true" class="nav-button">
            <el-icon><User /></el-icon>
            登录
          </el-button>
        </div>
        <div v-else class="user-info">
          <el-dropdown @command="handleUserCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" :src="userInfo.avatar">
                {{ getAvatarText(userInfo) }}
              </el-avatar>
              <span class="username">{{ userInfo.nickname || userInfo.username || '未知用户' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="my-resumes">我的简历</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

    <!-- 对话框部分保持不变 -->
    <!-- 加载简历对话框 -->
    <el-dialog v-model="loadDialogVisible" title="加载简历" width="30%">
      <el-input v-model="resumeId" placeholder="请输入简历ID" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="loadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmLoad">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入模板对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入模板" width="30%">
      <el-upload
        class="upload-demo"
        drag
        action="/api/templates/import"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :before-upload="beforeImportUpload"
        accept=".json"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">只能上传 json 文件</div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- PDF导出设置对话框 -->
    <el-dialog v-model="pdfSettingsVisible" title="PDF导出设置" width="30%">
      <div class="pdf-settings">
        <div class="setting-item">
          <span class="setting-label">清晰度 (DPI)</span>
          <div class="setting-control">
            <el-slider
              v-model="pdfDpi"
              :min="0"
              :max="10"
              :step="1"
              show-input
              :marks="{
                0: '低',
                4: '默认',
                10: '高',
              }"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="pdfSettingsVisible = false">取消</el-button>
          <el-button type="primary" @click="handleExportPdfWithSettings">
            导出PDF
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户登录/注册弹窗 -->
    <UserLoginDialog 
      v-model="userLoginVisible" 
      @login-success="handleUserLoginSuccess"
    />

    <!-- 管理员登录弹窗 -->
    <el-dialog v-model="adminLoginVisible" title="管理员登录" width="400px">
      <el-form ref="adminFormRef" :model="adminForm" :rules="adminFormRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="adminForm.username"
            placeholder="请输入管理员用户名"
            @keyup.enter="handleAdminLogin"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="adminForm.password"
            type="password"
            placeholder="请输入管理员密码"
            show-password
            @keyup.enter="handleAdminLogin"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="adminLoginVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAdminLogin" :loading="adminLoginLoading">
            登录
          </el-button>
        </span>
      </template>
    </el-dialog>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useResumeStore } from "../../store/resume";
import { 
  UploadFilled, 
  ArrowDown, 
  CopyDocument,
  Document,
  Delete,
  Monitor,
  Grid,
  Key,
  User
} from "@element-plus/icons-vue";
import { exportPDF } from "../../utils/export";
import { useRouter } from "vue-router";
import UserLoginDialog from "../common/UserLoginDialog.vue";

const store = useResumeStore();
const router = useRouter();
const loadDialogVisible = ref(false);
const resumeId = ref("");
const importDialogVisible = ref(false);
const pdfSettingsVisible = ref(false);
const pdfDpi = ref(4); // 默认DPI值为4
const saveLoading = ref(false);
let saveTimeout: number | null = null;

// 用户登录相关状态
const userLoginVisible = ref(false);
const userInfo = ref(null);
const userToken = ref(null);

// 计算用户登录状态
const isUserLoggedIn = computed(() => {
  return userToken.value !== null && userInfo.value !== null;
});

// 管理员登录相关状态
const adminLoginVisible = ref(false);
const adminLoginLoading = ref(false);
const adminForm = ref({
  username: '',
  password: ''
});
const adminFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};
const adminFormRef = ref(null);

// 检查用户登录状态
const checkUserLoginStatus = () => {
  const token = localStorage.getItem('userToken');
  const user = localStorage.getItem('userInfo');
  
  if (token && user) {
    userToken.value = token;
    userInfo.value = JSON.parse(user);
  }
};

// 用户登录成功处理
const handleUserLoginSuccess = (user) => {
  userInfo.value = user;
  userToken.value = localStorage.getItem('userToken');
  ElMessage.success('登录成功');
};

// 用户操作命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile');
      break;
    case 'my-resumes':
      router.push('/user/resumes');
      break;
    case 'settings':
      router.push('/user/settings');
      break;
    case 'logout':
      handleUserLogout();
      break;
  }
};

// 用户登出
const handleUserLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('userToken');
    localStorage.removeItem('userInfo');
    userToken.value = null;
    userInfo.value = null;
    ElMessage.success('已退出登录');
  }).catch(() => {
    // 用户取消退出
  });
};

const handleSave = async () => {
  if (saveLoading.value) return;
  
  // 检查用户登录状态
  if (!isUserLoggedIn.value) {
    userLoginVisible.value = true;
    return;
  }
  
  saveLoading.value = true;
  try {
    const id = await store.saveResume();
    ElMessage.success(`保存成功，简历ID：${id}`);
  } catch (error) {
    if (error.message.includes('登录')) {
      userLoginVisible.value = true;
    } else {
      ElMessage.error("保存失败");
    }
  } finally {
    if (saveTimeout) clearTimeout(saveTimeout);
    saveTimeout = window.setTimeout(() => {
      saveLoading.value = false;
    }, 2000); // 2秒后可再次点击
  }
};

const handleLoad = () => {
  loadDialogVisible.value = true;
};

const confirmLoad = async () => {
  if (!resumeId.value) {
    ElMessage.warning("请输入简历ID");
    return;
  }
  try {
    await store.loadResume(Number(resumeId.value));
    loadDialogVisible.value = false;
    ElMessage.success("加载成功");
  } catch (error) {
    ElMessage.error("加载失败");
  }
};

const handleExport = async () => {
  try {
    // 确保所有组件数据都是最新的
    store.updateSelectedComponent();

    // 获取编辑区实际宽高（与ResumeCanvas一致）
    const editorDom = document.querySelector(".resume-canvas") as HTMLElement;
    let width = 595,
      height = 842;
    if (editorDom) {
      width = editorDom.offsetWidth;
      height = editorDom.offsetHeight;
    }

    // 使用当前设置的DPI值导出PDF
    const pdfUrl = await exportPDF(store.components, pdfDpi.value, {
      width,
      height,
    });

    // 下载PDF
    const a = document.createElement("a");
    a.href = pdfUrl;
    a.download = "resume.pdf";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(pdfUrl);

    ElMessage.success("导出成功");
  } catch (error) {
    console.error("导出失败:", error);
    ElMessage.error("导出失败");
  }
};

const handleExportTemplate = async () => {
  try {
    const response = await fetch("/api/templates/export", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(store.components),
    });

    if (!response.ok) {
      throw new Error("导出模板失败");
    }

    const templateData = await response.json();
    const blob = new Blob([JSON.stringify(templateData, null, 2)], {
      type: "application/json",
    });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "resume-template.json";
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);

    ElMessage.success("模板导出成功");
  } catch (error) {
    ElMessage.error("模板导出失败");
  }
};

const handleExportPdfWithSettings = async () => {
  pdfSettingsVisible.value = false;
  await handleExport();
};

const handlePreview = () => {
  // TODO: 实现预览功能
  ElMessage.info("预览功能开发中");
};

const handleImportTemplate = () => {
  importDialogVisible.value = true;
};

const handleImportSuccess = (response: any) => {
  try {
    if (!Array.isArray(response)) {
      throw new Error("无效的模板数据格式");
    }

    // 验证每个组件的数据结构
    const isValid = response.every(
      (component) =>
        component.id &&
        component.type &&
        typeof component.x === "number" &&
        typeof component.y === "number" &&
        typeof component.width === "number" &&
        typeof component.height === "number"
    );

    if (!isValid) {
      throw new Error("模板数据格式不正确");
    }

    store.setComponents(response);
    importDialogVisible.value = false;
    ElMessage.success("模板导入成功");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "模板导入失败");
  }
};

const handleImportError = (error: any) => {
  console.error("导入错误:", error);
  ElMessage.error("模板导入失败，请检查文件格式是否正确");
};

const beforeImportUpload = (file: File) => {
  const isJSON = file.type === "application/json";
  if (!isJSON) {
    ElMessage.error("只能上传 JSON 文件！");
  }
  return isJSON;
};

// 添加导入命令处理函数
const handleImportCommand = (command: string) => {
  switch (command) {
    case "load":
      handleLoad();
      break;
    case "import":
      handleImportTemplate();
      break;
  }
};

// 修改导出命令处理函数
const handleExportCommand = (command: string) => {
  switch (command) {
    case "pdf":
      handleExport();
      break;
    case "pdf-settings":
      pdfSettingsVisible.value = true;
      break;
    case "template":
      handleExportTemplate();
      break;
  }
};

// 添加清空功能
const handleClear = () => {
  ElMessageBox.confirm("确定要清空当前编辑区吗？此操作不可恢复。", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      store.setComponents([]);
      ElMessage.success("已清空编辑区");
    })
    .catch(() => {
      ElMessage.info("已取消清空操作");
    });
};

// 添加logo点击计数器
const logoClickCount = ref(0);
const logoClickTimer = ref<number | null>(null);

const handleLogoClick = () => {
  logoClickCount.value++;
  
  // 清除之前的定时器
  if (logoClickTimer.value) {
    clearTimeout(logoClickTimer.value);
  }
  
  // 设置新的定时器，2秒后重置点击次数
  logoClickTimer.value = window.setTimeout(() => {
    logoClickCount.value = 0;
  }, 2000);
  
  // 检查是否达到三次点击
  if (logoClickCount.value === 3) {
    logoClickCount.value = 0; // 重置点击次数
    if (logoClickTimer.value) {
      clearTimeout(logoClickTimer.value);
    }
    handleAdminClick(); // 显示管理员登录弹窗
    return;
  }
  
  router.push("/editor");
};

const copyResumeId = () => {
  if (store.currentResumeId) {
    // 先判断 clipboard API 是否可用
    if (
      navigator.clipboard &&
      typeof navigator.clipboard.writeText === "function"
    ) {
      navigator.clipboard
        .writeText(String(store.currentResumeId))
        .then(() => {
          ElMessage.success("简历ID已复制");
        })
        .catch(() => {
          ElMessage.error("复制失败，请手动复制");
        });
    } else {
      // 降级方案：用 input + execCommand
      const input = document.createElement("input");
      input.value = String(store.currentResumeId);
      document.body.appendChild(input);
      input.select();
      try {
        document.execCommand("copy");
        ElMessage.success("简历ID已复制");
      } catch (e) {
        ElMessage.error("复制失败，请手动复制");
      }
      document.body.removeChild(input);
    }
  }
};

const handleResumeSquareClick = () => {
  router.push("/resume-square");
};

const handleAdminClick = () => {
  adminLoginVisible.value = true;
  // 重置表单
  adminForm.value = {
    username: '',
    password: ''
  };
  if (adminFormRef.value) {
    adminFormRef.value.resetFields();
  }
};

const handleAdminLogin = async () => {
  if (!adminFormRef.value) return;
  
  try {
    await adminFormRef.value.validate();
  } catch (error) {
    return;
  }
  
  adminLoginLoading.value = true;
  
  try {
    const response = await fetch('/api/admin/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: adminForm.value.username,
        password: adminForm.value.password
      })
    });
    
    const result = await response.json();
    
    if (result.success) {
      ElMessage.success('登录成功');
      adminLoginVisible.value = false;
      
      // 存储管理员信息到localStorage
      localStorage.setItem('adminToken', result.token);
      localStorage.setItem('adminUser', JSON.stringify(result.user));
      
      // 跳转到审批管理页面
      router.push('/approval-management');
    } else {
      throw new Error(result.message || '登录失败');
    }
  } catch (error) {
    console.error('管理员登录失败:', error);
    ElMessage.error('登录失败：' + error.message);
  } finally {
    adminLoginLoading.value = false;
  }
};

// 组件挂载时检查用户登录状态
onMounted(() => {
  checkUserLoginStatus();
});

// 在script setup部分添加getAvatarText函数
const getAvatarText = (user: any) => {
  if (user?.avatar) {
    return '' // 如果有头像，则不显示文本
  }
  if (user?.nickname) {
    return user.nickname.charAt(0)
  }
  if (user?.username) {
    return user.username.charAt(0)
  }
  return 'U' // 默认显示
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(76, 175, 80, 0.08);
  border-bottom: 1px solid #e8f5e9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px; /* 增加间距 */
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 导航菜单样式 */
.nav-menu {
  display: flex;
  gap: 12px; /* 减小按钮间距 */
  padding: 0 8px;
  border-left: 1px solid #e8f5e9; /* 添加分隔线 */
  height: 40px;
  align-items: center;
}

.nav-button {
  font-size: 14px; /* 稍微减小字体 */
  padding: 8px 16px; /* 调整内边距 */
  transition: all 0.3s ease;
  border-radius: 6px;
}

.nav-button:hover {
  transform: translateY(-2px); /* 悬停时上移效果 */
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

/* 操作工具栏样式 */
.action-toolbar {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.action-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
  position: relative;
}

.action-group:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -8px;
  top: 15%;
  height: 70%;
  width: 1px;
  background-color: #e8f5e9;
}

.group-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.group-buttons {
  display: flex;
  gap: 8px;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #2e7d32;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 15px;
  border-radius: 6px;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-image {
  height: 40px;
  width: 40px;
  object-fit: cover;
  border-radius: 6px;
}

.logo:hover {
  transform: scale(1.05);
  color: #4caf50;
  background-color: #f1f8e9;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.15);
}

.logo-text {
  white-space: nowrap;
  font-weight: 700;
  background: linear-gradient(45deg, #2e7d32, #4caf50);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 1px 2px rgba(46, 125, 50, 0.1);
}

.resume-id-area {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  background-color: #f1f8e9;
  border-radius: 6px;
  border: 1px solid #e8f5e9;
  transition: all 0.3s ease;
}

.resume-id-area:hover {
  background-color: #e8f5e9;
  box-shadow: 0 2px 6px rgba(76, 175, 80, 0.1);
}

.copy-icon {
  cursor: pointer;
  margin-left: 4px;
  color: #4caf50;
  transition: all 0.2s ease;
}

.copy-icon:hover {
  color: #2e7d32;
  transform: scale(1.2);
}

:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background-color: #4caf50;
  border-color: #4caf50;
}

:deep(.el-button--primary:hover) {
  background-color: #2e7d32;
  border-color: #2e7d32;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

:deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #e74c3c;
  color: #e74c3c;
}

:deep(.el-button--danger:hover) {
  background-color: #e74c3c;
  border-color: #e74c3c;
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(231, 76, 60, 0.2);
}

:deep(.el-button--success) {
  background-color: #ffffff;
  border-color: #4caf50;
  color: #4caf50;
}

:deep(.el-button--success:hover) {
  background-color: #4caf50;
  border-color: #4caf50;
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

:deep(.el-button:not(.el-button--primary):not(.el-button--danger):not(.el-button--success)) {
  background-color: #ffffff;
  border-color: #e8f5e9;
  color: #4caf50;
}

:deep(.el-button:not(.el-button--primary):not(.el-button--danger):not(.el-button--success):hover) {
  color: #2e7d32;
  border-color: #4caf50;
  background-color: #f1f8e9;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.15);
}

:deep(.el-dropdown-menu) {
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(76, 175, 80, 0.1);
  border: 1px solid #e8f5e9;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #f1f8e9;
  color: #4caf50;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.upload-demo {
  width: 100%;
}

:deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(76, 175, 80, 0.1);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e8f5e9;
  padding: 15px 20px;
}

:deep(.el-dialog__title) {
  color: #2e7d32;
  font-weight: 600;
}

:deep(.el-upload-dragger) {
  border: 2px dashed #e8f5e9;
  background-color: #ffffff;
  transition: all 0.3s ease;
}

:deep(.el-upload-dragger:hover) {
  border-color: #4caf50;
  background-color: #f1f8e9;
}

:deep(.el-upload__text) {
  color: #4caf50;
}

:deep(.el-upload__tip) {
  color: #2e7d32;
}

.pdf-settings {
  padding: 20px;
}

.setting-item {
  margin-bottom: 20px;
}

.setting-label {
  display: block;
  margin-bottom: 10px;
  font-weight: bold;
}

.setting-control {
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .action-toolbar {
    flex-direction: column;
    gap: 12px;
  }
  
  .action-group:not(:last-child)::after {
    display: none;
  }
  
  .action-group {
    width: 100%;
    border-bottom: 1px solid #e8f5e9;
    padding-bottom: 8px;
  }
  
  .group-buttons {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    height: auto;
    padding: 10px;
    gap: 15px;
  }
  
  .header-left, .header-right {
    width: 100%;
  }
  
  .header-left {
    flex-direction: column;
    gap: 12px;
  }
  
  .nav-menu {
    width: 100%;
    justify-content: space-between;
    border-left: none;
    border-top: 1px solid #e8f5e9;
    padding-top: 12px;
  }
  
  .resume-id-area {
    width: 100%;
    justify-content: center;
  }
  
  .action-toolbar {
    width: 100%;
  }
  
  .group-buttons {
    flex-direction: row;
    width: 100%;
  }
  
  .group-buttons .el-button,
  .group-buttons .el-dropdown {
    flex: 1;
  }
}
</style>