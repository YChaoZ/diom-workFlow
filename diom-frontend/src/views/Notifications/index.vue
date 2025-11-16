<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <el-button 
            v-if="unreadCount > 0" 
            type="primary" 
            size="small"
            @click="handleMarkAllRead"
          >
            全部已读
          </el-button>
        </div>
      </template>

      <!-- 过滤标签 -->
      <el-tabs v-model="activeTab" @tab-change="loadNotifications">
        <el-tab-pane label="全部" name="all">
          <el-badge :value="totalCount" :hidden="totalCount === 0" />
        </el-tab-pane>
        <el-tab-pane label="未读" name="unread">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" />
        </el-tab-pane>
      </el-tabs>

      <!-- 通知列表 -->
      <el-empty 
        v-if="notifications.length === 0" 
        description="暂无通知"
      />
      <div v-else class="notification-list">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: notification.isRead === 0 }"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-icon">
            <el-icon v-if="notification.type === 'TASK_ASSIGNED'" color="#409eff" :size="24">
              <Notification />
            </el-icon>
            <el-icon v-else-if="notification.type === 'TASK_COMPLETED'" color="#67c23a" :size="24">
              <SuccessFilled />
            </el-icon>
            <el-icon v-else-if="notification.type === 'PROCESS_STARTED'" color="#409eff" :size="24">
              <MessageBox />
            </el-icon>
            <el-icon v-else color="#909399" :size="24">
              <InfoFilled />
            </el-icon>
          </div>
          
          <div class="notification-content">
            <div class="notification-title">
              <span>{{ notification.title }}</span>
              <el-tag 
                v-if="notification.priority === 'HIGH'" 
                type="danger" 
                size="small"
              >
                重要
              </el-tag>
              <el-tag 
                v-if="notification.priority === 'URGENT'" 
                type="danger" 
                size="small"
                effect="dark"
              >
                紧急
              </el-tag>
            </div>
            <div class="notification-text">
              {{ notification.content }}
            </div>
            <div class="notification-meta">
              <span class="time">{{ formatTime(notification.createTime) }}</span>
              <el-tag v-if="notification.isRead === 0" type="success" size="small">未读</el-tag>
            </div>
          </div>

          <div class="notification-actions">
            <el-button
              v-if="notification.isRead === 0"
              type="primary"
              size="small"
              text
              @click.stop="handleMarkRead(notification.id)"
            >
              标记已读
            </el-button>
            <el-button
              type="danger"
              size="small"
              text
              @click.stop="handleDelete(notification.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="notifications.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredTotal"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: center;"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Notification, 
  SuccessFilled, 
  MessageBox, 
  InfoFilled 
} from '@element-plus/icons-vue'
import { 
  getNotifications, 
  getUnreadCount, 
  markAsRead, 
  markAllAsRead, 
  deleteNotification 
} from '@/api/notification'
import { useUserStore } from '@/stores/user'
import { formatTime } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('all')
const notifications = ref([])
const allNotifications = ref([])
const unreadCount = ref(0)
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filteredTotal = computed(() => {
  return activeTab.value === 'unread' 
    ? unreadCount.value 
    : totalCount.value
})

// 加载通知列表
const loadNotifications = async () => {
  try {
    const username = userStore.userInfo?.username
    if (!username) return

    const unreadOnly = activeTab.value === 'unread'
    const res = await getNotifications(username, unreadOnly)
    
    if (res.code === 200) {
      allNotifications.value = res.data || []
      totalCount.value = allNotifications.value.length
      
      // 分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      notifications.value = allNotifications.value.slice(start, end)
    }
  } catch (error) {
    ElMessage.error('加载通知失败: ' + error.message)
  }
}

// 加载未读数
const loadUnreadCount = async () => {
  try {
    const username = userStore.userInfo?.username
    if (!username) return

    const res = await getUnreadCount(username)
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载未读数失败:', error)
  }
}

// 点击通知
const handleNotificationClick = async (notification) => {
  // 如果未读，先标记为已读
  if (notification.isRead === 0) {
    await handleMarkRead(notification.id)
  }

  // 根据链接类型跳转
  if (notification.linkType === 'TASK' && notification.linkId) {
    router.push(`/workflow/task/${notification.linkId}`)
  } else if (notification.linkType === 'PROCESS' && notification.linkId) {
    router.push('/workflow/instances')
  }
}

// 标记为已读
const handleMarkRead = async (id) => {
  try {
    const res = await markAsRead(id)
    if (res.code === 200) {
      await loadNotifications()
      await loadUnreadCount()
    }
  } catch (error) {
    ElMessage.error('标记失败: ' + error.message)
  }
}

// 全部标记为已读
const handleMarkAllRead = async () => {
  try {
    const username = userStore.userInfo?.username
    if (!username) return

    const res = await markAllAsRead(username)
    if (res.code === 200) {
      ElMessage.success('已全部标记为已读')
      await loadNotifications()
      await loadUnreadCount()
    }
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

// 删除通知
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteNotification(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadNotifications()
      await loadUnreadCount()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

// 分页变化
const handlePageChange = () => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  notifications.value = allNotifications.value.slice(start, end)
}

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
})
</script>

<style scoped>
.notifications-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-list {
  margin-top: 20px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-icon {
  margin-right: 16px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.notification-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-actions {
  margin-left: 16px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>

