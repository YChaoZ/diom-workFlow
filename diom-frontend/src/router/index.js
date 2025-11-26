import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { title: '登录', requireAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout/index.vue'),
    redirect: '/home',
    meta: { requireAuth: true },
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: '/workflow/list',
        name: 'WorkflowList',
        component: () => import('@/views/Workflow/List.vue'),
        meta: { title: '流程定义', icon: 'List', parent: '工作流管理' }
      },
      {
        path: '/workflow/start/:key',
        name: 'WorkflowStart',
        component: () => import('@/views/Workflow/Start.vue'),
        meta: { title: '发起流程', hidden: true }
      },
      {
        path: '/workflow/tasks',
        name: 'WorkflowTasks',
        component: () => import('@/views/Workflow/Tasks.vue'),
        meta: { title: '我的任务', icon: 'Document', parent: '工作流管理' }
      },
      {
        path: '/workflow/task/:id',
        name: 'WorkflowTaskDetail',
        component: () => import('@/views/Workflow/TaskDetail.vue'),
        meta: { title: '任务详情', hidden: true }
      },
      {
        path: '/workflow/instances',
        name: 'WorkflowInstances',
        component: () => import('@/views/Workflow/Instances.vue'),
        meta: { title: '流程实例', icon: 'CopyDocument', parent: '工作流管理' }
      },
    {
      path: '/workflow/templates',
      name: 'WorkflowTemplates',
      component: () => import('@/views/Workflow/Templates.vue'),
      meta: { title: '模板管理', icon: 'Files', parent: '工作流管理' }
    },
    // 流程设计器路由（只有超管可见）
    {
      path: '/workflow/design/list',
      name: 'ProcessDesignList',
      component: () => import('@/views/Workflow/ProcessDesignList.vue'),
      meta: { title: '流程设计器', icon: 'Edit', parent: '工作流管理', requireRole: 'SUPER_ADMIN' }
    },
    // Flowable Modeler 路由（替代 bpmn.js）
    {
      path: '/workflow/modeler',
      name: 'FlowableModeler',
      component: () => import('@/views/Workflow/FlowableModeler.vue'),
      meta: { title: 'Flowable 流程设计器', hidden: true, permission: 'workflow:design:access' }
    },
    {
      path: '/workflow/design/new',
      name: 'ProcessDesignNew',
      component: () => import('@/flyflow/views/flow/create.vue'),
      meta: { title: '新建流程', hidden: true, permission: 'workflow:design:create' }
    },
    {
      path: '/workflow/design/edit/:id',
      name: 'ProcessDesignEdit',
      component: () => import('@/flyflow/views/flow/create.vue'),
      meta: { title: '编辑流程', hidden: true, permission: 'workflow:design:edit' }
    },
      {
        path: '/workflow/design/view/:id',
        name: 'ProcessDesignView',
        component: () => import('@/views/Workflow/FlowableModeler.vue'),
        meta: { title: '查看流程', hidden: true, permission: 'workflow:design:view' }
      },
      // FlyFlow 测试页面
      {
        path: '/workflow/flyflow-test',
        name: 'FlyFlowTest',
        component: () => import('@/views/Workflow/FlyFlowTest.vue'),
        meta: { title: 'FlyFlow 测试', icon: 'Test', parent: '工作流管理', hidden: false }
      },
    {
      path: '/notifications',
      name: 'Notifications',
      component: () => import('@/views/Notifications/index.vue'),
      meta: { title: '消息通知', icon: 'Bell' }
    },
      {
        path: '/user/profile',
        name: 'UserProfile',
        component: () => import('@/views/User/Profile.vue'),
        meta: { title: '个人信息', icon: 'UserFilled', parent: '用户中心' }
      },
      {
        path: '/user/list',
        name: 'UserList',
        component: () => import('@/views/User/List.vue'),
        meta: { title: '用户列表', icon: 'User', parent: '用户中心' }
      },
      {
        path: '/system/role',
        name: 'SystemRole',
        component: () => import('@/views/System/Role.vue'),
        meta: { title: '角色管理', icon: 'User', parent: '系统管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - DIOM工作流系统` : 'DIOM工作流系统'

  // 检查是否需要登录
  if (to.meta.requireAuth !== false) {
    if (isLoggedIn()) {
      next()
    } else {
      ElMessage.warning('请先登录')
      next('/login')
    }
  } else {
    // 如果已登录，访问登录页则重定向到首页
    if (to.path === '/login' && isLoggedIn()) {
      next('/')
    } else {
      next()
    }
  }
})

export default router

