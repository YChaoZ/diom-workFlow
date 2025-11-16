# 流程设计器优化 - 快速开始指南

## 🎯 一句话总结

**当前问题**: 设计器无法配置`assignee`等属性，必须手动编辑SQL ❌  
**优化目标**: 集成Camunda属性面板，所有配置通过UI完成 ✅  
**核心工作**: 安装2个NPM包 + 修改1个Vue文件 = 完成核心功能

---

## ⚡ 紧急修复方案（2-3天完成）

### 步骤1: 安装NPM依赖（5分钟）

```bash
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend

# 安装属性面板
npm install bpmn-js-properties-panel@1.22.2

# 安装Camunda模块定义
npm install camunda-bpmn-moddle@7.0.1
```

---

### 步骤2: 修改ProcessDesigner.vue（2小时）

**文件位置**: `diom-frontend/src/views/Workflow/ProcessDesigner.vue`

#### 2.1 添加导入语句

在文件顶部添加：

```javascript
// 在现有的bpmn-js导入后添加
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule  // ⭐ 关键：Camunda属性
} from 'bpmn-js-properties-panel'

// 添加样式导入
import 'bpmn-js-properties-panel/dist/assets/properties-panel.css'
```

#### 2.2 更新modeler初始化代码

找到 `initBpmnModeler()` 函数，修改为：

```javascript
const initBpmnModeler = () => {
  modeler = new BpmnModeler({
    container: bpmnCanvas.value,
    propertiesPanel: {
      parent: propertiesPanel.value  // ⭐ 添加这行：指定属性面板容器
    },
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule,
      CamundaPlatformPropertiesProviderModule  // ⭐ 添加这行：Camunda属性
    ],
    moddleExtensions: {
      camunda: CamundaBpmnModdle
    },
    keyboard: {
      bindTo: document
    }
  })
  
  // 原有代码保持不变...
}
```

#### 2.3 调整CSS样式（可选）

在 `<style scoped>` 中添加：

```css
/* 属性面板样式优化 */
:deep(.bio-properties-panel) {
  background: #fafafa;
  font-size: 13px;
}

:deep(.bio-properties-panel-header) {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 10px;
  font-weight: 600;
}

:deep(.bio-properties-panel-group-header) {
  background: #f5f5f5;
  padding: 8px 10px;
  font-weight: 500;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.bio-properties-panel-entry) {
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.bio-properties-panel-input) {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
}

:deep(.bio-properties-panel-input:focus) {
  border-color: #409eff;
  outline: none;
}
```

---

### 步骤3: 重新编译并测试（15分钟）

```bash
# 重新编译前端
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend
npm run dev

# 打开浏览器访问
# http://localhost:3000/workflow/design/list
```

---

## ✅ 验收标准

完成后，您应该看到：

### 1. 右侧属性面板出现

```
┌────────────────────────────────────────┐
│ Process Designer                       │
├──────┬─────────────────┬───────────────┤
│ 工具 │   BPMN画布      │  属性面板 ⬅️  │
│ 栏   │                 │               │
│      │  [开始] → [任务] │  General      │
│      │     ↓          │  ────────     │
│      │  [结束]        │  ID: Task_1   │
│      │                 │  Name: [任务名]│
│      │                 │               │
│      │                 │  Assignee ⭐  │
│      │                 │  ────────     │
│      │                 │  Type:        │
│      │                 │  ○ Assignee   │
│      │                 │  ○ Candidate  │
│      │                 │    Users      │
│      │                 │               │
│      │                 │  Assignee:    │
│      │                 │  [manager]    │
└──────┴─────────────────┴───────────────┘
```

### 2. 点击任意BPMN元素

- ✅ 右侧显示该元素的属性
- ✅ 可以编辑ID、Name
- ✅ **用户任务显示Assignee配置** ⭐

### 3. 配置用户任务的assignee

1. 点击画布上的用户任务
2. 右侧找到 "Assignee" 部分
3. 输入框中输入 `manager`
4. 保存草稿
5. 点击发布

**预期结果**: 
- ✅ 发布成功（不再报错）
- ✅ 无需手动编辑SQL
- ✅ BPMN XML自动包含 `camunda:assignee="manager"`

---

## 🔍 故障排查

### 问题1: 属性面板不显示

**检查项**:
```javascript
// 1. 确认导入了所有必要的模块
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule  // ⬅️ 这个最重要
} from 'bpmn-js-properties-panel'

// 2. 确认modeler配置中添加了属性面板
propertiesPanel: {
  parent: propertiesPanel.value  // ⬅️ 确保这个ref存在
}

// 3. 确认additionalModules包含了Camunda提供器
additionalModules: [
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule  // ⬅️ 必须有这个
]
```

**解决方法**:
- 查看浏览器控制台是否有错误
- 检查 `propertiesPanel.value` 是否为有效的DOM元素
- 确认NPM包安装成功 (`node_modules/bpmn-js-properties-panel`存在)

---

### 问题2: 看不到Assignee字段

**检查项**:
```javascript
// 确认Camunda moddle扩展已加载
moddleExtensions: {
  camunda: CamundaBpmnModdle  // ⬅️ 必须有这个
}

// 确认导入了CamundaBpmnModdle
import CamundaBpmnModdle from 'camunda-bpmn-moddle/resources/camunda.json'
```

**解决方法**:
- 点击的必须是**用户任务**（User Task），不是其他任务类型
- 检查NPM包 `camunda-bpmn-moddle` 是否安装成功

---

### 问题3: 编译失败

**常见错误**:
```
Module not found: Error: Can't resolve 'bpmn-js-properties-panel'
```

**解决方法**:
```bash
# 1. 清除缓存
rm -rf node_modules package-lock.json

# 2. 重新安装
npm install

# 3. 单独安装缺失的包
npm install bpmn-js-properties-panel@1.22.2
npm install camunda-bpmn-moddle@7.0.1
```

---

## 📸 效果预览

### 修复前 ❌

```
用户操作流程：
1. 在UI中拖拽创建用户任务 ✅
2. 无法配置assignee ❌
3. 发布失败："用户任务缺少assignee属性" ❌
4. 必须手动执行SQL更新BPMN XML ❌
5. 再次尝试发布 ✅

问题：步骤3-4用户体验极差，不适合生产环境
```

### 修复后 ✅

```
用户操作流程：
1. 在UI中拖拽创建用户任务 ✅
2. 点击任务，右侧显示属性面板 ✅
3. 在Assignee字段输入 "manager" ✅
4. 点击保存草稿 ✅
5. 点击发布 ✅

结果：所有操作在UI完成，零SQL编辑
```

---

## 🎯 关键属性说明

### Assignee（任务分配人）⭐⭐⭐⭐⭐

**显示位置**: 属性面板 → Assignee 部分

**配置选项**:

#### 1. Assignee（固定用户）
```
输入框: [manager]
效果: 任务固定分配给manager用户
BPMN: camunda:assignee="manager"
```

#### 2. Assignee Expression（动态表达式）
```
输入框: [${applicant}]
效果: 从流程变量applicant获取用户名
BPMN: camunda:assignee="${applicant}"
```

#### 3. Candidate Users（候选用户）
```
输入框: [user1, user2, user3]
效果: 任务可由三个用户中任何一个认领
BPMN: camunda:candidateUsers="user1,user2,user3"
```

#### 4. Candidate Groups（候选用户组）
```
输入框: [managers, hr]
效果: 任务可由managers组或hr组的任何人认领
BPMN: camunda:candidateGroups="managers,hr"
```

---

### Form Key（表单关联）⭐⭐⭐

**显示位置**: 属性面板 → Forms 部分

```
输入框: [embedded:app:forms/leave-form.html]
效果: 关联自定义表单页面
BPMN: camunda:formKey="embedded:app:forms/leave-form.html"
```

---

### Task Listeners（任务监听器）⭐⭐⭐⭐

**显示位置**: 属性面板 → Listeners 部分

**用途**: 在任务生命周期的特定时刻执行自定义逻辑

```
Event Type: create (任务创建时)
Listener Type: Java Class
Java Class: com.diom.workflow.listener.NotificationListener
效果: 任务创建时发送通知
```

---

## 🚀 下一步优化（可选）

完成上述核心功能后，可以考虑：

### 1. 启用元素连接功能（4小时）
- 目前：可以拖拽元素，但无法连接
- 优化后：可以拖拽连接线，形成完整流程

### 2. 添加键盘快捷键（2小时）
- Ctrl+C/V：复制粘贴
- Delete：删除元素
- Ctrl+Z/Y：撤销重做

### 3. 实时验证（6小时）
- 红色边框标记错误元素
- 悬停显示错误原因
- 智能修复建议

---

## 📞 需要帮助？

如果遇到问题，请提供：
1. 浏览器控制台错误截图
2. `package.json` 中的依赖版本
3. `ProcessDesigner.vue` 的initBpmnModeler函数代码

---

**预计完成时间**: 2-3小时（不含测试）  
**验收标准**: 能在UI中配置assignee并成功发布流程  
**文档版本**: v1.0  
**创建日期**: 2025-11-16

