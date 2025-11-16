# 流程设计器完整版优化 - 工作计划（方案B）

## 📋 项目概述

**项目名称**: 流程设计器完整版优化  
**目标**: 打造企业级BPMN流程设计器，支持所有Camunda属性配置和高级功能  
**预计总工时**: 56-72小时  
**预计工期**: 7-9个工作日  
**开始日期**: 待定  
**负责人**: 待定

---

## 🎯 总体目标

将流程设计器从**基础拖拽工具**升级为**企业级流程建模平台**：

### 核心能力
1. ✅ 完整的BPMN 2.0元素支持
2. ✅ 所有Camunda属性可视化配置
3. ✅ 表单设计与集成
4. ✅ 实时验证与智能提示
5. ✅ 流程模拟运行
6. ✅ 流程模板库
7. ✅ 多人协作功能

### 质量标准
- 🎯 零SQL/XML手动编辑
- 🎯 业务人员可独立建模
- 🎯 错误率降低80%以上
- 🎯 建模效率提升5倍以上

---

## 📅 工作阶段划分

### 阶段1: 核心功能（必须完成）⭐⭐⭐⭐⭐
**工时**: 20-26小时 | **工期**: 2.5-3.5天  
**目标**: 实现基本可用的流程设计器

### 阶段2: 增强功能（重要功能）⭐⭐⭐⭐
**工时**: 20-26小时 | **工期**: 2.5-3.5天  
**目标**: 提升用户体验和系统能力

### 阶段3: 高级功能（锦上添花）⭐⭐⭐
**工时**: 16-20小时 | **工期**: 2-2.5天  
**目标**: 达到企业级标准

---

## 🔧 阶段1: 核心功能（P0）

### 任务1.1: 集成Camunda属性面板 ⭐⭐⭐⭐⭐
**优先级**: P0（最高）  
**工时**: 8-10小时  
**负责人**: 前端开发  
**依赖**: 无

#### 子任务

##### 1.1.1 安装NPM依赖（15分钟）
```bash
cd diom-frontend
npm install bpmn-js-properties-panel@1.22.2
npm install camunda-bpmn-moddle@7.0.1
npm install camunda-bpmn-js-behaviors@0.5.0
```

**验收标准**:
- [ ] `package.json`中包含上述3个依赖
- [ ] `npm list`可以看到依赖版本
- [ ] 无版本冲突警告

---

##### 1.1.2 修改ProcessDesigner.vue - 导入模块（30分钟）

**文件**: `diom-frontend/src/views/Workflow/ProcessDesigner.vue`

```javascript
// ===== 在<script setup>顶部添加导入 =====

// BPMN属性面板相关
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule
} from 'bpmn-js-properties-panel'

// Camunda模块定义
import CamundaBpmnModdle from 'camunda-bpmn-moddle/resources/camunda.json'

// 属性面板样式
import 'bpmn-js-properties-panel/dist/assets/properties-panel.css'

// Camunda行为模块（可选，用于高级功能）
import CamundaBehaviorsModule from 'camunda-bpmn-js-behaviors/lib/camunda-platform'
```

**验收标准**:
- [ ] 编译无错误
- [ ] 浏览器控制台无导入错误

---

##### 1.1.3 修改ProcessDesigner.vue - 更新modeler初始化（1小时）

**找到`initBpmnModeler()`函数，完整替换为**:

```javascript
const initBpmnModeler = () => {
  modeler = new BpmnModeler({
    container: bpmnCanvas.value,
    // ===== 新增：属性面板配置 =====
    propertiesPanel: {
      parent: propertiesPanel.value
    },
    // ===== 新增：添加属性面板模块 =====
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule,
      CamundaPlatformPropertiesProviderModule,
      CamundaBehaviorsModule  // 可选
    ],
    // ===== 新增：Camunda模块定义 =====
    moddleExtensions: {
      camunda: CamundaBpmnModdle
    },
    keyboard: {
      bindTo: document
    }
  })
  
  // 监听流程变化
  modeler.on('commandStack.changed', () => {
    // 可以在这里添加自动保存逻辑
  })
  
  // ===== 新增：隐藏默认Palette（因为我们有自定义Toolbar） =====
  const canvas = modeler.get('canvas')
  const paletteContainer = canvas._container.parentNode.querySelector('.djs-palette')
  if (paletteContainer) {
    paletteContainer.style.display = 'none'
  }
}
```

**验收标准**:
- [ ] 编译成功
- [ ] 打开设计器页面无错误
- [ ] 右侧属性面板出现

---

##### 1.1.4 优化属性面板CSS样式（2小时）

**在`<style scoped>`中添加**:

```css
/* ===== 属性面板容器样式 ===== */
.designer-properties {
  width: 320px;  /* 增加宽度以显示更多内容 */
  background: #fff;
  overflow-y: auto;
  border-left: 1px solid #e4e7ed;
}

.properties-container {
  width: 100%;
  height: 100%;
}

/* ===== 属性面板整体样式 ===== */
:deep(.bio-properties-panel) {
  background: #fafafa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 13px;
  color: #333;
}

/* ===== 属性面板头部 ===== */
:deep(.bio-properties-panel-header) {
  background: #fff;
  border-bottom: 2px solid #409eff;
  padding: 12px 15px;
  font-weight: 600;
  font-size: 14px;
  color: #409eff;
  position: sticky;
  top: 0;
  z-index: 10;
}

/* ===== 属性分组标题 ===== */
:deep(.bio-properties-panel-group-header) {
  background: #f5f7fa;
  padding: 10px 15px;
  font-weight: 600;
  font-size: 13px;
  border-top: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  color: #606266;
  cursor: pointer;
  transition: background-color 0.2s;
}

:deep(.bio-properties-panel-group-header:hover) {
  background: #ecf5ff;
}

/* ===== 属性条目 ===== */
:deep(.bio-properties-panel-entry) {
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

:deep(.bio-properties-panel-entry:hover) {
  background: #f9f9f9;
}

/* ===== 属性标签 ===== */
:deep(.bio-properties-panel-label) {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #606266;
}

/* ===== 输入框样式 ===== */
:deep(.bio-properties-panel-input),
:deep(.bio-properties-panel-textarea) {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  background: #fff;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

:deep(.bio-properties-panel-input:focus),
:deep(.bio-properties-panel-textarea:focus) {
  border-color: #409eff;
  outline: none;
  background: #fff;
}

:deep(.bio-properties-panel-input:disabled),
:deep(.bio-properties-panel-textarea:disabled) {
  background: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

/* ===== 下拉选择框样式 ===== */
:deep(.bio-properties-panel-select) {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  background: #fff;
  cursor: pointer;
}

:deep(.bio-properties-panel-select:focus) {
  border-color: #409eff;
  outline: none;
}

/* ===== 复选框样式 ===== */
:deep(.bio-properties-panel-checkbox) {
  margin-right: 6px;
  cursor: pointer;
}

/* ===== 按钮样式 ===== */
:deep(.bio-properties-panel-button) {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: #606266;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.bio-properties-panel-button:hover) {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

/* ===== 列表样式 ===== */
:deep(.bio-properties-panel-list) {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

:deep(.bio-properties-panel-list-entry) {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  cursor: pointer;
  transition: background-color 0.2s;
}

:deep(.bio-properties-panel-list-entry:hover) {
  background: #f5f7fa;
}

:deep(.bio-properties-panel-list-entry:last-child) {
  border-bottom: none;
}

/* ===== 错误提示样式 ===== */
:deep(.bio-properties-panel-error) {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
}

/* ===== 帮助提示样式 ===== */
:deep(.bio-properties-panel-description) {
  color: #909399;
  font-size: 11px;
  margin-top: 4px;
  line-height: 1.4;
}

/* ===== 折叠/展开图标 ===== */
:deep(.bio-properties-panel-group-header-icon) {
  float: right;
  transition: transform 0.2s;
}

:deep(.bio-properties-panel-group--collapsed .bio-properties-panel-group-header-icon) {
  transform: rotate(-90deg);
}

/* ===== 滚动条样式 ===== */
.designer-properties::-webkit-scrollbar {
  width: 8px;
}

.designer-properties::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.designer-properties::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.designer-properties::-webkit-scrollbar-track {
  background: #f1f1f1;
}
```

**验收标准**:
- [ ] 属性面板样式美观
- [ ] 与Element Plus风格一致
- [ ] 输入框、按钮交互正常

---

##### 1.1.5 测试属性面板功能（2小时）

**测试清单**:

1. **用户任务属性测试**
   - [ ] 点击用户任务，右侧显示属性
   - [ ] 可以配置Assignee（固定用户）
   - [ ] 可以配置Assignee Expression（表达式）
   - [ ] 可以配置Candidate Users
   - [ ] 可以配置Candidate Groups
   - [ ] 可以配置Due Date
   - [ ] 可以配置Priority

2. **服务任务属性测试**
   - [ ] 可以配置Java Class
   - [ ] 可以配置Expression
   - [ ] 可以配置Delegate Expression
   - [ ] 可以配置External Task

3. **网关属性测试**
   - [ ] 可以配置Default Flow
   - [ ] 可以配置Condition Expression

4. **流程属性测试**
   - [ ] 可以配置流程ID
   - [ ] 可以配置流程名称
   - [ ] 可以配置Executable
   - [ ] 可以配置Candidate Starter Groups

5. **保存与发布测试**
   - [ ] 配置属性后保存草稿成功
   - [ ] 草稿中包含配置的属性
   - [ ] 发布流程成功
   - [ ] 发布后BPMN XML包含Camunda属性

**测试脚本**:
```javascript
// 在浏览器控制台执行
const modeler = window.modeler  // 需要将modeler暴露到window
const elementRegistry = modeler.get('elementRegistry')
const modeling = modeler.get('modeling')

// 获取所有用户任务
const userTasks = elementRegistry.filter(el => el.type === 'bpmn:UserTask')
console.log('用户任务列表:', userTasks)

// 检查第一个用户任务的属性
const task = userTasks[0]
console.log('任务属性:', task.businessObject)
console.log('Assignee:', task.businessObject.assignee)
console.log('Candidate Users:', task.businessObject.candidateUsers)
```

**验收标准**:
- [ ] 所有测试项通过
- [ ] 无JavaScript错误
- [ ] 属性配置可正确保存到数据库

---

### 任务1.2: 启用元素连接功能 ⭐⭐⭐⭐
**优先级**: P0  
**工时**: 4-6小时  
**负责人**: 前端开发  
**依赖**: 任务1.1完成

#### 子任务

##### 1.2.1 启用Context Pad模块（1小时）

**修改`ProcessDesigner.vue`的`initBpmnModeler()`**:

```javascript
// 导入Context Pad模块
import ContextPadModule from 'diagram-js/lib/features/context-pad'

// 在additionalModules中添加
modeler = new BpmnModeler({
  // ... 其他配置
  additionalModules: [
    BpmnPropertiesPanelModule,
    BpmnPropertiesProviderModule,
    CamundaPlatformPropertiesProviderModule,
    ContextPadModule  // ⭐ 新增：启用元素周围的快捷菜单
  ]
})
```

**验收标准**:
- [ ] 点击元素时，周围出现操作菜单
- [ ] 可以通过菜单添加连接线
- [ ] 可以通过菜单删除元素

---

##### 1.2.2 添加连接工具到Toolbar（2小时）

**修改`Toolbar.vue`，添加工具类**:

```vue
<script setup>
// 在工具列表中添加连接工具
const tools = ref([
  {
    type: 'hand-tool',
    label: '手型工具',
    icon: '✋',
    color: '#67c23a',
    bpmnType: 'hand-tool'
  },
  {
    type: 'lasso-tool',
    label: '套索选择',
    icon: '⭕',
    color: '#409eff',
    bpmnType: 'lasso-tool'
  },
  {
    type: 'space-tool',
    label: '空间调整',
    icon: '↔️',
    color: '#909399',
    bpmnType: 'space-tool'
  },
  {
    type: 'create-connection',
    label: '连接工具',
    icon: '🔗',
    color: '#e6a23c',
    bpmnType: 'global-connect-tool'
  }
])
</script>

<template>
  <!-- 工具部分（在其他分类之前） -->
  <div class="toolbar-section">
    <div class="section-title">工具</div>
    <div 
      v-for="item in tools"
      :key="item.type"
      class="toolbar-item"
      @click="handleToolClick(item)"
      :title="item.label"
    >
      <div class="item-icon" :style="{ backgroundColor: item.color }">
        {{ item.icon }}
      </div>
      <div class="item-label">{{ item.label }}</div>
    </div>
  </div>
  
  <!-- 原有的事件、任务等分类 -->
</template>
```

**在`ProcessDesigner.vue`中处理工具点击**:

```javascript
// 处理工具栏工具点击
const handleToolClick = (tool) => {
  if (!modeler) return
  
  const globalConnect = modeler.get('globalConnect')
  const handTool = modeler.get('handTool')
  const lassoTool = modeler.get('lassoTool')
  const spaceTool = modeler.get('spaceTool')
  
  // 取消所有工具的激活状态
  globalConnect.toggle(false)
  handTool.toggle(false)
  lassoTool.toggle(false)
  spaceTool.toggle(false)
  
  // 激活选中的工具
  switch (tool.bpmnType) {
    case 'global-connect-tool':
      globalConnect.toggle()
      break
    case 'hand-tool':
      handTool.toggle()
      break
    case 'lasso-tool':
      lassoTool.toggle()
      break
    case 'space-tool':
      spaceTool.toggle()
      break
  }
}

// 在模板中监听工具点击事件
<Toolbar 
  @drag-start="handleToolbarDragStart"
  @tool-click="handleToolClick"  // ⭐ 新增
/>
```

**验收标准**:
- [ ] 点击连接工具后，鼠标变为连接模式
- [ ] 可以点击起始元素拖动到目标元素创建连接
- [ ] 连接线样式正常
- [ ] 其他工具（手型、套索）也能正常工作

---

##### 1.2.3 测试连接功能（1小时）

**测试清单**:
- [ ] 开始事件→用户任务（创建SequenceFlow）
- [ ] 用户任务→网关（创建SequenceFlow）
- [ ] 网关→结束事件（创建SequenceFlow）
- [ ] 任务→任务（创建SequenceFlow）
- [ ] 删除连接线
- [ ] 修改连接线的弯曲点
- [ ] 保存后重新打开，连接线保持

**验收标准**:
- [ ] 所有连接操作正常
- [ ] 连接线可以自动路由
- [ ] 保存后数据正确

---

### 任务1.3: 添加基本编辑功能 ⭐⭐⭐
**优先级**: P1  
**工时**: 4-6小时  
**负责人**: 前端开发  
**依赖**: 任务1.1、1.2完成

#### 子任务

##### 1.3.1 启用键盘快捷键（2小时）

```javascript
// 导入键盘模块
import KeyboardModule from 'diagram-js/lib/features/keyboard'
import KeyboardMoveSelectionModule from 'diagram-js/lib/features/keyboard-move-selection'

// 在additionalModules中添加
modeler = new BpmnModeler({
  additionalModules: [
    // ... 其他模块
    KeyboardModule,
    KeyboardMoveSelectionModule
  ],
  keyboard: {
    bindTo: document  // 绑定到整个文档
  }
})
```

**支持的快捷键**:
- `Delete` / `Backspace`: 删除选中元素
- `Ctrl+C`: 复制
- `Ctrl+V`: 粘贴
- `Ctrl+Z`: 撤销
- `Ctrl+Y` / `Ctrl+Shift+Z`: 重做
- `Ctrl+A`: 全选
- `方向键`: 移动选中元素
- `Ctrl+方向键`: 快速移动

**验收标准**:
- [ ] 所有快捷键功能正常
- [ ] 不与浏览器快捷键冲突
- [ ] 在输入框内不触发快捷键

---

##### 1.3.2 添加右键菜单（2小时）

```javascript
// 导入Pop-up Menu模块
import PopupMenuModule from 'diagram-js/lib/features/popup-menu'

// 自定义右键菜单
class CustomPopupMenuProvider {
  constructor(popupMenu, modeling, translate) {
    this._popupMenu = popupMenu
    this._modeling = modeling
    this._translate = translate
    
    popupMenu.registerProvider('bpmn-replace', this)
  }
  
  getPopupMenuEntries(element) {
    const modeling = this._modeling
    const translate = this._translate
    
    return {
      'delete': {
        label: translate('删除'),
        className: 'bpmn-icon-trash',
        action: () => {
          modeling.removeElements([element])
        }
      },
      'copy': {
        label: translate('复制'),
        className: 'bpmn-icon-copy',
        action: () => {
          // 复制逻辑
        }
      },
      'properties': {
        label: translate('属性'),
        className: 'bpmn-icon-properties',
        action: () => {
          // 打开属性面板
        }
      }
    }
  }
}

// 注册自定义菜单提供器
modeler = new BpmnModeler({
  additionalModules: [
    PopupMenuModule,
    {
      __init__: ['customPopupMenuProvider'],
      customPopupMenuProvider: ['type', CustomPopupMenuProvider]
    }
  ]
})
```

**验收标准**:
- [ ] 右键点击元素显示菜单
- [ ] 菜单项正常工作
- [ ] 菜单样式美观

---

##### 1.3.3 添加编辑工具栏（1小时）

**在ProcessDesigner.vue的顶部工具栏添加编辑按钮**:

```vue
<template>
  <div class="designer-toolbar">
    <div class="toolbar-left">
      <!-- 原有的流程信息输入 -->
      <el-input v-model="processName" placeholder="流程名称" />
      <!-- ... -->
    </div>
    
    <!-- ⭐ 新增：编辑工具栏 -->
    <div class="toolbar-center">
      <el-button-group>
        <el-button @click="handleUndo" :disabled="!canUndo">
          <el-icon><RefreshLeft /></el-icon> 撤销
        </el-button>
        <el-button @click="handleRedo" :disabled="!canRedo">
          <el-icon><RefreshRight /></el-icon> 重做
        </el-button>
      </el-button-group>
      
      <el-button-group style="margin-left: 10px">
        <el-button @click="handleCopy" :disabled="!hasSelection">
          <el-icon><DocumentCopy /></el-icon> 复制
        </el-button>
        <el-button @click="handlePaste" :disabled="!hasClipboard">
          <el-icon><DocumentAdd /></el-icon> 粘贴
        </el-button>
        <el-button @click="handleDelete" :disabled="!hasSelection">
          <el-icon><Delete /></el-icon> 删除
        </el-button>
      </el-button-group>
      
      <el-button-group style="margin-left: 10px">
        <el-button @click="handleZoomIn">
          <el-icon><ZoomIn /></el-icon> 放大
        </el-button>
        <el-button @click="handleZoomOut">
          <el-icon><ZoomOut /></el-icon> 缩小
        </el-button>
        <el-button @click="handleZoomReset">
          <el-icon><FullScreen /></el-icon> 适应
        </el-button>
      </el-button-group>
    </div>
    
    <div class="toolbar-right">
      <!-- 原有的操作按钮 -->
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { 
  RefreshLeft, RefreshRight, 
  DocumentCopy, DocumentAdd, Delete,
  ZoomIn, ZoomOut, FullScreen
} from '@element-plus/icons-vue'

const canUndo = ref(false)
const canRedo = ref(false)
const hasSelection = ref(false)
const hasClipboard = ref(false)

// 监听commandStack变化
modeler.on('commandStack.changed', () => {
  const commandStack = modeler.get('commandStack')
  canUndo.value = commandStack.canUndo()
  canRedo.value = commandStack.canRedo()
})

// 监听选择变化
modeler.on('selection.changed', (event) => {
  hasSelection.value = event.newSelection.length > 0
})

// 编辑操作
const handleUndo = () => {
  const commandStack = modeler.get('commandStack')
  commandStack.undo()
}

const handleRedo = () => {
  const commandStack = modeler.get('commandStack')
  commandStack.redo()
}

const handleCopy = () => {
  const clipboard = modeler.get('clipboard')
  const selection = modeler.get('selection')
  const copyPaste = modeler.get('copyPaste')
  
  copyPaste.copy(selection.get())
  hasClipboard.value = true
}

const handlePaste = () => {
  const copyPaste = modeler.get('copyPaste')
  copyPaste.paste()
}

const handleDelete = () => {
  const modeling = modeler.get('modeling')
  const selection = modeler.get('selection')
  
  modeling.removeElements(selection.get())
}

// 缩放操作
const handleZoomIn = () => {
  const zoomScroll = modeler.get('zoomScroll')
  zoomScroll.stepZoom(1)
}

const handleZoomOut = () => {
  const zoomScroll = modeler.get('zoomScroll')
  zoomScroll.stepZoom(-1)
}

const handleZoomReset = () => {
  const canvas = modeler.get('canvas')
  canvas.zoom('fit-viewport')
}
</script>
```

**验收标准**:
- [ ] 编辑按钮显示正常
- [ ] 按钮状态随操作变化
- [ ] 所有编辑操作正常工作

---

### 任务1.4: 优化UI交互体验 ⭐⭐⭐
**优先级**: P1  
**工时**: 4-6小时  
**负责人**: 前端开发  
**依赖**: 任务1.1、1.2、1.3完成

#### 子任务

##### 1.4.1 添加Mini地图（1小时）

```javascript
import MiniMapModule from 'diagram-js-minimap'

modeler = new BpmnModeler({
  additionalModules: [
    // ... 其他模块
    MiniMapModule
  ]
})
```

**验收标准**:
- [ ] 右下角显示Mini地图
- [ ] 可以通过Mini地图快速导航
- [ ] Mini地图样式美观

---

##### 1.4.2 添加网格背景（30分钟）

```css
:deep(.djs-container) {
  background-color: #fafafa;
  background-image: 
    linear-gradient(#e4e7ed 1px, transparent 1px),
    linear-gradient(90deg, #e4e7ed 1px, transparent 1px);
  background-size: 20px 20px;
}
```

**验收标准**:
- [ ] 画布显示网格背景
- [ ] 网格大小适中
- [ ] 不影响元素显示

---

##### 1.4.3 添加元素高亮效果（1小时）

```javascript
// 监听鼠标悬停
modeler.on('element.hover', (event) => {
  const canvas = modeler.get('canvas')
  canvas.addMarker(event.element.id, 'highlight')
})

modeler.on('element.out', (event) => {
  const canvas = modeler.get('canvas')
  canvas.removeMarker(event.element.id, 'highlight')
})
```

```css
:deep(.highlight) {
  stroke: #409eff !important;
  stroke-width: 3px !important;
}
```

**验收标准**:
- [ ] 鼠标悬停时元素高亮
- [ ] 高亮效果明显但不刺眼
- [ ] 移开鼠标后恢复正常

---

## 📊 阶段1总结

**预计完成时间**: 20-26小时（2.5-3.5天）

**交付物清单**:
- [x] ProcessDesigner.vue（完整修改）
- [x] Toolbar.vue（添加工具）
- [x] package.json（新增依赖）
- [x] 测试报告（功能验证）

**验收标准**:
- [ ] 属性面板完整显示并可配置所有Camunda属性
- [ ] 元素可以正常连接形成流程图
- [ ] 基本编辑功能（复制、删除、撤销等）正常
- [ ] UI交互流畅，用户体验良好
- [ ] 可以完全通过UI创建、配置、发布流程
- [ ] **零SQL/XML手动编辑**

---

## 🚀 阶段2: 增强功能（P1）

### 任务2.1: 表单设计器集成 ⭐⭐⭐⭐
**优先级**: P1  
**工时**: 12-16小时  
**负责人**: 前端+后端  
**依赖**: 阶段1完成

#### 子任务

##### 2.1.1 技术选型与调研（2小时）

**评估三个方案**:

**方案1: form-js（Camunda官方）**
- ✅ 原生支持Camunda
- ✅ 与bpmn-js集成度高
- ❌ 文档较少
- ❌ 定制性一般

**方案2: vue-form-making**
- ✅ 国产开源，文档完整
- ✅ Vue3兼容
- ✅ 组件丰富
- ❌ 需要适配Camunda

**方案3: 自研轻量级表单设计器**
- ✅ 完全可控
- ✅ 轻量级
- ❌ 开发工作量大

**推荐**: 方案2（vue-form-making）

**验收标准**:
- [ ] 完成技术评估文档
- [ ] 确定最终方案

---

##### 2.1.2 安装表单设计器（1小时）

```bash
npm install vue-form-making@3.0.0
```

---

##### 2.1.3 创建表单设计器组件（4小时）

**创建`FormDesigner.vue`**:

```vue
<template>
  <el-dialog 
    v-model="visible" 
    title="表单设计器" 
    width="90%"
    :close-on-click-modal="false"
  >
    <div class="form-designer-container">
      <fm-making-form 
        ref="makingForm"
        :fields="formFields"
        :config="formConfig"
        @save="handleSave"
      />
    </div>
    
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { FormMaking } from 'vue-form-making'
import 'vue-form-making/dist/index.css'

const visible = ref(false)
const formFields = ref([])
const formConfig = ref({})

const show = (taskId, existingForm) => {
  visible.value = true
  if (existingForm) {
    formFields.value = existingForm.fields
    formConfig.value = existingForm.config
  }
}

const handleSave = (data) => {
  console.log('表单数据:', data)
}

const handleConfirm = () => {
  const data = makingForm.value.getData()
  emit('save', data)
  visible.value = false
}

defineExpose({ show })
</script>
```

---

##### 2.1.4 集成到流程设计器（3小时）

**修改ProcessDesigner.vue**:

```vue
<template>
  <!-- 原有内容 -->
  
  <!-- 表单设计器组件 -->
  <FormDesigner 
    ref="formDesigner" 
    @save="handleFormSave"
  />
</template>

<script setup>
import FormDesigner from './FormDesigner.vue'

const formDesigner = ref(null)

// 在属性面板中添加"设计表单"按钮
const openFormDesigner = (taskId) => {
  formDesigner.value.show(taskId)
}

// 保存表单配置
const handleFormSave = (formData) => {
  // 将表单配置保存到用户任务的formKey
  const element = getSelectedElement()
  const modeling = modeler.get('modeling')
  
  modeling.updateProperties(element, {
    'camunda:formKey': `form:${formData.id}`
  })
  
  // 保存表单定义到后端
  saveFormDefinition(formData)
}
</script>
```

---

##### 2.1.5 后端API开发（4小时）

**创建表单相关表**:

```sql
-- 表单定义表
CREATE TABLE `form_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `form_key` varchar(100) NOT NULL COMMENT '表单Key',
  `form_name` varchar(100) NOT NULL COMMENT '表单名称',
  `form_fields` json NOT NULL COMMENT '表单字段定义(JSON)',
  `form_config` json COMMENT '表单配置(JSON)',
  `version` int(11) NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态:DRAFT-草稿,PUBLISHED-已发布',
  `create_user` varchar(50) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` varchar(50) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_form_key_version` (`form_key`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单定义表';
```

**创建Controller**:

```java
@RestController
@RequestMapping("/workflow/api/form")
public class FormController {
    
    @PostMapping("/save")
    public Map<String, Object> saveForm(@RequestBody FormDTO dto) {
        // 保存表单定义
    }
    
    @GetMapping("/{formKey}")
    public Map<String, Object> getForm(@PathVariable String formKey) {
        // 获取表单定义
    }
    
    @PostMapping("/publish")
    public Map<String, Object> publishForm(@RequestParam Long id) {
        // 发布表单
    }
}
```

---

**任务2.1验收标准**:
- [ ] 可以打开表单设计器
- [ ] 可以拖拽添加表单字段
- [ ] 可以配置字段属性（必填、默认值等）
- [ ] 可以预览表单效果
- [ ] 表单定义保存到数据库
- [ ] 用户任务关联表单Key

---

### 任务2.2: 实时验证与智能提示 ⭐⭐⭐⭐
**优先级**: P1  
**工时**: 8-10小时  
**负责人**: 前端开发  
**依赖**: 阶段1完成

#### 子任务

##### 2.2.1 实现BPMN验证引擎（4小时）

**创建`bpmnValidator.js`**:

```javascript
export class BpmnValidator {
  constructor(modeler) {
    this.modeler = modeler
    this.elementRegistry = modeler.get('elementRegistry')
    this.canvas = modeler.get('canvas')
  }
  
  validate() {
    const errors = []
    const warnings = []
    
    // 验证规则1: 流程必须有开始事件
    const startEvents = this.elementRegistry.filter(
      el => el.type === 'bpmn:StartEvent'
    )
    if (startEvents.length === 0) {
      errors.push({
        type: 'error',
        message: '流程必须至少有一个开始事件',
        elementId: null
      })
    }
    
    // 验证规则2: 流程必须有结束事件
    const endEvents = this.elementRegistry.filter(
      el => el.type === 'bpmn:EndEvent'
    )
    if (endEvents.length === 0) {
      errors.push({
        type: 'error',
        message: '流程必须至少有一个结束事件',
        elementId: null
      })
    }
    
    // 验证规则3: 用户任务必须有assignee
    const userTasks = this.elementRegistry.filter(
      el => el.type === 'bpmn:UserTask'
    )
    userTasks.forEach(task => {
      const bo = task.businessObject
      if (!bo.assignee && !bo.candidateUsers && !bo.candidateGroups) {
        errors.push({
          type: 'error',
          message: `用户任务"${bo.name || task.id}"缺少assignee配置`,
          elementId: task.id,
          element: task
        })
      }
    })
    
    // 验证规则4: 元素必须有连接
    this.elementRegistry.forEach(element => {
      if (this.isFlowNode(element)) {
        const incoming = element.incoming || []
        const outgoing = element.outgoing || []
        
        if (element.type !== 'bpmn:StartEvent' && incoming.length === 0) {
          warnings.push({
            type: 'warning',
            message: `元素"${element.businessObject.name || element.id}"没有输入连接`,
            elementId: element.id,
            element: element
          })
        }
        
        if (element.type !== 'bpmn:EndEvent' && outgoing.length === 0) {
          warnings.push({
            type: 'warning',
            message: `元素"${element.businessObject.name || element.id}"没有输出连接`,
            elementId: element.id,
            element: element
          })
        }
      }
    })
    
    // 验证规则5: 网关必须有条件
    const gateways = this.elementRegistry.filter(
      el => el.type === 'bpmn:ExclusiveGateway' || el.type === 'bpmn:InclusiveGateway'
    )
    gateways.forEach(gateway => {
      const outgoing = gateway.outgoing || []
      outgoing.forEach(flow => {
        if (!flow.businessObject.conditionExpression) {
          warnings.push({
            type: 'warning',
            message: `网关出口流"${flow.id}"缺少条件表达式`,
            elementId: flow.id,
            element: flow
          })
        }
      })
    })
    
    return { errors, warnings }
  }
  
  isFlowNode(element) {
    return element.type && (
      element.type.includes('Task') ||
      element.type.includes('Event') ||
      element.type.includes('Gateway')
    )
  }
  
  // 高亮错误元素
  highlightErrors(errors) {
    errors.forEach(error => {
      if (error.elementId) {
        this.canvas.addMarker(error.elementId, 'error-marker')
      }
    })
  }
  
  // 高亮警告元素
  highlightWarnings(warnings) {
    warnings.forEach(warning => {
      if (warning.elementId) {
        this.canvas.addMarker(warning.elementId, 'warning-marker')
      }
    })
  }
  
  // 清除所有标记
  clearMarkers() {
    this.elementRegistry.forEach(element => {
      this.canvas.removeMarker(element.id, 'error-marker')
      this.canvas.removeMarker(element.id, 'warning-marker')
    })
  }
}
```

**在ProcessDesigner.vue中使用**:

```javascript
import { BpmnValidator } from './utils/bpmnValidator'

let validator = null

const initBpmnModeler = () => {
  // ... modeler初始化
  
  // 创建验证器
  validator = new BpmnValidator(modeler)
  
  // 监听变化，实时验证
  modeler.on('commandStack.changed', () => {
    debounceValidate()
  })
}

// 防抖验证
let validateTimer = null
const debounceValidate = () => {
  if (validateTimer) {
    clearTimeout(validateTimer)
  }
  validateTimer = setTimeout(() => {
    const result = validator.validate()
    validator.clearMarkers()
    validator.highlightErrors(result.errors)
    validator.highlightWarnings(result.warnings)
    
    // 显示验证结果
    showValidationResult(result)
  }, 500)
}

// 显示验证结果
const showValidationResult = (result) => {
  if (result.errors.length > 0) {
    ElMessage.warning(`发现${result.errors.length}个错误`)
  } else if (result.warnings.length > 0) {
    ElMessage.info(`发现${result.warnings.length}个警告`)
  }
}
```

**添加CSS样式**:

```css
:deep(.error-marker) {
  stroke: #f56c6c !important;
  stroke-width: 3px !important;
  fill: rgba(245, 108, 108, 0.1) !important;
}

:deep(.warning-marker) {
  stroke: #e6a23c !important;
  stroke-width: 2px !important;
  fill: rgba(230, 162, 60, 0.1) !important;
}
```

---

##### 2.2.2 添加验证结果面板（2小时）

**在ProcessDesigner.vue中添加**:

```vue
<template>
  <!-- 原有内容 -->
  
  <!-- 验证结果面板 -->
  <div v-if="showValidationPanel" class="validation-panel">
    <div class="validation-header">
      <span>验证结果</span>
      <el-button text @click="showValidationPanel = false">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
    
    <div class="validation-content">
      <!-- 错误列表 -->
      <div v-if="validationResult.errors.length > 0" class="validation-section">
        <div class="section-title error-title">
          <el-icon><CircleClose /></el-icon>
          错误 ({{ validationResult.errors.length }})
        </div>
        <div 
          v-for="(error, index) in validationResult.errors"
          :key="index"
          class="validation-item error-item"
          @click="locateElement(error.elementId)"
        >
          <el-icon><Warning /></el-icon>
          <span>{{ error.message }}</span>
        </div>
      </div>
      
      <!-- 警告列表 -->
      <div v-if="validationResult.warnings.length > 0" class="validation-section">
        <div class="section-title warning-title">
          <el-icon><WarningFilled /></el-icon>
          警告 ({{ validationResult.warnings.length }})
        </div>
        <div 
          v-for="(warning, index) in validationResult.warnings"
          :key="index"
          class="validation-item warning-item"
          @click="locateElement(warning.elementId)"
        >
          <el-icon><InfoFilled /></el-icon>
          <span>{{ warning.message }}</span>
        </div>
      </div>
      
      <!-- 通过提示 -->
      <div v-if="validationResult.errors.length === 0 && validationResult.warnings.length === 0" class="validation-section">
        <div class="section-title success-title">
          <el-icon><CircleCheck /></el-icon>
          验证通过
        </div>
        <div class="validation-success">
          ✅ 流程定义没有发现任何问题，可以安全发布！
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CircleClose, WarningFilled, InfoFilled, CircleCheck, Warning, Close } from '@element-plus/icons-vue'

const showValidationPanel = ref(false)
const validationResult = ref({ errors: [], warnings: [] })

// 定位到错误元素
const locateElement = (elementId) => {
  if (!elementId || !modeler) return
  
  const elementRegistry = modeler.get('elementRegistry')
  const selection = modeler.get('selection')
  const canvas = modeler.get('canvas')
  
  const element = elementRegistry.get(elementId)
  if (element) {
    // 选中元素
    selection.select(element)
    // 居中显示
    canvas.scrollToElement(element)
  }
}
</script>

<style scoped>
.validation-panel {
  position: fixed;
  bottom: 20px;
  right: 340px;
  width: 350px;
  max-height: 400px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 100;
}

.validation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  background: #f5f7fa;
  border-radius: 8px 8px 0 0;
}

.validation-content {
  max-height: 340px;
  overflow-y: auto;
  padding: 10px;
}

.validation-section {
  margin-bottom: 15px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  padding: 6px 10px;
  border-radius: 4px;
}

.error-title {
  background: #fef0f0;
  color: #f56c6c;
}

.warning-title {
  background: #fdf6ec;
  color: #e6a23c;
}

.success-title {
  background: #f0f9ff;
  color: #67c23a;
}

.validation-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px;
  margin-bottom: 6px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.error-item {
  background: #fef0f0;
  color: #f56c6c;
  border-left: 3px solid #f56c6c;
}

.error-item:hover {
  background: #fde2e2;
}

.warning-item {
  background: #fdf6ec;
  color: #e6a23c;
  border-left: 3px solid #e6a23c;
}

.warning-item:hover {
  background: #faecd8;
}

.validation-success {
  padding: 20px;
  text-align: center;
  color: #67c23a;
  font-size: 14px;
}
</style>
```

---

##### 2.2.3 添加智能修复建议（2小时）

**在验证项中添加修复建议**:

```javascript
// 在bpmnValidator.js中为每个错误添加修复建议
{
  type: 'error',
  message: '用户任务缺少assignee配置',
  elementId: task.id,
  fix: {
    label: '快速修复',
    action: () => {
      // 打开属性面板并聚焦到assignee字段
      selectElement(task.id)
      focusAssigneeField()
    }
  }
}
```

---

**任务2.2验收标准**:
- [ ] 流程变化时自动验证
- [ ] 错误元素红色高亮
- [ ] 警告元素黄色高亮
- [ ] 验证结果面板显示详细信息
- [ ] 点击错误项可定位到元素
- [ ] 智能修复建议可用

---

*由于篇幅限制，完整的工作计划将继续在下一个回复中...*

---

## 📝 总结

以上是**阶段1（核心功能）和阶段2前半部分（表单设计器+实时验证）**的详细工作计划。

**关键要点**:
1. 每个任务都有明确的子任务和验收标准
2. 提供了具体的代码实现示例
3. 标注了工时、优先级和依赖关系
4. 便于讨论和调整

**接下来需要讨论**:
1. 是否接受这个工作量评估？
2. 是否需要调整优先级？
3. 是否需要更详细的某个任务分解？
4. 何时开始实施？

请告诉我您的想法，我们可以继续细化后续的阶段3（高级功能）！

