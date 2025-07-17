<template>
  <div class="user-settings-view">
    <div class="page-header">
      <h1>账户设置</h1>
      <p>管理您的账户安全和偏好</p>
    </div>

    <div class="page-content">
      <div class="settings-menu">
        <el-menu 
          :default-active="activeTab" 
          mode="vertical"
          @select="handleMenuSelect"
        >
          <el-menu-item index="security">
            <el-icon><Lock /></el-icon>
            <span>账户安全</span>
          </el-menu-item>
          <el-menu-item index="preferences">
            <el-icon><Setting /></el-icon>
            <span>偏好设置</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="settings-content">
        <!-- 账户安全设置 -->
        <div v-if="activeTab === 'security'" class="settings-panel">
          <h2>账户安全</h2>
          
          <div class="settings-section">
            <h3>修改密码</h3>
            <el-form 
              :model="passwordForm" 
              :rules="passwordRules" 
              ref="passwordFormRef"
              label-width="120px"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input 
                  v-model="passwordForm.oldPassword" 
                  type="password" 
                  placeholder="请输入当前密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  placeholder="请输入新密码（至少6位）"
                  show-password
                />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="changePassword" 
                  :loading="passwordLoading"
                >
                  修改密码
                </el-button>
                <el-button @click="resetPasswordForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="settings-section">
            <h3>危险操作</h3>
            <p class="danger-warning">
              <el-icon><Warning /></el-icon>
              以下操作将永久删除您的账户，请谨慎操作！
            </p>
            <el-button 
              type="danger" 
              @click="handleDeleteAccount"
              :loading="deleteLoading"
            >
              删除账户
            </el-button>
          </div>
        </div>

        <!-- 偏好设置 -->
        <div v-if="activeTab === 'preferences'" class="settings-panel">
          <h2>偏好设置</h2>
          
          <div class="settings-section">
            <h3>界面设置</h3>
            <el-form label-width="120px">
              <el-form-item label="主题">
                <el-radio-group v-model="preferences.theme">
                  <el-radio label="light">浅色主题</el-radio>
                  <el-radio label="dark">深色主题</el-radio>
                  <el-radio label="auto">跟随系统</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="语言">
                <el-select v-model="preferences.language">
                  <el-option label="简体中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <el-divider />

          <div class="settings-section">
            <h3>通知设置</h3>
            <el-form label-width="120px">
              <el-form-item label="邮件通知">
                <el-switch 
                  v-model="preferences.emailNotification"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              <el-form-item label="系统消息">
                <el-switch 
                  v-model="preferences.systemNotification"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
            </el-form>
          </div>

          <div class="settings-actions">
            <el-button type="primary" @click="savePreferences" :loading="preferencesLoading">
              保存设置
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, Setting, Warning } from '@element-plus/icons-vue'
import { useSettingsStore } from '../store/settings'

const router = useRouter()
const settingsStore = useSettingsStore()
const activeTab = ref('security')
const passwordLoading = ref(false)
const deleteLoading = ref(false)
const preferencesLoading = ref(false)
const passwordFormRef = ref()

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

const preferences = reactive({
  theme: settingsStore.theme,
  language: settingsStore.language,
  emailNotification: true,
  systemNotification: true
})

// 菜单选择
const handleMenuSelect = (key: string) => {
  activeTab.value = key
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
  } catch (error) {
    return
  }
  
  passwordLoading.value = true
  
  try {
    const token = localStorage.getItem('userToken')
    if (!token) {
      ElMessage.error('请先登录')
      return
    }

    const response = await fetch('/api/user/password', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token
      },
      body: JSON.stringify({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
    })

    const result = await response.json()
    if (result.success) {
      ElMessage.success('密码修改成功')
      resetPasswordForm()
    } else {
      ElMessage.error(result.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败')
  } finally {
    passwordLoading.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

// 删除账户
const handleDeleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除您的账户吗？这将永久删除您的所有数据，且不可恢复！',
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    // 二次确认
    await ElMessageBox.confirm(
      '请再次确认！删除账户后，您的所有简历和个人数据将永久丢失！',
      '最终确认',
      {
        confirmButtonText: '我已了解，删除账户',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    deleteLoading.value = true
    
    // 这里应该调用删除账户的API
    ElMessage.success('账户删除功能暂未开放')
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除账户失败:', error)
      ElMessage.error('删除账户失败')
    }
  } finally {
    deleteLoading.value = false
  }
}

// 保存偏好设置
const savePreferences = async () => {
  preferencesLoading.value = true
  
  try {
    const token = localStorage.getItem('userToken')
    if (!token) {
      ElMessage.error('请先登录')
      return
    }

    // 更新主题和语言设置
    settingsStore.setTheme(preferences.theme)
    settingsStore.setLanguage(preferences.language)

    // 保存到后端
    const response = await fetch('/api/user/preferences', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token
      },
      body: JSON.stringify(preferences)
    })

    const result = await response.json()
    if (result.success) {
      ElMessage.success('设置保存成功')
    } else {
      ElMessage.error(result.message || '设置保存失败')
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    preferencesLoading.value = false
  }
}

// 加载用户偏好设置
const loadPreferences = async () => {
  try {
    const token = localStorage.getItem('userToken')
    if (!token) {
      ElMessage.error('请先登录')
      return
    }

    const response = await fetch('/api/user/preferences', {
      headers: {
        'Authorization': token
      }
    })

    const result = await response.json()
    if (result.success) {
      // 更新本地设置
      preferences.theme = result.preferences.theme || settingsStore.theme
      preferences.language = result.preferences.language || settingsStore.language
      preferences.emailNotification = result.preferences.emailNotification
      preferences.systemNotification = result.preferences.systemNotification
      
      // 应用主题和语言设置
      settingsStore.setTheme(preferences.theme)
      settingsStore.setLanguage(preferences.language)
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

onMounted(() => {
  loadPreferences()
})
</script>

<style scoped>
.user-settings-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  background: white;
  padding: 24px;
  border-bottom: 1px solid #e8e8e8;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.page-content {
  display: flex;
  gap: 24px;
  padding: 24px;
}

.settings-menu {
  width: 200px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.settings-menu .el-menu {
  border-right: none;
}

.settings-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.settings-panel h2 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.settings-section {
  margin-bottom: 32px;
}

.settings-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.settings-section .el-form {
  max-width: 500px;
}

.danger-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
  font-size: 14px;
  margin: 16px 0;
  padding: 12px;
  background: #fef0f0;
  border-radius: 4px;
}

.settings-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
}

@media (max-width: 768px) {
  .page-content {
    flex-direction: column;
    padding: 16px;
  }
  
  .settings-menu {
    width: 100%;
  }
  
  .settings-menu .el-menu {
    mode: horizontal;
  }
}
</style> 