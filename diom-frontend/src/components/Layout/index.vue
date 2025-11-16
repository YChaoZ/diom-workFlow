<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse">DIOM工作流</span>
        <span v-else>D</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
      >
        <template v-for="menu in visibleMenus" :key="menu.index">
          <!-- 有子菜单的项 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.index">
            <template #title>
              <el-icon>
                <component :is="menu.icon" />
              </el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.index"
              :index="child.index"
            >
              <el-icon>
                <component :is="child.icon" />
              </el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 无子菜单的项 -->
          <el-menu-item v-else :index="menu.index">
            <el-icon>
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主体内容 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        
        <div class="header-right">
          <!-- 通知图标 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-icon 
              :size="20" 
              class="notification-icon" 
              @click="goToNotifications"
            >
              <Bell />
            </el-icon>
          </el-badge>

          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ userInfo?.nickname || userInfo?.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUnreadCount } from '@/api/notification'
import { 
  UserFilled, Fold, Expand, ArrowDown, User, SwitchButton,
  HomeFilled, Operation, List, Document, CopyDocument, Files, Bell, Setting, Edit
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const unreadCount = ref(0)
const activeMenu = ref(route.path)
let unreadInterval = null

// 用户信息和权限
const userInfo = computed(() => userStore.userInfo)
const permissions = computed(() => userStore.permissions)
const roles = computed(() => userStore.roles)

// 菜单配置
const allMenus = [
  {
    index: '/home',
    title: '首页',
    icon: 'HomeFilled',
    permission: 'DASHBOARD'
  },
  {
    index: 'workflow',
    title: '工作流管理',
    icon: 'Operation',
    permission: 'WORKFLOW',
    children: [
      {
        index: '/workflow/list',
        title: '流程定义',
        icon: 'List',
        permission: 'WORKFLOW_DEFINITION'
      },
      {
        index: '/workflow/tasks',
        title: '我的任务',
        icon: 'Document',
        permission: 'WORKFLOW_TASK'
      },
      {
        index: '/workflow/instances',
        title: '流程实例',
        icon: 'CopyDocument',
        permission: 'WORKFLOW_INSTANCE'
      },
      {
        index: '/workflow/templates',
        title: '模板管理',
        icon: 'Files',
        permission: 'WORKFLOW_TEMPLATE'
      },
      {
        index: '/workflow/design/list',
        title: '流程设计器',
        icon: 'Edit',
        requireRole: 'SUPER_ADMIN'  // 只有超管可见
      }
    ]
  },
  {
    index: 'user',
    title: '用户中心',
    icon: 'User',
    permission: 'USER_CENTER',
    children: [
      {
        index: '/user/profile',
        title: '个人信息',
        icon: 'UserFilled',
        permission: 'USER_CENTER'
      },
      {
        index: '/user/list',
        title: '用户列表',
        icon: 'User',
        permission: 'SYSTEM_USER'
      }
    ]
  },
  {
    index: '/notifications',
    title: '消息通知',
    icon: 'Bell',
    permission: 'WORKFLOW_NOTIFICATION'
  },
  {
    index: 'system',
    title: '系统管理',
    icon: 'Setting',
    permission: 'SYSTEM',
    children: [
      {
        index: '/system/role',
        title: '角色管理',
        icon: 'User',
        permission: 'SYSTEM_ROLE'
      }
    ]
  }
]

// 权限检查函数
const hasPermission = (permissionCode) => {
  if (!permissionCode) return true
  return permissions.value.includes(permissionCode)
}

// 角色检查函数
const hasRole = (roleCode) => {
  if (!roleCode) return true
  return roles.value.some(role => role.roleCode === roleCode)
}

// 过滤后的可见菜单
const visibleMenus = computed(() => {
  return allMenus.filter(menu => {
    // 检查父菜单权限和角色
    if (menu.permission && !hasPermission(menu.permission)) {
      return false
    }
    if (menu.requireRole && !hasRole(menu.requireRole)) {
      return false
    }
    
    // 过滤子菜单
    if (menu.children) {
      menu.children = menu.children.filter(child => {
        // 检查子菜单权限
        if (child.permission && !hasPermission(child.permission)) {
          return false
        }
        // 检查子菜单角色
        if (child.requireRole && !hasRole(child.requireRole)) {
          return false
        }
        return true
      })
      // 如果子菜单全部被过滤掉，父菜单也隐藏
      if (menu.children.length === 0) {
        return false
      }
    }
    
    return true
  })
})

// 监听路由变化，更新激活菜单
watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
})

onMounted(() => {
  loadUnreadCount()
  // 每30秒刷新一次未读数
  unreadInterval = setInterval(loadUnreadCount, 30000)
})

// 组件卸载时清除定时器
onBeforeUnmount(() => {
  if (unreadInterval) {
    clearInterval(unreadInterval)
    unreadInterval = null
  }
})

// 切换侧边栏折叠
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 加载未读通知数
const loadUnreadCount = async () => {
  try {
    const username = userInfo.value?.username
    if (!username) return

    const res = await getUnreadCount(username)
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    // 静默处理错误（可能是未登录或接口不存在）
    // 不显示错误信息，避免干扰用户
  }
}

// 跳转到通知中心
const goToNotifications = () => {
  router.push('/notifications')
}

// 下拉菜单命令处理
const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      // 等待退出登录完成
      await userStore.logoutAction()
      ElMessage.success('已退出登录')
      // 清除定时器
      if (unreadInterval) {
        clearInterval(unreadInterval)
        unreadInterval = null
      }
      // 跳转到登录页
      router.push('/login')
    }).catch(() => {})
  }
}

// 页面加载时获取用户信息
if (!userInfo.value) {
  userStore.getUserInfoAction()
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
  color: white;
  font-size: 18px;
  font-weight: bold;
  transition: all 0.3s;
}

.el-menu {
  border-right: none;
  background-color: #304156;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #bfcbd9;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: #fff;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.collapse-icon:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  cursor: pointer;
}

.notification-icon {
  transition: color 0.3s;
}

.notification-icon:hover {
  color: #409eff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #333;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

