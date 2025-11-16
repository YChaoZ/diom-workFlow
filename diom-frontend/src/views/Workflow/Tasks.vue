<template>
  <div class="workflow-tasks">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的任务</span>
          <el-button type="primary" @click="loadTasks" :icon="Refresh">
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table 
        :data="taskList" 
        v-loading="loading" 
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无待办任务'"
        stripe
        highlight-current-row
      >
        <el-table-column prop="name" label="任务名称" min-width="150">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 8px; color: #409eff;"><Document /></el-icon>
              <span style="font-weight: 500;">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="processDefinitionName" label="流程名称" min-width="120" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 6px; color: #909399;"><Clock /></el-icon>
              <span>{{ formatTime(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="办理人" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleTask(row.id)"
            >
              立即处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="taskList.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Document, Clock } from '@element-plus/icons-vue'
import { getMyTasks } from '@/api/workflow'
import { useUserStore } from '@/stores/user'
import { formatTime } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const taskList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const username = userStore.userInfo?.username
    if (!username) {
      ElMessage.warning('请先登录')
      return
    }
    
    const res = await getMyTasks({ assignee: username })
    if (res.code === 200) {
      taskList.value = res.data || []
      total.value = taskList.value.length
    }
  } catch (error) {
    ElMessage.error('加载任务列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 处理任务
const handleTask = (taskId) => {
  router.push(`/workflow/task/${taskId}`)
}

// 分页改变
const handlePageChange = (page) => {
  currentPage.value = page
  // 这里可以实现真正的分页加载
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.workflow-tasks {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

