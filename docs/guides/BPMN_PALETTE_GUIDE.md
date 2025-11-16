# 🎨 BPMN Palette（调色板）使用指南

## 📊 什么是Palette？

Palette是bpmn-js提供的**元素选择面板**，包含所有可拖拽的BPMN元素：

### 事件（Events）
- 🟢 **开始事件** - 流程的起点
- 🔴 **结束事件** - 流程的终点
- 🟡 **中间事件** - 流程中的事件（消息、定时器等）
- 🔵 **边界事件** - 附加在任务上的事件

### 任务（Tasks）
- 👤 **用户任务** - 需要人工处理的任务
- ⚙️ **服务任务** - 自动执行的任务（调用服务）
- 📝 **脚本任务** - 执行脚本代码
- 📧 **发送任务** - 发送消息
- 📬 **接收任务** - 接收消息
- ✋ **手工任务** - 不需要系统支持的任务
- 📋 **业务规则** - 执行业务规则引擎

### 网关（Gateways）
- ❌ **排他网关** - 单一路径选择
- ➕ **并行网关** - 多路径并行执行
- ⚪ **包容网关** - 多路径条件执行
- 🔷 **事件网关** - 基于事件的路径选择

### 其他
- 🏊 **泳道/池** - 组织流程参与者
- 📝 **注释** - 添加说明文字
- ➡️ **序列流** - 连接元素的箭头

---

## ✅ 我们的项目已支持Palette

### 当前配置

在 `diom-frontend/src/views/Workflow/ProcessDesigner.vue` 中：

```javascript
import BpmnModeler from 'bpmn-js/lib/Modeler'

// BpmnModeler 默认包含 Palette
modeler = new BpmnModeler({
    container: bpmnCanvas.value,
    // Palette会自动显示在画布左侧
    propertiesPanel: {
        parent: propertiesPanel.value
    },
    additionalModules: [
        BpmnPropertiesPanelModule,
        BpmnPropertiesProviderModule
    ],
    moddleExtensions: {
        camunda: CamundaBpmnModdle
    }
})
```

### Palette样式

```css
:deep(.djs-palette) {
  left: 20px;    /* 距离左边20px */
  top: 20px;     /* 距离顶部20px */
}
```

---

## 🎯 如何使用Palette？

### 步骤1: 登录系统

访问：`http://localhost:3000/login`

```
用户名: admin
密码: admin123
```

### 步骤2: 进入流程设计器

有两种方式：

**方式1: 新建流程**
1. 访问 `http://localhost:3000/workflow/design/list`
2. 点击"新建流程"按钮
3. 进入流程设计器

**方式2: 编辑现有流程**
1. 访问 `http://localhost:3000/workflow/design/list`
2. 在列表中点击"编辑"按钮
3. 进入流程设计器

### 步骤3: 使用Palette

**拖拽方式**：
1. 在左侧Palette面板中找到需要的元素
2. 用鼠标拖拽元素到画布上
3. 释放鼠标，元素被放置

**点击方式**：
1. 选中画布上的一个元素
2. 该元素周围会出现快捷菜单
3. 点击快捷菜单图标创建新元素
4. 或拖动箭头创建序列流

### 步骤4: 连接元素

1. 从Palette拖拽"序列流"工具
2. 点击起始元素，再点击目标元素
3. 序列流自动创建

### 步骤5: 配置属性

1. 选中元素
2. 右侧属性面板自动显示该元素的属性
3. 修改名称、ID、条件等属性

---

## 🔍 Palette元素详解

### 左侧Palette面板结构

```
┌─────────────────┐
│  ✋ 手型工具     │  → 拖动画布
├─────────────────┤
│  📍 套索选择    │  → 框选多个元素
├─────────────────┤
│  🔧 空间调整    │  → 调整元素间距
├─────────────────┤
│  🟢 开始事件    │  → 流程起点
├─────────────────┤
│  ⭕ 中间事件    │  → 中间事件
├─────────────────┤
│  🔴 结束事件    │  → 流程终点
├─────────────────┤
│  📋 任务        │  → 普通任务
├─────────────────┤
│  👤 用户任务    │  → 用户交互
├─────────────────┤
│  ⚙️ 服务任务    │  → 自动执行
├─────────────────┤
│  ❌ 排他网关    │  → 条件分支
├─────────────────┤
│  ➕ 并行网关    │  → 并行执行
├─────────────────┤
│  🏊 泳道/池     │  → 组织结构
├─────────────────┤
│  📝 注释        │  → 添加说明
└─────────────────┘
```

---

## 🎨 自定义Palette（可选）

如果需要自定义Palette（例如：添加中文标签、隐藏某些元素），可以修改配置：

### 方案1: 完全自定义Palette

```javascript
import BpmnModeler from 'bpmn-js/lib/Modeler'
import CustomPaletteProvider from './CustomPaletteProvider' // 自定义Palette提供者

modeler = new BpmnModeler({
    container: bpmnCanvas.value,
    additionalModules: [
        {
            paletteProvider: ['type', CustomPaletteProvider]
        }
    ]
})
```

### 方案2: 只修改标签为中文

创建 `CustomPaletteProvider.js`:

```javascript
export default function CustomPaletteProvider(
    palette, 
    create, 
    elementFactory, 
    spaceTool, 
    lassoTool
) {
    this._create = create
    this._elementFactory = elementFactory
    this._spaceTool = spaceTool
    this._lassoTool = lassoTool

    palette.registerProvider(this)
}

CustomPaletteProvider.prototype.getPaletteEntries = function() {
    return {
        'hand-tool': {
            group: 'tools',
            className: 'bpmn-icon-hand-tool',
            title: '移动画布',
            action: {
                click: function(event) {
                    // ...
                }
            }
        },
        'create.start-event': {
            group: 'event',
            className: 'bpmn-icon-start-event-none',
            title: '创建开始事件',
            action: {
                click: function(event) {
                    // ...
                }
            }
        },
        'create.user-task': {
            group: 'activity',
            className: 'bpmn-icon-user-task',
            title: '创建用户任务',
            action: {
                click: function(event) {
                    // ...
                }
            }
        }
        // 更多元素...
    }
}
```

### 方案3: 使用bpmn-js-i18n（推荐）

安装中文国际化插件：

```bash
npm install bpmn-js-i18n --save
```

使用：

```javascript
import BpmnModeler from 'bpmn-js/lib/Modeler'
import customTranslate from 'bpmn-js-i18n/translations/zh_CN'

const customTranslateModule = {
    translate: ['value', customTranslate]
}

modeler = new BpmnModeler({
    container: bpmnCanvas.value,
    additionalModules: [
        customTranslateModule  // 添加中文翻译
    ]
})
```

---

## 🐛 常见问题

### Q1: Palette不显示？

**检查项**：
1. ✅ 确认CSS已正确导入
   ```javascript
   import 'bpmn-js/dist/assets/diagram-js.css'
   ```

2. ✅ 确认使用的是 `BpmnModeler`（不是Viewer）
   ```javascript
   import BpmnModeler from 'bpmn-js/lib/Modeler'
   ```

3. ✅ 检查浏览器控制台是否有CSS加载错误

4. ✅ 尝试调整Palette位置
   ```css
   :deep(.djs-palette) {
       left: 0 !important;
       top: 0 !important;
       z-index: 100 !important;
   }
   ```

### Q2: Palette位置不对？

**解决方案**：
```css
:deep(.djs-palette) {
    position: absolute;
    left: 20px;
    top: 20px;
}
```

### Q3: Palette元素拖不动？

**检查项**：
1. 确认页面已完全加载
2. 检查是否有CSS冲突
3. 验证bpmn-js版本是否正确
4. 清除浏览器缓存并刷新

### Q4: 如何隐藏Palette？

如果需要只读模式（不显示Palette），使用 `BpmnViewer` 而不是 `BpmnModeler`：

```javascript
import BpmnViewer from 'bpmn-js/lib/Viewer'

const viewer = new BpmnViewer({
    container: bpmnCanvas.value
})
```

---

## 📊 当前系统状态

### 已完成 ✅
- [x] bpmn-js依赖已安装（v14.2.0）
- [x] BpmnModeler已正确配置
- [x] Palette默认已启用
- [x] CSS样式已正确导入
- [x] 属性面板已集成

### 待验证 ⏸️
- [ ] 登录系统后测试Palette显示
- [ ] 测试拖拽功能
- [ ] 验证元素创建
- [ ] 测试序列流连接

---

## 🎯 下一步操作

### 立即执行

1. **修复登录问题**（如果需要）
   ```sql
   UPDATE sys_user 
   SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lFOgCOqqVo0MKhg/m' 
   WHERE username = 'admin';
   ```

2. **登录并访问流程设计器**
   - URL: `http://localhost:3000/workflow/design/list`
   - 点击"新建流程"
   - 观察左侧Palette面板

3. **测试拖拽功能**
   - 拖拽"用户任务"到画布
   - 拖拽"排他网关"到画布
   - 用序列流连接元素

### 可选优化

4. **添加中文标签**
   - 安装 bpmn-js-i18n
   - 配置中文翻译模块

5. **自定义Palette**
   - 根据业务需求调整元素
   - 添加常用元素快捷方式

---

## 📚 参考资源

- **bpmn-js官方文档**: https://bpmn.io/toolkit/bpmn-js/
- **Palette API**: https://github.com/bpmn-io/bpmn-js/tree/develop/lib/features/palette
- **自定义Palette示例**: https://github.com/bpmn-io/bpmn-js-examples
- **中文国际化**: https://github.com/bpmn-io/bpmn-js-i18n

---

**文档生成时间**: 2025-11-16  
**bpmn-js版本**: 14.2.0  
**状态**: ✅ Palette已配置，待测试验证

