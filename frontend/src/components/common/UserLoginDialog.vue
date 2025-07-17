<template>
  <el-dialog 
    v-model="dialogVisible" 
    :title="isLogin ? '用户登录' : '用户注册'" 
    width="400px"
    @close="resetForm"
  >
    <!-- 登录表单 -->
    <el-form 
      v-if="isLogin"
      ref="loginFormRef" 
      :model="loginForm" 
      :rules="loginRules" 
      label-width="80px"
    >
      <el-form-item label="用户名" prop="usernameOrEmail">
        <el-input
          v-model="loginForm.usernameOrEmail"
          placeholder="请输入用户名或邮箱"
          @keyup.enter="handleLogin"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          show-password
          @keyup.enter="handleLogin"
        />
      </el-form-item>
    </el-form>

    <!-- 注册表单 -->
    <el-form 
      v-else
      ref="registerFormRef" 
      :model="registerForm" 
      :rules="registerRules" 
      label-width="80px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="registerForm.username"
          placeholder="请输入用户名"
          @blur="checkUsername"
        />
        <span v-if="usernameCheckMessage" :class="usernameCheckClass">
          {{ usernameCheckMessage }}
        </span>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="registerForm.email"
          placeholder="请输入邮箱"
          @blur="checkEmail"
        />
        <span v-if="emailCheckMessage" :class="emailCheckClass">
          {{ emailCheckMessage }}
        </span>
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="registerForm.nickname"
          placeholder="请输入昵称（可选）"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="请输入密码（至少6位）"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="请确认密码"
          show-password
          @keyup.enter="handleRegister"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <div class="switch-mode">
          <el-button type="text" @click="switchMode">
            {{ isLogin ? '还没有账号？立即注册' : '已有账号？立即登录' }}
          </el-button>
        </div>
        <div class="action-buttons">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="isLogin ? handleLogin() : handleRegister()" 
            :loading="loading"
          >
            {{ isLogin ? '登录' : '注册' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'

// Props和Emits定义
const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'login-success': [user: any]
}>()

// 响应式数据
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isLogin = ref(true)
const loading = ref(false)

// 登录表单
const loginForm = reactive({
  usernameOrEmail: '',
  password: ''
})

const loginRules = {
  usernameOrEmail: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 表单引用
const loginFormRef = ref()
const registerFormRef = ref()

// 用户名和邮箱检查
const usernameCheckMessage = ref('')
const usernameCheckClass = ref('')
const emailCheckMessage = ref('')
const emailCheckClass = ref('')

// 检查用户名可用性
const checkUsername = async () => {
  if (!registerForm.username) return
  
  try {
    const response = await fetch(`/api/user/check-username?username=${registerForm.username}`)
    const result = await response.json()
    
    if (result.success) {
      if (result.isAvailable) {
        usernameCheckMessage.value = '用户名可用'
        usernameCheckClass.value = 'check-success'
      } else {
        usernameCheckMessage.value = '用户名已存在'
        usernameCheckClass.value = 'check-error'
      }
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 检查邮箱可用性
const checkEmail = async () => {
  if (!registerForm.email) return
  
  try {
    const response = await fetch(`/api/user/check-email?email=${registerForm.email}`)
    const result = await response.json()
    
    if (result.success) {
      if (result.isAvailable) {
        emailCheckMessage.value = '邮箱可用'
        emailCheckClass.value = 'check-success'
      } else {
        emailCheckMessage.value = '邮箱已存在'
        emailCheckClass.value = 'check-error'
      }
    }
  } catch (error) {
    console.error('检查邮箱失败:', error)
  }
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
  } catch (error) {
    return
  }
  
  loading.value = true
  
  try {
    const response = await fetch('/api/user/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(loginForm)
    })
    
    const result = await response.json()
    
    if (result.success) {
      ElMessage.success('登录成功')
      
      // 存储用户信息
      localStorage.setItem('userToken', result.token)
      localStorage.setItem('userInfo', JSON.stringify(result.user))
      
      // 发送登录成功事件
      emit('login-success', result.user)
      
      // 关闭对话框
      dialogVisible.value = false
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
  } catch (error) {
    return
  }
  
  loading.value = true
  
  try {
    const response = await fetch('/api/user/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(registerForm)
    })
    
    const result = await response.json()
    
    if (result.success) {
      ElMessage.success('注册成功')
      
      // 存储用户信息
      localStorage.setItem('userToken', result.token)
      localStorage.setItem('userInfo', JSON.stringify(result.user))
      
      // 发送登录成功事件
      emit('login-success', result.user)
      
      // 关闭对话框
      dialogVisible.value = false
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 切换模式
const switchMode = () => {
  isLogin.value = !isLogin.value
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.assign(loginForm, {
    usernameOrEmail: '',
    password: ''
  })
  
  Object.assign(registerForm, {
    username: '',
    email: '',
    nickname: '',
    password: '',
    confirmPassword: ''
  })
  
  // 清空检查消息
  usernameCheckMessage.value = ''
  emailCheckMessage.value = ''
  
  // 清空表单验证
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-mode {
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.check-success {
  color: #67c23a;
  font-size: 12px;
  margin-left: 5px;
}

.check-error {
  color: #f56c6c;
  font-size: 12px;
  margin-left: 5px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-form-item__label {
  font-weight: 500;
}

.el-input {
  width: 100%;
}

.el-button--text {
  padding: 0;
  font-size: 13px;
}

.el-button--text:hover {
  color: #409eff;
}
</style> 