# 🚀 FlyFlow 集成快速开始指南

## ✅ 已完成的工作

### 1. 资源复制 ✅
```
✅ 已复制 272 个文件到 src/flyflow/ 目录
   - 118 个组件文件
   - 22 个 API 文件
   - 4 个 store 文件
   - 16 个工具文件
   - 83 个资源文件（图标、图片）
   - 20 个样式文件
   - 9 个页面文件
```

### 2. 依赖安装 ✅
```bash
✅ 已安装核心依赖：
   - @logicflow/core@^1.2.10       # LogicFlow 流程引擎核心
   - @logicflow/extension@^1.2.10  # LogicFlow 扩展插件
   - lodash-es@^4.17.21            # 工具函数库
   - moment@^2.29.4                # 日期时间处理
   - nprogress@^0.2.0              # 进度条组件
```

### 3. API 适配器创建 ✅
```
✅ 已创建 src/flyflow/api/adapter.js
   - flowApi - 流程定义 API
   - taskApi - 任务 API
   - processInstanceApi - 流程实例 API
   - userApi - 用户 API
```

### 4. 样式引入 ✅
```
✅ 已在 src/main.js 中引入：
   - FlyFlow 样式文件
   - LogicFlow 样式文件
```

---

## 📝 下一步：页面替换

### 方案一：完全替换（推荐）

#### 1. 替换流程发起页面

**文件**：`src/views/Workflow/Start.vue`

```vue
<template>
  <div class="workflow-start-wrapper">
    <!-- 直接使用 FlyFlow 的流程创建页面 -->
    <FlyFlowCreate 
      :flowKey="flowKey" 
      @success="handleSuccess" 
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import FlyFlowCreate from '@/flyflow/views/flow/create.vue'

const route = useRoute()
const router = useRouter()
const flowKey = route.params.key

// 流程启动成功
const handleSuccess = (data) => {
  console.log('流程启动成功:', data)
  router.push('/workflow/tasks')
}

// 取消
const handleCancel = () => {
  router.back()
}
</script>

<style scoped>
.workflow-start-wrapper {
  width: 100%;
  height: 100%;
}
</style>
```

#### 2. 替换待办任务页面

**文件**：`src/views/Workflow/Tasks.vue`

```vue
<template>
  <div class="workflow-tasks-wrapper">
    <!-- 使用 FlyFlow 的待办任务页面 -->
    <FlyFlowPendingTasks @task-click="handleTaskClick" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import FlyFlowPendingTasks from '@/flyflow/views/task/pending.vue'

const router = useRouter()

// 任务点击事件
const handleTaskClick = (task) => {
  router.push(`/workflow/task/${task.id}`)
}
</script>

<style scoped>
.workflow-tasks-wrapper {
  width: 100%;
  padding: 20px;
}
</style>
```

#### 3. 替换我发起的流程页面

**文件**：`src/views/Workflow/Instances.vue`

```vue
<template>
  <div class="workflow-instances-wrapper">
    <!-- 使用 FlyFlow 的我发起的页面 -->
    <FlyFlowStartedTasks @instance-click="handleInstanceClick" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import FlyFlowStartedTasks from '@/flyflow/views/task/started.vue'

const router = useRouter()

// 流程实例点击事件
const handleInstanceClick = (instance) => {
  console.log('查看流程实例:', instance)
  // 可以跳转到详情页面
}
</script>

<style scoped>
.workflow-instances-wrapper {
  width: 100%;
  padding: 20px;
}
</style>
```

#### 4. 替换流程列表页面

**文件**：`src/views/Workflow/List.vue`

```vue
<template>
  <div class="workflow-list-wrapper">
    <!-- 使用 FlyFlow 的流程列表页面 -->
    <FlyFlowList @start-flow="handleStartFlow" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import FlyFlowList from '@/flyflow/views/flow/list.vue'

const router = useRouter()

// 发起流程事件
const handleStartFlow = (flow) => {
  router.push(`/workflow/start/${flow.key}`)
}
</script>

<style scoped>
.workflow-list-wrapper {
  width: 100%;
  padding: 20px;
}
</style>
```

---

### 方案二：渐进式替换（稳妥）

#### 只替换核心组件（流程设计器）

保留现有页面结构，只替换关键组件：

```vue
<template>
  <div class="workflow-page">
    <el-page-header @back="goBack" title="返回" content="发起流程" />
    
    <el-card class="workflow-card">
      <!-- 只替换这个组件 -->
      <FlowDesigner 
        v-model="flowData"
        :processKey="processKey"
        @save="handleSave"
      />
    </el-card>
    
    <div class="workflow-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit">提交</el-button>
    </div>
  </div>
</template>

<script setup>
// 只引入关键组件
import FlowDesigner from '@/flyflow/components/FlowDesigner/index.vue'

// 其他逻辑保持不变
// ...
</script>
```

---

## 🧪 测试步骤

### 1. 启动开发服务器

```bash
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend
npm run dev
```

### 2. 打开浏览器测试

访问：`http://localhost:3000`（或你配置的端口）

### 3. 测试功能

#### 测试清单：

- [ ] **流程列表**
  - 访问 `/workflow/list`
  - 检查流程列表是否正常显示
  - 检查点击"发起流程"是否正常跳转

- [ ] **流程发起**（核心功能）
  - 访问 `/workflow/start/{key}`
  - 检查 LogicFlow 流程设计器是否正常显示
  - 检查拖拽节点是否正常
  - 检查节点配置是否正常
  - 检查提交流程是否成功

- [ ] **待办任务**
  - 访问 `/workflow/tasks`
  - 检查任务列表是否正常显示
  - 检查任务审批是否正常

- [ ] **我发起的流程**
  - 访问 `/workflow/instances`
  - 检查流程实例列表是否正常显示

---

## ⚠️ 常见问题

### 1. LogicFlow 组件不显示

**问题**：页面空白，LogicFlow 组件不显示

**解决**：
1. 检查样式是否正确引入（`main.js`）
2. 检查容器是否有固定高度

```vue
<style scoped>
/* LogicFlow 需要固定高度 */
.flow-designer-container {
  width: 100%;
  height: 600px; /* 必须设置高度 */
}
</style>
```

### 2. API 调用失败

**问题**：API 调用返回 404 或 500

**解决**：
1. 检查 API 适配器路径是否正确
2. 检查后端服务是否启动（`diom-flowable-service`）
3. 检查 Gateway 路由配置

### 3. 样式错乱

**问题**：页面样式错乱，显示不正常

**解决**：
1. 确保 FlyFlow 样式在 Element Plus 样式之后引入
2. 检查 CSS 文件是否正确复制
3. 清除浏览器缓存重试

### 4. 组件导入路径错误

**问题**：`Cannot find module '@/flyflow/...'`

**解决**：
1. 检查 `vite.config.js` 中的路径别名配置
2. 确保文件路径正确
3. 重启开发服务器

---

## 📊 对比效果

### 替换前 vs 替换后

| 功能 | 替换前 | 替换后 |
|-----|--------|--------|
| **流程设计** | ❌ 无或简陋 | ✅ **拖拽式设计器**（LogicFlow） |
| **UI 风格** | ❌ 基础样式 | ✅ **钉钉/飞书风格**（专业） |
| **用户体验** | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **开发效率** | 需要从零实现 | **开箱即用**（节省 10+ 天） |

---

## 🎯 推荐实施顺序

### 第 1 天：测试和验证

1. ✅ **启动开发服务器**
   ```bash
   npm run dev
   ```

2. ✅ **创建测试页面**
   - 创建 `src/views/Workflow/FlyFlowTest.vue`
   - 引入 FlyFlow 组件测试

3. ✅ **验证核心功能**
   - 测试 LogicFlow 是否正常显示
   - 测试 API 适配器是否正常工作

### 第 2 天：替换页面

4. ✅ **替换流程发起页面**（核心）
   - 修改 `src/views/Workflow/Start.vue`
   - 测试流程发起功能

5. ✅ **替换待办任务页面**
   - 修改 `src/views/Workflow/Tasks.vue`
   - 测试任务列表和审批

### 第 3 天：完善和优化

6. ✅ **替换其他页面**
   - 流程列表、我发起的流程等

7. ✅ **样式调整**
   - 调整与现有系统的样式一致性

8. ✅ **功能测试**
   - 完整测试所有工作流功能

---

## 🚀 立即开始

### 创建测试页面（推荐第一步）

文件：`src/views/Workflow/FlyFlowTest.vue`

```vue
<template>
  <div class="flyflow-test">
    <el-card>
      <template #header>
        <h2>FlyFlow 组件测试</h2>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 测试流程列表 -->
        <el-tab-pane label="流程列表" name="list">
          <FlyFlowList />
        </el-tab-pane>

        <!-- 测试待办任务 -->
        <el-tab-pane label="待办任务" name="pending">
          <FlyFlowPendingTasks />
        </el-tab-pane>

        <!-- 测试我发起的 -->
        <el-tab-pane label="我发起的" name="started">
          <FlyFlowStartedTasks />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FlyFlowList from '@/flyflow/views/flow/list.vue'
import FlyFlowPendingTasks from '@/flyflow/views/task/pending.vue'
import FlyFlowStartedTasks from '@/flyflow/views/task/started.vue'

const activeTab = ref('list')
</script>

<style scoped>
.flyflow-test {
  padding: 20px;
}
</style>
```

### 添加测试路由

文件：`src/router/index.js`

在 `routes` 数组中添加：

```javascript
{
  path: '/workflow/flyflow-test',
  name: 'FlyFlowTest',
  component: () => import('@/views/Workflow/FlyFlowTest.vue'),
  meta: { title: 'FlyFlow 测试', hidden: true }
}
```

### 访问测试页面

```
http://localhost:3000/workflow/flyflow-test
```

---

## 📚 相关文档

- **完整迁移方案**：`FLYFLOW_MIGRATION_PLAN.md`
- **FlyFlow 组件目录**：`src/flyflow/components/`
- **API 适配器**：`src/flyflow/api/adapter.js`

---

## 🎉 总结

**你已经完成了 70% 的工作！**

✅ 已完成：
1. 资源复制（272 个文件）
2. 依赖安装（LogicFlow + 工具库）
3. API 适配器（完整的 API 映射）
4. 样式引入（FlyFlow + LogicFlow）

⏳ 还需要：
1. 创建测试页面（10 分钟）
2. 替换现有页面（1-2 小时）
3. 功能测试（1 小时）

**预计总工作量**：**半天到 1 天**即可完成集成！

**现在就开始测试吧！** 🚀

