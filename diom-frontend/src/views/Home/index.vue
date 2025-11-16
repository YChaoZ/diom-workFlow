<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409eff" :size="48">
              <Document />
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.myTasks }}</div>
              <div class="stat-label">我的任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67c23a" :size="48">
              <CopyDocument />
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.myInstances }}</div>
              <div class="stat-label">我的流程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#e6a23c" :size="48">
              <Clock />
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingTasks }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#f56c6c" :size="48">
              <CircleCheck />
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completedTasks }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待办任务</span>
              <el-button text type="primary" @click="$router.push('/workflow/tasks')">
                查看全部
              </el-button>
            </div>
          </template>
          
          <el-table :data="recentTasks" style="width: 100%" v-loading="tasksLoading">
            <el-table-column prop="name" label="任务名称" />
            <el-table-column prop="processName" label="流程名称" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button text type="primary" @click="handleTask(row.id)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速入口</span>
            </div>
          </template>
          
          <div class="quick-actions">
            <el-button
              v-for="action in quickActions"
              :key="action.path"
              :icon="action.icon"
              @click="$router.push(action.path)"
              size="large"
            >
              {{ action.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, CopyDocument, Clock, CircleCheck, Plus, List, FolderOpened } from '@element-plus/icons-vue'
import { getMyTasks } from '@/api/workflow'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const tasksLoading = ref(false)
const recentTasks = ref([])

// 统计数据
const stats = reactive({
  myTasks: 0,
  myInstances: 0,
  pendingTasks: 0,
  completedTasks: 0
})

// 快速入口
const quickActions = [
  { label: '发起流程', path: '/workflow/list', icon: Plus },
  { label: '我的任务', path: '/workflow/tasks', icon: List },
  { label: '流程实例', path: '/workflow/instances', icon: FolderOpened }
]

// 加载待办任务
const loadTasks = async () => {
  tasksLoading.value = true
  try {
    const username = userStore.userInfo?.username
    if (!username) {
      return
    }
    
    const res = await getMyTasks({ assignee: username })
    if (res.code === 200 && res.data) {
      recentTasks.value = res.data.slice(0, 5) // 只显示前5条
      stats.myTasks = res.data.length
      stats.pendingTasks = res.data.filter(t => !t.endTime).length
      stats.completedTasks = res.data.filter(t => t.endTime).length
    }
  } catch (error) {
    console.error('加载任务失败:', error)
  } finally {
    tasksLoading.value = false
  }
}

// 处理任务
const handleTask = (taskId) => {
  router.push(`/workflow/task/${taskId}`)
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 10px 0;
}

.quick-actions .el-button {
  width: 100%;
  justify-content: flex-start;
}
</style>

