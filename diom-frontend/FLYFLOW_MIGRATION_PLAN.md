# 🎨 FlyFlow-Vue3 流程管理功能迁移方案

## 📊 项目结构对比

### FlyFlow-Vue3 核心功能

**流程管理** (`views/flyflow/views/flow/`)：
- ✅ `create.vue` - **创建流程**（拖拽式设计器，核心功能！）
- ✅ `list.vue` - 流程列表
- ✅ `group.vue` - 流程分组

**任务管理** (`views/flyflow/views/task/`)：
- ✅ `pending.vue` - 待办任务
- ✅ `completed.vue` - 已完成任务
- ✅ `started.vue` - 我发起的
- ✅ `cc.vue` - 抄送我的

**核心组件** (`views/flyflow/components/`)：
- ✅ **LogicFlow 流程设计器组件**（约 118 个文件）
- ✅ 流程节点组件
- ✅ 表单设计组件
- ✅ 流程审批组件

**支持文件**：
- ✅ API 接口 (`views/flyflow/api/`)
- ✅ 状态管理 (`views/flyflow/stores/`)
- ✅ 工具函数 (`views/flyflow/utils/`)
- ✅ 样式文件 (`views/flyflow/css/`)
- ✅ 图标和图片 (`views/flyflow/assets/`)

---

### diom-frontend 现有路由映射

| diom-frontend 路由 | 现有页面 | FlyFlow 替换页面 | 说明 |
|------------------|---------|----------------|------|
| `/workflow/list` | `List.vue` | `flow/list.vue` | 流程定义列表 |
| `/workflow/start/:key` | `Start.vue` | `flow/create.vue` | **发起流程（核心）** |
| `/workflow/tasks` | `Tasks.vue` | `task/pending.vue` | 待办任务 |
| `/workflow/instances` | `Instances.vue` | `task/started.vue` | 我发起的流程 |
| `/workflow/design/list` | `ProcessDesignList.vue` | `flow/create.vue` | 流程设计器 |

---

## 🎯 迁移策略（推荐）

### 方案：渐进式迁移 + 保持路由结构

**优点**：
- ✅ 不破坏现有路由和菜单
- ✅ 只替换页面组件，API 适配简单
- ✅ 可以逐个页面替换，风险可控
- ✅ 保留 FlyFlow 的优秀交互设计

**工作量**：约 **2-3 天**

---

## 📦 迁移步骤

### 第一阶段：准备工作（0.5 天）

#### 1. 安装必要的依赖

FlyFlow-Vue3 使用了 **LogicFlow** 流程引擎，需要安装：

```bash
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend

# 安装 LogicFlow（核心依赖）
npm install @logicflow/core@^1.2.10 @logicflow/extension@^1.2.10

# 安装其他必要依赖
npm install lodash-es@^4.17.21
npm install moment@^2.29.4
npm install nprogress@^0.2.0

# 如果使用 TypeScript（可选）
npm install @types/lodash --save-dev
```

#### 2. 复制 FlyFlow 资源到 diom-frontend

创建目录结构：

```bash
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend/src

# 创建 FlyFlow 资源目录
mkdir -p flyflow/components
mkdir -p flyflow/api
mkdir -p flyflow/stores
mkdir -p flyflow/utils
mkdir -p flyflow/assets
mkdir -p flyflow/css
```

需要复制的文件：

```
从 flyflow-vue3-master/src/views/flyflow/ 复制到 diom-frontend/src/flyflow/：

✅ components/（全部）- 约 118 个文件
✅ api/（全部）- API 接口定义
✅ stores/（全部）- 状态管理
✅ utils/（全部）- 工具函数
✅ assets/（全部）- 图标、图片
✅ css/（全部）- 样式文件
```

---

### 第二阶段：复制核心文件（0.5 天）

#### 执行复制命令

```bash
cd /Users/yanchao/IdeaProjects/diom-workFlow

# 复制组件
cp -r flyflow-vue3-master/src/views/flyflow/components diom-frontend/src/flyflow/

# 复制 API
cp -r flyflow-vue3-master/src/views/flyflow/api diom-frontend/src/flyflow/

# 复制状态管理
cp -r flyflow-vue3-master/src/views/flyflow/stores diom-frontend/src/flyflow/

# 复制工具函数
cp -r flyflow-vue3-master/src/views/flyflow/utils diom-frontend/src/flyflow/

# 复制资源文件
cp -r flyflow-vue3-master/src/views/flyflow/assets diom-frontend/src/flyflow/

# 复制样式文件
cp -r flyflow-vue3-master/src/views/flyflow/css diom-frontend/src/flyflow/

# 复制页面文件（用于替换现有页面）
cp -r flyflow-vue3-master/src/views/flyflow/views diom-frontend/src/flyflow/views
```

---

### 第三阶段：API 适配（1 天）

#### 1. 创建 API 适配器

**目的**：将 FlyFlow 的 API 调用映射到 `diom-flowable-service` 的 API

文件：`diom-frontend/src/flyflow/api/adapter.js`

```javascript
/**
 * FlyFlow API 适配器
 * 将 FlyFlow 的 API 调用适配到 diom-flowable-service
 */

import request from '@/utils/request'

/**
 * 流程定义 API 适配
 */
export const flowApi = {
  // FlyFlow: GET /api/flow/list
  // diom: GET /workflow/definitions
  getFlowList(params) {
    return request({
      url: '/workflow/definitions',
      method: 'get',
      params: {
        page: params.pageNum || 1,
        size: params.pageSize || 10,
        ...params
      }
    }).then(res => {
      // 转换响应格式
      return {
        code: '00000',
        data: {
          list: res.data || [],
          total: res.total || 0
        }
      }
    })
  },

  // FlyFlow: POST /api/flow/start
  // diom: POST /workflow/start/{key}
  startFlow(flowKey, data) {
    return request({
      url: `/workflow/start/${flowKey}`,
      method: 'post',
      data: data.variables || data
    }).then(res => {
      return {
        code: '00000',
        data: res.data,
        msg: '流程启动成功'
      }
    })
  },

  // FlyFlow: GET /api/flow/detail/{id}
  // diom: GET /workflow/definition/{key}
  getFlowDetail(id) {
    return request({
      url: `/workflow/definition/${id}`,
      method: 'get'
    }).then(res => {
      return {
        code: '00000',
        data: res.data
      }
    })
  },

  // FlyFlow: POST /api/flow/deploy
  // diom: POST /workflow/deploy
  deployFlow(data) {
    return request({
      url: '/workflow/deploy',
      method: 'post',
      data
    }).then(res => {
      return {
        code: '00000',
        data: res.data,
        msg: '流程部署成功'
      }
    })
  }
}

/**
 * 任务 API 适配
 */
export const taskApi = {
  // FlyFlow: GET /api/task/pending
  // diom: GET /workflow/tasks
  getPendingTasks(params) {
    return request({
      url: '/workflow/tasks',
      method: 'get',
      params: {
        assignee: params.assignee,
        page: params.pageNum || 1,
        size: params.pageSize || 10
      }
    }).then(res => {
      return {
        code: '00000',
        data: {
          list: res.data || [],
          total: res.total || 0
        }
      }
    })
  },

  // FlyFlow: POST /api/task/complete
  // diom: POST /workflow/task/complete
  completeTask(taskId, data) {
    return request({
      url: `/workflow/task/complete`,
      method: 'post',
      data: {
        taskId,
        variables: data.variables,
        comment: data.comment
      }
    }).then(res => {
      return {
        code: '00000',
        data: res.data,
        msg: '任务完成'
      }
    })
  },

  // FlyFlow: GET /api/task/started
  // diom: GET /workflow/my-instances
  getStartedTasks(params) {
    return request({
      url: '/workflow/my-instances',
      method: 'get',
      params: {
        page: params.pageNum || 1,
        size: params.pageSize || 10
      }
    }).then(res => {
      return {
        code: '00000',
        data: {
          list: res.data || [],
          total: res.total || 0
        }
      }
    })
  }
}
```

#### 2. 修改 FlyFlow 组件中的 API 调用

**方法一：全局替换（推荐）**

在 FlyFlow 组件中，将原来的 API 导入：

```javascript
// 原来
import { getFlowList } from '@/api/flow'

// 修改为
import { flowApi } from '@/flyflow/api/adapter'

// 调用时
flowApi.getFlowList(params)
```

**方法二：批量查找替换**

使用 VS Code 的批量替换功能：

1. 在 `diom-frontend/src/flyflow/` 目录下搜索：`from '@/api/`
2. 替换为：`from '@/flyflow/api/adapter'`

---

### 第四阶段：页面替换（0.5 天）

#### 1. 替换流程发起页面（核心）

**目标**：使用 FlyFlow 的拖拽式流程设计器替换现有的简陋页面

文件：`diom-frontend/src/views/Workflow/Start.vue`

**方案 A：完全替换**（推荐）

```vue
<template>
  <!-- 直接使用 FlyFlow 的 create.vue 页面 -->
  <FlyFlowCreate :flowKey="flowKey" @success="handleSuccess" />
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import FlyFlowCreate from '@/flyflow/views/flow/create.vue'

const route = useRoute()
const router = useRouter()
const flowKey = route.params.key

const handleSuccess = () => {
  router.push('/workflow/tasks')
}
</script>
```

**方案 B：保留现有布局，只替换核心组件**

```vue
<template>
  <div class="workflow-start-container">
    <el-page-header @back="goBack" title="返回" content="发起流程" />
    
    <!-- 使用 FlyFlow 的流程设计器组件 -->
    <FlyFlowDesigner 
      v-model="formData"
      :flowKey="flowKey"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup>
import FlyFlowDesigner from '@/flyflow/components/FlowDesigner/index.vue'
import { flowApi } from '@/flyflow/api/adapter'
// ... 其他逻辑
</script>
```

#### 2. 替换待办任务页面

文件：`diom-frontend/src/views/Workflow/Tasks.vue`

```vue
<template>
  <!-- 使用 FlyFlow 的待办任务页面 -->
  <FlyFlowPendingTasks />
</template>

<script setup>
import FlyFlowPendingTasks from '@/flyflow/views/task/pending.vue'
</script>
```

#### 3. 替换我发起的流程页面

文件：`diom-frontend/src/views/Workflow/Instances.vue`

```vue
<template>
  <!-- 使用 FlyFlow 的我发起的页面 -->
  <FlyFlowStartedTasks />
</template>

<script setup>
import FlyFlowStartedTasks from '@/flyflow/views/task/started.vue'
</script>
```

#### 4. 替换流程列表页面

文件：`diom-frontend/src/views/Workflow/List.vue`

```vue
<template>
  <!-- 使用 FlyFlow 的流程列表页面 -->
  <FlyFlowList />
</template>

<script setup>
import FlyFlowList from '@/flyflow/views/flow/list.vue'
</script>
```

---

### 第五阶段：样式和路由调整（0.5 天）

#### 1. 引入 FlyFlow 样式

文件：`diom-frontend/src/main.js`

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入 FlyFlow 样式
import '@/flyflow/css/workflow.css'
import '@/flyflow/css/dialog.css'
import '@/flyflow/assets/base.css'
import '@/flyflow/assets/main.css'

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.use(ElementPlus)

app.mount('#app')
```

#### 2. 配置路由别名（如果需要）

文件：`diom-frontend/vite.config.js`

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      '@flyflow': path.resolve(__dirname, 'src/flyflow')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/workflow': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 🔍 关键调整点

### 1. LogicFlow 流程引擎配置

FlyFlow 使用 **LogicFlow** 作为流程设计器引擎，需要了解其基本用法：

**核心组件位置**：
- `flyflow/components/FlowDesigner/` - 流程设计器主组件
- `flyflow/components/NodeConfig/` - 节点配置组件
- `flyflow/components/FormDesigner/` - 表单设计组件

**使用示例**：

```vue
<template>
  <div id="logic-flow-container" style="width: 100%; height: 600px;"></div>
</template>

<script setup>
import { onMounted } from 'vue'
import LogicFlow from '@logicflow/core'
import '@logicflow/core/dist/style/index.css'

onMounted(() => {
  const lf = new LogicFlow({
    container: document.querySelector('#logic-flow-container'),
    grid: true
  })
  
  // 加载流程数据
  lf.render(flowData)
})
</script>
```

### 2. API 响应格式转换

FlyFlow 的 API 响应格式：

```javascript
{
  code: '00000',  // 成功码
  data: { ... },
  msg: '操作成功'
}
```

你的 diom-flowable-service 响应格式（需要在适配器中转换）：

```javascript
{
  code: 200,      // HTTP 状态码
  data: { ... },
  message: 'Success'
}
```

### 3. 状态管理集成

FlyFlow 使用 Pinia 状态管理，需要在 `diom-frontend` 中注册：

文件：`diom-frontend/src/stores/flyflow.js`

```javascript
import { defineStore } from 'pinia'

export const useFlyFlowStore = defineStore('flyflow', {
  state: () => ({
    currentFlow: null,
    currentTask: null
  }),
  actions: {
    setCurrentFlow(flow) {
      this.currentFlow = flow
    },
    setCurrentTask(task) {
      this.currentTask = task
    }
  }
})
```

---

## 📋 迁移检查清单

### 准备阶段
- [ ] 安装 LogicFlow 依赖（`@logicflow/core`, `@logicflow/extension`）
- [ ] 安装其他必要依赖（`lodash-es`, `moment`, `nprogress`）
- [ ] 创建 `flyflow` 目录结构

### 复制阶段
- [ ] 复制 `components` 目录（约 118 个文件）
- [ ] 复制 `api` 目录
- [ ] 复制 `stores` 目录
- [ ] 复制 `utils` 目录
- [ ] 复制 `assets` 目录（图标、图片）
- [ ] 复制 `css` 目录（样式文件）
- [ ] 复制 `views` 目录（页面文件）

### API 适配阶段
- [ ] 创建 API 适配器 (`flyflow/api/adapter.js`)
- [ ] 实现 `flowApi.getFlowList`
- [ ] 实现 `flowApi.startFlow`
- [ ] 实现 `flowApi.getFlowDetail`
- [ ] 实现 `taskApi.getPendingTasks`
- [ ] 实现 `taskApi.completeTask`
- [ ] 实现 `taskApi.getStartedTasks`
- [ ] 批量替换 FlyFlow 组件中的 API 导入

### 页面替换阶段
- [ ] 替换流程发起页面 (`Start.vue`)
- [ ] 替换待办任务页面 (`Tasks.vue`)
- [ ] 替换我发起的页面 (`Instances.vue`)
- [ ] 替换流程列表页面 (`List.vue`)

### 样式和配置阶段
- [ ] 在 `main.js` 中引入 FlyFlow 样式
- [ ] 配置 Vite 路由别名
- [ ] 配置代理（如果需要）

### 测试阶段
- [ ] 测试流程列表加载
- [ ] 测试流程发起（拖拽设计器）
- [ ] 测试待办任务查询
- [ ] 测试任务审批
- [ ] 测试我发起的流程查询
- [ ] 测试样式显示是否正常

---

## ⚠️ 注意事项

### 1. TypeScript vs JavaScript

FlyFlow-Vue3 使用 TypeScript，而 diom-frontend 使用 JavaScript。

**解决方案**：
- 保留 `.ts` 文件，Vite 会自动处理
- 或者将 `.ts` 重命名为 `.js`，删除类型注解

### 2. 组件导入路径

FlyFlow 组件中的导入路径可能需要调整：

```javascript
// 原来
import SomeComponent from '@/components/SomeComponent.vue'

// 修改为
import SomeComponent from '@/flyflow/components/SomeComponent.vue'
```

### 3. Element Plus 版本兼容

- FlyFlow: Element Plus 2.4.3
- diom-frontend: Element Plus 2.11.8

**建议**：保持使用 diom-frontend 的版本（2.11.8），向后兼容

### 4. 图标资源

FlyFlow 使用了大量自定义 SVG 图标，确保复制了 `assets/icons/` 目录

---

## 🚀 快速开始命令

### 一键复制所有资源

```bash
#!/bin/bash
# 文件：copy-flyflow.sh

cd /Users/yanchao/IdeaProjects/diom-workFlow

SOURCE_DIR="flyflow-vue3-master/src/views/flyflow"
TARGET_DIR="diom-frontend/src/flyflow"

echo "开始复制 FlyFlow 资源..."

# 创建目标目录
mkdir -p "$TARGET_DIR"

# 复制所有资源
cp -r "$SOURCE_DIR/components" "$TARGET_DIR/"
cp -r "$SOURCE_DIR/api" "$TARGET_DIR/"
cp -r "$SOURCE_DIR/stores" "$TARGET_DIR/"
cp -r "$SOURCE_DIR/utils" "$TARGET_DIR/"
cp -r "$SOURCE_DIR/assets" "$TARGET_DIR/"
cp -r "$SOURCE_DIR/css" "$TARGET_DIR/"
cp -r "$SOURCE_DIR/views" "$TARGET_DIR/"

echo "✅ 复制完成！"
echo "文件统计："
find "$TARGET_DIR" -type f | wc -l
```

执行：

```bash
chmod +x copy-flyflow.sh
./copy-flyflow.sh
```

---

## 📊 预期效果

### 替换前 vs 替换后

| 功能 | 替换前 | 替换后 |
|-----|--------|--------|
| **流程发起** | 简单表单 | ✅ **拖拽式流程设计器**（钉钉风格） |
| **任务列表** | 基础列表 | ✅ **卡片式任务列表**（更直观） |
| **流程跟踪** | 无可视化 | ✅ **流程图可视化**（实时状态） |
| **表单设计** | 无 | ✅ **拖拽式表单设计器** |
| **审批交互** | 简单按钮 | ✅ **审批意见、附件、签名** |

---

## 🎉 总结

**这个方案的优势**：

1. ✅ **保持现有路由和菜单结构**（不破坏用户习惯）
2. ✅ **只替换页面组件**（API 适配工作量小）
3. ✅ **渐进式迁移**（可以逐个页面替换，风险可控）
4. ✅ **获得 FlyFlow 的优秀交互**（拖拽设计器、钉钉风格）
5. ✅ **工作量可控**（2-3 天完成）

**下一步**：
1. 执行复制命令（`copy-flyflow.sh`）
2. 安装依赖（`npm install @logicflow/core @logicflow/extension`）
3. 创建 API 适配器（`flyflow/api/adapter.js`）
4. 逐个替换页面组件
5. 测试功能

**现在就开始吧！** 🚀

