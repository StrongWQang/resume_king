<template>
  <div class="approval-management">
    <div class="management-header">
      <h1 class="page-title">审批管理</h1>
      <div class="header-actions">
        <el-button @click="handleServerMonitor" type="warning" icon="Monitor">服务器监控</el-button>
        <el-button @click="refreshData" icon="Refresh">刷新</el-button>
        <el-button @click="handleLogout" type="danger" icon="SwitchButton">退出登录</el-button>
      </div>
    </div>

    <!-- 统计面板 -->
    <div class="statistics-panel">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card pending">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.pending || 0 }}</div>
              <div class="stat-label">待审批</div>
            </div>
            <el-icon class="stat-icon"><Clock /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card approved">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.approved || 0 }}</div>
              <div class="stat-label">已通过</div>
            </div>
            <el-icon class="stat-icon"><Check /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card rejected">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.rejected || 0 }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
            <el-icon class="stat-icon"><Close /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card cancelled">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.cancelled || 0 }}</div>
              <div class="stat-label">已取消</div>
            </div>
            <el-icon class="stat-icon"><Remove /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <el-row :gutter="20" align="middle">
        <el-col :span="6">
          <el-select v-model="filterStatus" placeholder="筛选状态" clearable @change="handleFilterChange">
            <el-option label="全部" value="" />
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button-group>
            <el-button 
              type="primary" 
              @click="approveSelected" 
              :disabled="selectedIds.length === 0"
              icon="Check"
            >
              批量通过
            </el-button>
            <el-button 
              type="danger" 
              @click="rejectSelected" 
              :disabled="selectedIds.length === 0"
              icon="Close"
            >
              批量拒绝
            </el-button>
          </el-button-group>
        </el-col>
      </el-row>
    </div>

    <!-- 申请列表 -->
    <div class="request-list">
      <el-table
        ref="tableRef"
        :data="requests"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="resumeId" label="简历ID" width="120">
          <template #default="scope">
            {{ String(scope.row.resumeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="模板标题" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="approverName" label="审批人" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button 
                size="small" 
                @click="viewRequest(scope.row)"
                icon="View"
              >
                查看
              </el-button>
              <el-button 
                v-if="scope.row.status === 'PENDING'"
                size="small" 
                type="success" 
                @click="approveRequest(scope.row)"
                icon="Check"
              >
                通过
              </el-button>
              <el-button 
                v-if="scope.row.status === 'PENDING'"
                size="small" 
                type="danger" 
                @click="rejectRequest(scope.row)"
                icon="Close"
              >
                拒绝
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="申请详情" width="80%">
      <div v-if="selectedRequest" class="request-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ selectedRequest.id }}</el-descriptions-item>
          <el-descriptions-item label="简历ID">{{ String(selectedRequest.resumeId) }}</el-descriptions-item>
          <el-descriptions-item label="模板标题">{{ selectedRequest.title }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedRequest.status)">
              {{ getStatusText(selectedRequest.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(selectedRequest.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">{{ formatTime(selectedRequest.approveTime) }}</el-descriptions-item>
          <el-descriptions-item label="审批人">{{ selectedRequest.approverName || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="拒绝理由">{{ selectedRequest.rejectReason || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="模板描述" :span="2">{{ selectedRequest.description }}</el-descriptions-item>
        </el-descriptions>

        <!-- 简历预览 -->
        <div class="resume-preview-section">
          <h3>简历预览</h3>
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
                :style="getComponentStyle(component, 0.2)"
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
                      margin: (component.padding * 0.3) + 'px 0'
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

        <!-- 审批历史 -->
        <div class="approval-history-section">
          <h3>审批历史</h3>
          <el-timeline>
            <el-timeline-item
              v-for="record in approvalHistory"
              :key="record.id"
              :timestamp="formatTime(record.createTime)"
              :type="getTimelineType(record.operation)"
            >
              <h4>{{ record.operationDisplayName || getOperationText(record.operation) }}</h4>
              <p>审批人：{{ record.approverName || '系统' }}</p>
              <p v-if="record.comment">备注：{{ record.comment }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="selectedRequest && selectedRequest.status === 'PENDING'"
            type="success" 
            @click="approveRequest(selectedRequest)"
            icon="Check"
          >
            通过申请
          </el-button>
          <el-button 
            v-if="selectedRequest && selectedRequest.status === 'PENDING'"
            type="danger" 
            @click="rejectRequest(selectedRequest)"
            icon="Close"
          >
            拒绝申请
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审批操作弹窗 -->
    <el-dialog v-model="approvalDialogVisible" :title="approvalDialogTitle" width="500px">
      <el-form :model="approvalForm" :rules="approvalFormRules" ref="approvalFormRef" label-width="80px">
        <el-form-item v-if="approvalAction === 'reject'" label="拒绝理由" prop="reason">
          <el-input
            v-model="approvalForm.reason"
            type="textarea"
            placeholder="请输入拒绝理由"
            rows="4"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-else label="审批备注" prop="comment">
          <el-input
            v-model="approvalForm.comment"
            type="textarea"
            placeholder="请输入审批备注（可选）"
            rows="4"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approvalDialogVisible = false">取消</el-button>
          <el-button 
            :type="approvalAction === 'approve' ? 'success' : 'danger'" 
            @click="confirmApproval"
            :loading="approvalLoading"
          >
            确认{{ approvalAction === 'approve' ? '通过' : '拒绝' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Refresh, 
  SwitchButton, 
  Clock, 
  Check, 
  Close, 
  Remove, 
  View,
  Loading,
  Warning,
  Monitor
} from '@element-plus/icons-vue';
import axios from 'axios';

const router = useRouter();

// 管理员信息
const adminUser = ref(JSON.parse(localStorage.getItem('adminUser') || '{}'));

// 数据状态
const loading = ref(false);
const statistics = ref({});
const requests = ref([]);
const selectedIds = ref([]);
const filterStatus = ref('');

// 分页
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);

// 详情弹窗
const detailDialogVisible = ref(false);
const selectedRequest = ref(null);
const previewLoading = ref(false);
const previewError = ref('');
const previewComponents = ref([]);
const approvalHistory = ref([]);

// 审批弹窗
const approvalDialogVisible = ref(false);
const approvalDialogTitle = ref('');
const approvalAction = ref(''); // 'approve' or 'reject'
const approvalForm = ref({
  reason: '',
  comment: ''
});
const approvalFormRules = {
  reason: [
    { required: true, message: '请输入拒绝理由', trigger: 'blur' },
    { min: 5, max: 200, message: '理由长度应在 5 到 200 个字符', trigger: 'blur' }
  ]
};
const approvalFormRef = ref(null);
const approvalLoading = ref(false);
const pendingApprovalRequest = ref(null);

// 获取统计数据
const fetchStatistics = async () => {
  try {
    console.log('开始获取统计数据');
    
    const response = await axios.get('/api/resume-publish/statistics', {
      transformResponse: [(data) => {
        // 记录原始响应数据
        console.log('统计数据原始响应:', data);
        
        // 使用正则表达式处理大数字
        const processedData = data.replace(/:\s*(\d{15,})/g, ':"$1"');
        const jsonData = JSON.parse(processedData);
        console.log('处理后的统计数据:', jsonData);
        
        return jsonData;
      }]
    });
    
    // 将API返回的统计数据映射到前端所需的格式
    const apiData = response.data;
    statistics.value = {
      pending: apiData.PENDING || 0,
      approved: apiData.APPROVED || 0,
      rejected: apiData.REJECTED || 0,
      cancelled: apiData.CANCELLED || 0
    };
    
    console.log('映射后的统计数据:', statistics.value);
  } catch (error) {
    console.error('获取统计数据失败:', error);
    console.error('错误详情:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    });
  }
};

// 获取申请列表
const fetchRequests = async () => {
  loading.value = true;
  try {
    console.log('开始获取申请列表, 页码:', currentPage.value, '每页数量:', pageSize.value);
    
    const response = await axios.get('/api/resume-publish/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        status: filterStatus.value || undefined
      },
      transformResponse: [(data) => {
        // 在解析响应数据之前，先记录原始数据
        console.log('API原始响应数据:', data);
        
        // 使用正则表达式将大数字转换为字符串，避免精度丢失
        // 匹配18位或以上的数字，并将其用引号包围
        const processedData = data.replace(/:\s*(\d{15,})/g, ':"$1"');
        console.log('预处理后的数据:', processedData);
        
        // 现在安全地解析JSON
        const jsonData = JSON.parse(processedData);
        
        console.log('处理后的响应数据:', jsonData);
        return jsonData;
      }]
    });
    
    requests.value = response.data.requests || [];
    total.value = response.data.total || 0;
    
    // 记录处理后的请求数据
    console.log('最终的请求数据:', requests.value);
    
    // 验证ID是否正确
    requests.value.forEach(request => {
      console.log('请求ID:', request.id, '类型:', typeof request.id);
      console.log('简历ID:', request.resumeId, '类型:', typeof request.resumeId);
    });
    
  } catch (error) {
    console.error('获取申请列表失败:', error);
    console.error('错误详情:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    });
    ElMessage.error('获取申请列表失败');
  } finally {
    loading.value = false;
  }
};

// 刷新数据
const refreshData = async () => {
  await Promise.all([fetchStatistics(), fetchRequests()]);
  ElMessage.success('数据刷新成功');
};

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminUser');
    router.push('/');
    ElMessage.success('已退出登录');
  });
};

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1;
  fetchRequests();
};

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id);
};

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchRequests();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchRequests();
};

// 状态相关方法
const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  };
  return types[status] || 'info';
};

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  };
  return texts[status] || '未知';
};

const getOperationText = (operation) => {
  const texts = {
    'SUBMIT': '提交申请',
    'APPROVE': '审批通过',
    'REJECT': '审批拒绝',
    'CANCEL': '取消申请'
  };
  return texts[operation] || '未知操作';
};

const getTimelineType = (operation) => {
  const types = {
    'SUBMIT': 'primary',
    'APPROVE': 'success',
    'REJECT': 'danger',
    'CANCEL': 'warning'
  };
  return types[operation] || 'primary';
};

// 时间格式化
const formatTime = (time) => {
  if (!time) return '暂无';
  return new Date(time).toLocaleString('zh-CN');
};

// 查看申请详情
const viewRequest = async (request) => {
  console.log('查看申请详情:', {
    requestId: request.id,
    requestIdType: typeof request.id,
    resumeId: request.resumeId,
    resumeIdType: typeof request.resumeId
  });
  
  selectedRequest.value = request;
  detailDialogVisible.value = true;
  
  // 加载简历预览
  await loadResumePreview(request.resumeId);
  
  // 加载审批历史
  await loadApprovalHistory(request.id);
};

// 加载简历预览
const loadResumePreview = async (resumeId) => {
  console.log('开始加载简历预览:', {
    resumeId: resumeId,
    resumeIdType: typeof resumeId
  });
  
  previewLoading.value = true;
  previewError.value = '';
  previewComponents.value = [];
  
  try {
    // 确保使用字符串形式的ID
    const resumeIdString = resumeId.toString();
    console.log('发送请求的简历ID:', resumeIdString);
    
    const response = await axios.get(`/api/resumes/${resumeIdString}`);
    console.log('简历预览响应数据:', response.data);
    
    previewComponents.value = processComponents(response.data || []);
  } catch (error) {
    console.error('加载简历预览失败:', {
      error: error,
      message: error.message,
      response: error.response?.data
    });
    previewError.value = '加载简历数据失败：' + (error.response?.data?.message || error.message);
  } finally {
    previewLoading.value = false;
  }
};

// 加载审批历史
const loadApprovalHistory = async (requestId) => {
  console.log('开始加载审批历史:', {
    requestId: requestId,
    requestIdType: typeof requestId
  });
  
  try {
    const requestIdString = requestId.toString();
    console.log('发送请求的审批ID:', requestIdString);
    
    const response = await axios.get(`/api/approval/${requestIdString}/history`);
    console.log('审批历史响应数据:', response.data);
    
    approvalHistory.value = response.data || [];
  } catch (error) {
    console.error('获取审批历史失败:', {
      error: error,
      message: error.message,
      response: error.response?.data
    });
  }
};

// 处理简历组件数据
const processComponents = (components) => {
  return components.map(component => ({
    ...component,
    x: component.x || 0,
    y: component.y || 0,
    width: component.width || 100,
    height: component.height || 20,
    fontSize: component.fontSize || 12,
    color: component.color || '#000000',
    textAlign: component.textAlign || 'left',
  }));
};

// 获取组件样式
const getComponentStyle = (component, scaleFactor) => {
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

// 图片加载错误处理
const handleImageError = (e) => {
  const img = e.target;
  img.style.border = '1px solid #ff4d4f';
  img.style.backgroundColor = '#fff2f0';
};

// 单个审批
const approveRequest = (request) => {
  pendingApprovalRequest.value = request;
  approvalAction.value = 'approve';
  approvalDialogTitle.value = '审批通过';
  approvalForm.value = { reason: '', comment: '' };
  approvalDialogVisible.value = true;
};

const rejectRequest = (request) => {
  pendingApprovalRequest.value = request;
  approvalAction.value = 'reject';
  approvalDialogTitle.value = '审批拒绝';
  approvalForm.value = { reason: '', comment: '' };
  approvalDialogVisible.value = true;
};

// 批量审批
const approveSelected = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要审批的申请');
    return;
  }
  
  pendingApprovalRequest.value = { ids: selectedIds.value };
  approvalAction.value = 'approve';
  approvalDialogTitle.value = `批量审批通过 (${selectedIds.value.length}条)`;
  approvalForm.value = { reason: '', comment: '' };
  approvalDialogVisible.value = true;
};

const rejectSelected = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要审批的申请');
    return;
  }
  
  pendingApprovalRequest.value = { ids: selectedIds.value };
  approvalAction.value = 'reject';
  approvalDialogTitle.value = `批量审批拒绝 (${selectedIds.value.length}条)`;
  approvalForm.value = { reason: '', comment: '' };
  approvalDialogVisible.value = true;
};

// 确认审批
const confirmApproval = async () => {
  if (!approvalFormRef.value) return;
  
  try {
    if (approvalAction.value === 'reject') {
      await approvalFormRef.value.validateField('reason');
    }
  } catch (error) {
    return;
  }
  
  approvalLoading.value = true;
  
  try {
    const isBatch = Array.isArray(pendingApprovalRequest.value?.ids);
    
    if (isBatch) {
      // 批量审批
      await axios.post('/api/approval/batch', {
        requestIds: pendingApprovalRequest.value.ids,
        approverId: adminUser.value.id,
        action: approvalAction.value,
        comment: approvalAction.value === 'reject' ? approvalForm.value.reason : approvalForm.value.comment
      });
      
      ElMessage.success(`批量${approvalAction.value === 'approve' ? '通过' : '拒绝'}成功`);
    } else {
      // 单个审批
      const url = `/api/approval/${pendingApprovalRequest.value.id}/${approvalAction.value}`;
      await axios.post(url, {
        approverId: adminUser.value.id,
        reason: approvalForm.value.reason,
        comment: approvalForm.value.comment
      });
      
      ElMessage.success(`${approvalAction.value === 'approve' ? '通过' : '拒绝'}成功`);
    }
    
    approvalDialogVisible.value = false;
    detailDialogVisible.value = false;
    
    // 刷新数据
    await refreshData();
    
  } catch (error) {
    console.error('审批操作失败:', error);
    ElMessage.error('审批操作失败：' + (error.response?.data?.message || error.message));
  } finally {
    approvalLoading.value = false;
  }
};

const handleServerMonitor = () => {
  router.push("/server-monitor");
};

// 页面初始化
onMounted(async () => {
  // 检查管理员登录状态
  if (!adminUser.value.id) {
    ElMessage.error('请先登录管理员账户');
    router.push('/');
    return;
  }
  
  await refreshData();
});
</script>

<style scoped>
.approval-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 24px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.statistics-panel {
  margin-bottom: 30px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-content {
  position: relative;
  z-index: 2;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 48px;
  opacity: 0.1;
}

.stat-card.pending .stat-number { color: #e6a23c; }
.stat-card.approved .stat-number { color: #67c23a; }
.stat-card.rejected .stat-number { color: #f56c6c; }
.stat-card.cancelled .stat-number { color: #909399; }

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.request-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.pagination-container {
  padding: 20px;
  text-align: center;
  border-top: 1px solid #ebeef5;
}

.request-detail {
  padding: 20px 0;
}

.resume-preview-section,
.approval-history-section {
  margin-top: 30px;
}

.resume-preview-section h3,
.approval-history-section h3 {
  margin-bottom: 16px;
  color: #2c3e50;
  font-weight: 600;
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
  display: flex;
  justify-content: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.preview-resume-content {
  position: relative;
  width: 360px;
  height: 509px; /* A4比例缩小 */
  background-color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #eaeaea;
  border-radius: 8px;
  overflow: hidden;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Element Plus 样式覆盖 */
:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table th) {
  background-color: #fafafa;
  font-weight: 600;
}

:deep(.el-button-group .el-button) {
  margin: 0;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #2c3e50;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 12px;
}

@media (max-width: 768px) {
  .approval-management {
    padding: 10px;
  }
  
  .management-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .statistics-panel :deep(.el-col) {
    margin-bottom: 16px;
  }
  
  .filter-section :deep(.el-col) {
    margin-bottom: 12px;
  }
}
</style> 