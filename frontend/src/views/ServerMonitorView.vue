<template>
  <div class="server-monitor">
    <div class="monitor-header">
      <h1>服务器负载监控</h1>
      <div class="monitor-controls">
        <el-button 
          type="primary" 
          @click="refreshData" 
          :loading="loading"
          :disabled="loading"
        >
          刷新数据
        </el-button>
        <el-switch
          v-model="autoRefresh"
          @change="toggleAutoRefresh"
          active-text="自动刷新"
          inactive-text="手动刷新"
        />
      </div>
    </div>

    <div class="monitor-content">
      <!-- 服务器基本信息 -->
      <div class="info-cards">
        <el-card class="info-card">
          <template #header>
            <span class="card-title">
              <el-icon><Cpu /></el-icon>
              CPU 使用率
            </span>
          </template>
          <div class="metric-display">
            <div class="metric-value" :class="getCpuStatusClass()">
              {{ serverInfo.cpu }}%
            </div>
            <div class="metric-trend">
              <el-progress 
                :percentage="serverInfo.cpu" 
                :color="getCpuColor()"
                :show-text="false"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>

        <el-card class="info-card">
          <template #header>
            <span class="card-title">
              <el-icon><Monitor /></el-icon>
              内存使用率
            </span>
          </template>
          <div class="metric-display">
            <div class="metric-value" :class="getMemoryStatusClass()">
              {{ serverInfo.memory }}%
            </div>
            <div class="metric-trend">
              <el-progress 
                :percentage="serverInfo.memory" 
                :color="getMemoryColor()"
                :show-text="false"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>

        <el-card class="info-card">
          <template #header>
            <span class="card-title">
              <el-icon><DataAnalysis /></el-icon>
              磁盘使用率
            </span>
          </template>
          <div class="metric-display">
            <div class="metric-value" :class="getDiskStatusClass()">
              {{ serverInfo.disk }}%
            </div>
            <div class="metric-trend">
              <el-progress 
                :percentage="serverInfo.disk" 
                :color="getDiskColor()"
                :show-text="false"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>
      </div>

      <!-- 历史图表 -->
      <div class="charts-container">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">历史负载趋势</span>
          </template>
          <div class="chart-wrapper">
            <canvas ref="historyChart" id="historyChart"></canvas>
          </div>
        </el-card>
      </div>

      <!-- DeepSeek 分析结果 -->
      <div class="analysis-container">
        <el-card class="analysis-card">
          <template #header>
            <div class="analysis-header">
              <span class="card-title">
                <el-icon><ChatLineRound /></el-icon>
                AI 服务器状态分析
              </span>
              <el-button 
                type="primary" 
                size="small" 
                @click="getAIAnalysis"
                :loading="aiAnalysisLoading"
                :disabled="aiAnalysisLoading"
              >
                获取分析
              </el-button>
            </div>
          </template>
          <div class="analysis-content">
            <div v-if="!aiAnalysis && !aiAnalysisLoading" class="analysis-placeholder">
              点击"获取分析"按钮，AI 将分析当前服务器状态并提供建议
            </div>
            <div v-if="aiAnalysisLoading" class="analysis-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              AI 正在分析服务器状态...
            </div>
            <div v-if="aiAnalysis" class="analysis-result">
              <div class="analysis-text">{{ aiAnalysis }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, markRaw, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Monitor, 
  Cpu, 
  DataAnalysis, 
  ChatLineRound, 
  Loading 
} from '@element-plus/icons-vue'
import Chart from 'chart.js/auto'

interface ServerInfo {
  cpu: number
  memory: number
  disk: number
  timestamp: string
}

const serverInfo = ref<ServerInfo>({
  cpu: 0,
  memory: 0,
  disk: 0,
  timestamp: ''
})

const loading = ref(false)
const autoRefresh = ref(true)
const aiAnalysis = ref('')
const aiAnalysisLoading = ref(false)
const historyChart = ref<HTMLCanvasElement>()
const chart = shallowRef<Chart | null>(null)

let refreshInterval: number | null = null

// 历史数据 - 使用普通数组而不是响应式对象
const historyData = ref<ServerInfo[]>([])
// 使用完全非响应式的数据存储，避免Vue响应式系统监听
let chartData = markRaw({
  labels: [] as string[],
  cpuData: [] as number[],
  memoryData: [] as number[],
  diskData: [] as number[]
})

const fetchServerInfo = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/monitor/server-info')
    if (!response.ok) {
      throw new Error('获取服务器信息失败')
    }
    const data = await response.json()
    
    // 处理数据
    const processedData = {
      cpu: typeof data.cpu === 'number' ? data.cpu : Math.random() * 50 + 10,
      memory: typeof data.memory === 'number' ? data.memory : Math.random() * 60 + 20,
      disk: typeof data.disk === 'number' ? data.disk : Math.random() * 40 + 30,
      timestamp: data.timestamp || new Date().toISOString()
    }
    
    serverInfo.value = processedData
    
    // 添加到历史数据
    historyData.value.push({ ...processedData })
    
    // 保持最近50个数据点
    if (historyData.value.length > 50) {
      historyData.value.shift()
    }
    
    // 更新图表数据（非响应式）
    const date = new Date(processedData.timestamp)
    const timeString = date.toLocaleTimeString()
    
    // 安全地更新非响应式数据
    try {
      chartData.labels.push(timeString)
      chartData.cpuData.push(processedData.cpu)
      chartData.memoryData.push(processedData.memory)
      chartData.diskData.push(processedData.disk)
      
      // 保持最近50个数据点
      if (chartData.labels.length > 50) {
        chartData.labels.shift()
        chartData.cpuData.shift()
        chartData.memoryData.shift()
        chartData.diskData.shift()
      }
    } catch (dataError) {
      console.error('更新图表数据失败:', dataError)
    }
    
    // 更新图表
    if (chart.value) {
      updateChart()
    }
  } catch (error) {
    console.error('获取服务器信息失败:', error)
    ElMessage.error('获取服务器信息失败，使用模拟数据')
    
    // 使用模拟数据
    const mockData = {
      cpu: Math.random() * 50 + 10,
      memory: Math.random() * 60 + 20,
      disk: Math.random() * 40 + 30,
      timestamp: new Date().toISOString()
    }
    
    serverInfo.value = mockData
    historyData.value.push({ ...mockData })
    
    // 更新图表数据（非响应式）
    const date = new Date(mockData.timestamp)
    const timeString = date.toLocaleTimeString()
    
    // 安全地更新非响应式数据
    try {
      chartData.labels.push(timeString)
      chartData.cpuData.push(mockData.cpu)
      chartData.memoryData.push(mockData.memory)
      chartData.diskData.push(mockData.disk)
      
      // 保持最近50个数据点
      if (chartData.labels.length > 50) {
        chartData.labels.shift()
        chartData.cpuData.shift()
        chartData.memoryData.shift()
        chartData.diskData.shift()
      }
    } catch (dataError) {
      console.error('更新模拟数据失败:', dataError)
    }
    
    if (historyData.value.length > 50) {
      historyData.value.shift()
    }
    
    if (chart.value) {
      updateChart()
    }
  } finally {
    loading.value = false
  }
}

const startAutoRefresh = () => {
  stopAutoRefresh() // 先停止现有的刷新
  
  try {
    refreshInterval = window.setInterval(() => {
      if (!document.hidden) { // 只在页面可见时刷新
        fetchServerInfo().catch(error => {
          console.error('自动刷新数据失败:', error)
          // 如果连续失败，可能需要重置图表
          if (error.message && error.message.includes('Maximum call stack size exceeded')) {
            console.warn('检测到堆栈溢出，尝试重置图表')
            setTimeout(() => {
              try {
                if (chart.value) {
                  chart.value.destroy()
                  chart.value = null
                }
                initChart()
              } catch (resetError) {
                console.error('重置图表失败:', resetError)
              }
            }, 1000)
          }
        })
      }
    }, 5000) // 每5秒刷新一次
    console.log('自动刷新已启动')
  } catch (error) {
    console.error('启动自动刷新失败:', error)
  }
}

const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
    console.log('自动刷新已停止')
  }
}

const toggleAutoRefresh = (value: boolean) => {
  if (value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const refreshData = async () => {
  try {
    await fetchServerInfo()
    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('手动刷新失败:', error)
    ElMessage.error('刷新失败')
  }
}

const getAIAnalysis = async () => {
  aiAnalysisLoading.value = true
  try {
    const response = await fetch('/api/monitor/ai-analysis', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(serverInfo.value)
    })
    
    if (!response.ok) {
      throw new Error('获取AI分析失败')
    }
    
    const data = await response.json()
    if (data && data.analysis) {
      aiAnalysis.value = data.analysis
    } else {
      throw new Error('返回数据格式不正确')
    }
  } catch (error) {
    console.error('获取AI分析失败:', error)
    ElMessage.error('获取AI分析失败，使用模拟分析')
    
    // 使用模拟分析结果
    aiAnalysis.value = generateMockAnalysis(serverInfo.value)
  } finally {
    aiAnalysisLoading.value = false
  }
}

// 生成模拟分析结果
const generateMockAnalysis = (info: ServerInfo) => {
  const cpu = info.cpu
  const memory = info.memory
  const disk = info.disk
  
  let analysis = '🔍 服务器状态分析报告\n\n'
  
  // 整体健康状况评估
  analysis += '📊 整体健康状况：'
  if (cpu < 60 && memory < 70 && disk < 80) {
    analysis += '良好 ✅\n'
  } else if (cpu < 80 && memory < 80 && disk < 90) {
    analysis += '警告 ⚠️\n'
  } else {
    analysis += '严重 ❌\n'
  }
  
  analysis += '\n🔧 详细分析：\n'
  
  // CPU分析
  if (cpu < 50) {
    analysis += '• CPU负载正常，系统运行流畅\n'
  } else if (cpu < 80) {
    analysis += '• CPU负载较高，建议关注进程占用情况\n'
  } else {
    analysis += '• CPU负载过高，可能影响系统响应速度\n'
  }
  
  // 内存分析
  if (memory < 60) {
    analysis += '• 内存使用正常，有充足的可用空间\n'
  } else if (memory < 80) {
    analysis += '• 内存使用率较高，建议监控内存泄漏\n'
  } else {
    analysis += '• 内存使用率过高，建议及时释放或增加内存\n'
  }
  
  // 磁盘分析
  if (disk < 70) {
    analysis += '• 磁盘空间充足，无需担心\n'
  } else if (disk < 90) {
    analysis += '• 磁盘使用率较高，建议清理不必要的文件\n'
  } else {
    analysis += '• 磁盘空间不足，建议立即清理或扩容\n'
  }
  
  analysis += '\n💡 优化建议：\n'
  
  if (cpu > 70) {
    analysis += '• 检查CPU密集型进程，考虑优化算法或增加服务器资源\n'
  }
  
  if (memory > 70) {
    analysis += '• 监控内存使用情况，及时回收不必要的对象\n'
  }
  
  if (disk > 80) {
    analysis += '• 定期清理日志文件和临时文件\n'
    analysis += '• 考虑数据归档或磁盘扩容\n'
  }
  
  if (cpu < 50 && memory < 60 && disk < 70) {
    analysis += '• 服务器运行状态良好，继续保持监控\n'
  }
  
  return analysis
}

const getCpuStatusClass = () => {
  const cpu = serverInfo.value.cpu
  if (cpu >= 80) return 'status-danger'
  if (cpu >= 60) return 'status-warning'
  return 'status-normal'
}

const getMemoryStatusClass = () => {
  const memory = serverInfo.value.memory
  if (memory >= 80) return 'status-danger'
  if (memory >= 60) return 'status-warning'
  return 'status-normal'
}

const getDiskStatusClass = () => {
  const disk = serverInfo.value.disk
  if (disk >= 80) return 'status-danger'
  if (disk >= 60) return 'status-warning'
  return 'status-normal'
}

const getCpuColor = () => {
  const cpu = serverInfo.value.cpu
  if (cpu >= 80) return '#f56c6c'
  if (cpu >= 60) return '#e6a23c'
  return '#67c23a'
}

const getMemoryColor = () => {
  const memory = serverInfo.value.memory
  if (memory >= 80) return '#f56c6c'
  if (memory >= 60) return '#e6a23c'
  return '#67c23a'
}

const getDiskColor = () => {
  const disk = serverInfo.value.disk
  if (disk >= 80) return '#f56c6c'
  if (disk >= 60) return '#e6a23c'
  return '#67c23a'
}

const initChart = () => {
  if (!historyChart.value) {
    console.warn('图表DOM元素未找到')
    return
  }

  try {
    const ctx = historyChart.value.getContext('2d')
    if (!ctx) {
      console.warn('无法获取图表上下文')
      return
    }

    // 销毁现有图表（如果存在）
    if (chart.value) {
      chart.value.destroy()
      chart.value = null
    }

    // 创建完全独立的数据副本，确保非响应式
    const chartConfig = markRaw({
      type: 'line',
      data: {
        labels: [...chartData.labels],
        datasets: [
          {
            label: 'CPU 使用率',
            data: [...chartData.cpuData],
            borderColor: '#409EFF',
            backgroundColor: 'rgba(64, 158, 255, 0.1)',
            tension: 0.4
          },
          {
            label: '内存使用率',
            data: [...chartData.memoryData],
            borderColor: '#67C23A',
            backgroundColor: 'rgba(103, 194, 58, 0.1)',
            tension: 0.4
          },
          {
            label: '磁盘使用率',
            data: [...chartData.diskData],
            borderColor: '#E6A23C',
            backgroundColor: 'rgba(230, 162, 60, 0.1)',
            tension: 0.4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: false, // 禁用动画，减少性能开销
        interaction: {
          intersect: false,
          mode: 'index'
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 100,
            ticks: {
              callback: function(value) {
                return value + '%'
              }
            }
          },
          x: {
            display: true,
            title: {
              display: true,
              text: '时间'
            }
          }
        },
        plugins: {
          legend: {
            display: true,
            position: 'top'
          },
          tooltip: {
            mode: 'index',
            intersect: false,
            callbacks: {
              label: function(context) {
                return context.dataset.label + ': ' + context.parsed.y + '%'
              }
            }
          }
        }
      }
    })

    // 使用markRaw包装Chart实例，防止响应式处理
    const chartInstance = new Chart(ctx, chartConfig)
    chart.value = markRaw(chartInstance)

    console.log('图表初始化成功')
  } catch (error) {
    console.error('初始化图表失败:', error)
  }
}

const updateChart = () => {
  if (!chart.value) {
    console.warn('图表实例不存在，跳过更新')
    return
  }

  try {
    // 创建完全独立的数据副本，防止响应式监听
    const newLabels = JSON.parse(JSON.stringify(chartData.labels))
    const newCpuData = JSON.parse(JSON.stringify(chartData.cpuData))
    const newMemoryData = JSON.parse(JSON.stringify(chartData.memoryData))
    const newDiskData = JSON.parse(JSON.stringify(chartData.diskData))
    
    // 安全地更新图表数据
    const chartInstance = chart.value
    if (chartInstance && chartInstance.data && chartInstance.data.datasets) {
      // 使用splice方法完全替换数组内容，避免引用问题
      chartInstance.data.labels.splice(0, chartInstance.data.labels.length, ...newLabels)
      
      if (chartInstance.data.datasets[0]) {
        chartInstance.data.datasets[0].data.splice(0, chartInstance.data.datasets[0].data.length, ...newCpuData)
      }
      if (chartInstance.data.datasets[1]) {
        chartInstance.data.datasets[1].data.splice(0, chartInstance.data.datasets[1].data.length, ...newMemoryData)
      }
      if (chartInstance.data.datasets[2]) {
        chartInstance.data.datasets[2].data.splice(0, chartInstance.data.datasets[2].data.length, ...newDiskData)
      }
      
      // 使用更安全的更新方式
      chartInstance.update('none')
    }
  } catch (error) {
    console.error('更新图表失败:', error)
    // 如果更新失败，尝试重新初始化图表
    setTimeout(() => {
      try {
        initChart()
      } catch (reinitError) {
        console.error('重新初始化图表失败:', reinitError)
      }
    }, 100)
  }
}

onMounted(async () => {
  try {
    // 先获取数据
    await fetchServerInfo()
    
    // 使用setTimeout确保DOM已更新
    setTimeout(() => {
      try {
        initChart()
        console.log('Chart initialization completed')
      } catch (chartError) {
        console.error('Chart initialization error:', chartError)
      }
      
      // 延迟启动自动刷新，避免初始化阶段的冲突
      if (autoRefresh.value) {
        setTimeout(() => {
          startAutoRefresh()
        }, 1000)
      }
    }, 300) // 增加延迟时间，确保DOM完全渲染
  } catch (error) {
    console.error('组件挂载时出错:', error)
  }
})

// 在组件卸载时清理资源
onUnmounted(() => {
  try {
    stopAutoRefresh()
    if (chart.value) {
      chart.value.destroy()
      chart.value = null
    }
    // 清理图表数据
    chartData.labels.length = 0
    chartData.cpuData.length = 0
    chartData.memoryData.length = 0
    chartData.diskData.length = 0
  } catch (error) {
    console.error('组件卸载时出错:', error)
  }
})
</script>

<style scoped>
.server-monitor {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.monitor-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
}

.monitor-controls {
  display: flex;
  gap: 15px;
  align-items: center;
}

.monitor-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-card {
  min-height: 120px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.metric-display {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.metric-value {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
}

.status-normal {
  color: #67c23a;
}

.status-warning {
  color: #e6a23c;
}

.status-danger {
  color: #f56c6c;
}

.charts-container {
  margin-top: 20px;
}

.chart-card {
  min-height: 400px;
}

.chart-wrapper {
  height: 350px;
  position: relative;
}

.analysis-container {
  margin-top: 20px;
}

.analysis-card {
  min-height: 200px;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.analysis-content {
  min-height: 150px;
}

.analysis-placeholder {
  color: #909399;
  text-align: center;
  padding: 40px;
  font-style: italic;
}

.analysis-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #409EFF;
  padding: 40px;
}

.analysis-result {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
}

.analysis-text {
  line-height: 1.6;
  color: #2c3e50;
  white-space: pre-wrap;
}

:deep(.el-card__header) {
  background-color: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-progress-bar__outer) {
  border-radius: 4px;
}

:deep(.el-progress-bar__inner) {
  border-radius: 4px;
}

.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style> 