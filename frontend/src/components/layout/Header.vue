<template>
  <header class="header">
    <!-- 左侧区域：Logo和导航按钮 -->
    <div class="header-left">
      <div class="logo" @click="handleLogoClick" title="点击回到首页">
        <img src="/logo-large.jpg" alt="Resume King Logo" class="logo-image" />
        <span class="logo-text">Resume_King 简历王</span>
      </div>
      
      <!-- 导航按钮组 -->
      <div class="nav-buttons">
        <el-button
          type="info"
          @click="handleResumeSquareClick"
          class="nav-button"
          icon="Grid"
        >简历广场</el-button>
        
        <el-button
          type="warning"
          @click="handleServerMonitorClick"
          class="nav-button"
          icon="Monitor"
        >服务器监控</el-button>
        
        <el-button
          type="danger"
          @click="handleAdminClick"
          class="nav-button"
          icon="Key"
        >管理员入口</el-button>
      </div>
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
      <div class="action-buttons">
        <!-- 文件操作组 -->
        <div class="button-group file-actions">
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
        
        <!-- 导入导出组 -->
        <div class="button-group io-actions">
          <el-dropdown @command="handleImportCommand">
            <el-button type="primary">
              导入简历
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
              导出简历
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
import { ref } from "vue";
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
  Key
} from "@element-plus/icons-vue";
import { exportPDF } from "../../utils/export";
import { useRouter } from "vue-router";

const store = useResumeStore();
const loadDialogVisible = ref(false);
const resumeId = ref("");
const importDialogVisible = ref(false);
const pdfSettingsVisible = ref(false);
const pdfDpi = ref(4); // 默认DPI值为4
const saveLoading = ref(false);
let saveTimeout: number | null = null;

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

const handleSave = async () => {
  if (saveLoading.value) return;
  saveLoading.value = true;
  try {
    const id = await store.saveResume();
    ElMessage.success(`保存成功，简历ID：${id}`);
  } catch (error) {
    ElMessage.error("保存失败");
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

const handleLogoClick = () => {
  router.push("/");
};

const router = useRouter();

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

const handleServerMonitorClick = () => {
  router.push("/server-monitor");
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
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between; /* Distribute space between left and right */
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
  gap: 20px; /* Space between logo and nav buttons */
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px; /* Space between right items */
}

.nav-buttons {
  display: flex;
  gap: 10px; /* Space between nav buttons */
}

.nav-button {
  font-size: 16px; /* Larger font size */
  padding: 10px 20px; /* Larger padding */
  transform: scale(1.1); /* Slightly larger button */
  transition: all 0.3s ease;
  border-radius: 8px;
}

.nav-button:hover {
  transform: scale(1.15); /* Even larger on hover */
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2); /* More prominent shadow */
}

.action-buttons {
  display: flex;
  gap: 16px; /* Space between button groups */
}

.button-group {
  display: flex;
  gap: 10px; /* Space between buttons within a group */
  position: relative;
}

/* Add subtle divider between button groups */
.button-group.file-actions::after {
  content: '';
  position: absolute;
  right: -8px;
  top: 15%;
  height: 70%;
  width: 1px;
  background-color: #e8f5e9;
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
  color: #4caf50; /* Match primary color */
  transition: all 0.2s ease;
}

.copy-icon:hover {
  color: #2e7d32; /* Darker on hover */
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

:deep(
    .el-button:not(.el-button--primary):not(.el-button--danger):not(
        .el-button--success
      )
  ) {
  background-color: #ffffff;
  border-color: #e8f5e9;
  color: #4caf50;
}

:deep(
    .el-button:not(.el-button--primary):not(.el-button--danger):not(
        .el-button--success
      ):hover
  ) {
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
  .action-buttons {
    flex-direction: column;
    gap: 8px;
  }
  
  .button-group.file-actions::after {
    display: none;
  }
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    height: auto;
    padding: 10px;
    gap: 10px;
  }
  
  .header-left, .header-right {
    width: 100%;
    justify-content: center;
  }
  
  .nav-buttons {
    margin-top: 10px;
  }
}
</style>