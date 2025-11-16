# 阶段1.1完成 - Camunda属性面板集成测试指南

## 🎉 完成内容

### ✅ 已完成任务

1. **NPM依赖安装** ✅
   - camunda-bpmn-js-behaviors@0.5.0

2. **ProcessDesigner.vue集成** ✅
   - 导入 `CamundaPlatformPropertiesProviderModule`
   - 导入 `CamundaBehaviorsModule`
   - 添加到modeler的additionalModules

3. **CSS样式优化** ✅
   - 新增300+行属性面板样式
   - Element Plus风格统一
   - 响应式交互效果

4. **前端编译** ✅
   - 编译成功，无错误
   - 代码已提交到Git
   - 已推送到GitHub

---

## 🧪 本地测试步骤

### 前置条件

请确保以下服务正在运行：
- MySQL (localhost:3306)
- Nacos (localhost:8848)
- diom-auth-service (localhost:8081)
- diom-gateway (localhost:8080)
- diom-workflow-service (localhost:8085)

### 步骤1: 启动前端开发服务器

```bash
cd /Users/yanchao/IdeaProjects/diom-workFlow/diom-frontend
npm run dev
```

访问: http://localhost:5173

### 步骤2: 登录系统

- 用户名: `admin`
- 密码: `123456`

### 步骤3: 进入流程设计器

1. 点击左侧菜单 "工作流管理" → "流程设计器"
2. 点击 "新建流程" 按钮

### 步骤4: 测试属性面板

#### 4.1 创建用户任务

1. **从左侧工具栏拖拽"用户任务"到画布**
2. **点击刚创建的用户任务元素**
3. **查看右侧属性面板** 👈 关键！

**预期结果**:
- ✅ 右侧应该显示完整的属性面板
- ✅ 面板应该有蓝色标题栏
- ✅ 面板应该有多个分组（General、Assignments、Forms等）
- ✅ 样式应该美观，与Element Plus一致

#### 4.2 配置Assignee（重点测试）

1. **在属性面板中找到"Assignments"分组**
2. **展开该分组**
3. **找到"Assignee"字段**
4. **输入一个用户名，例如: `manager`**
5. **观察输入框样式和交互**

**预期结果**:
- ✅ 输入框应该有蓝色聚焦边框
- ✅ 输入内容时应该没有卡顿
- ✅ 输入框宽度应该填满整个属性面板

#### 4.3 配置其他Camunda属性

**测试项目清单**:

##### 用户任务 (User Task)
- [ ] **Assignee** - 固定用户 (e.g., `manager`)
- [ ] **Assignee Expression** - 表达式 (e.g., `${applicant}`)
- [ ] **Candidate Users** - 候选用户列表
- [ ] **Candidate Groups** - 候选用户组
- [ ] **Due Date** - 到期时间
- [ ] **Priority** - 优先级 (0-100)
- [ ] **Form Key** - 表单Key

##### 服务任务 (Service Task)
1. 从工具栏拖拽"服务任务"到画布
2. 点击服务任务
3. 在属性面板配置:
   - [ ] **Java Class** - Java类名
   - [ ] **Expression** - 表达式
   - [ ] **Delegate Expression** - 委托表达式

##### 网关 (Gateway)
1. 从工具栏拖拽"排他网关"到画布
2. 点击网关
3. 在属性面板配置:
   - [ ] **Default Flow** - 默认流向
   - [ ] **Condition Expression** - 条件表达式

### 步骤5: 保存并验证

1. **填写流程信息**:
   - 流程名称: `测试流程-属性面板`
   - 流程Key: `test-properties-panel`
   - 流程分类: 行政

2. **点击"保存草稿"按钮**

3. **等待保存成功提示**

4. **点击"验证"按钮**

**预期结果**:
- ✅ 保存成功提示
- ✅ 验证通过（如果配置了assignee）
- ✅ 或显示友好的验证错误提示（如果有问题）

### 步骤6: 检查BPMN XML

1. **点击"导出XML"按钮**
2. **下载BPMN文件**
3. **用文本编辑器打开**
4. **搜索 `camunda:assignee`**

**预期结果**:
```xml
<bpmn:userTask id="Activity_xxx" name="用户任务" camunda:assignee="manager">
```

---

## 📊 测试验收标准

### 必须通过的测试 ⭐⭐⭐⭐⭐

- [ ] **属性面板正常显示** - 右侧属性面板完整显示
- [ ] **Assignee可配置** - 可以在用户任务中输入assignee
- [ ] **Assignee保存成功** - 保存后重新打开，assignee值正确
- [ ] **导出XML包含Camunda属性** - XML中有 `camunda:assignee` 属性
- [ ] **样式美观** - 属性面板样式与Element Plus一致

### 建议通过的测试 ⭐⭐⭐

- [ ] Candidate Users可配置
- [ ] Candidate Groups可配置
- [ ] Service Task的Java Class可配置
- [ ] 网关的Condition Expression可配置

### 额外测试 ⭐

- [ ] 属性面板可滚动
- [ ] 输入框聚焦时有蓝色边框
- [ ] 分组可以折叠/展开
- [ ] 帮助提示显示正常

---

## 🐛 常见问题排查

### 问题1: 属性面板不显示

**症状**: 右侧属性面板区域是空白的

**可能原因**:
1. 模块未正确导入
2. CSS样式未加载

**解决方法**:
```bash
# 1. 检查浏览器控制台是否有错误
# 2. 清除缓存并重新加载页面 (Ctrl+Shift+R)
# 3. 重新编译前端
cd diom-frontend
npm run build
npm run dev
```

### 问题2: Assignee字段不显示

**症状**: 属性面板显示了，但是没有Assignee字段

**可能原因**:
- `CamundaPlatformPropertiesProviderModule` 未正确加载

**解决方法**:
```bash
# 检查NPM依赖
npm list camunda-bpmn-js-behaviors

# 如果未安装，重新安装
npm install camunda-bpmn-js-behaviors@0.5.0
```

### 问题3: 样式不美观

**症状**: 属性面板显示了，但是样式很丑

**可能原因**:
- CSS样式未正确加载
- 浏览器缓存问题

**解决方法**:
1. 强制刷新页面 (Ctrl+Shift+R)
2. 检查开发者工具的Network面板，确认CSS加载成功
3. 检查 `properties-panel.css` 是否正确导入

### 问题4: 保存后配置丢失

**症状**: 配置了assignee，保存后重新打开，配置丢失

**可能原因**:
- 后端未正确保存BPMN XML
- XML中缺少Camunda命名空间

**解决方法**:
1. 检查导出的XML是否包含:
```xml
xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
```
2. 检查后端日志，确认BPMN XML正确保存到数据库

---

## 📸 预期截图

### 1. 属性面板完整显示
![属性面板](应该看到蓝色标题栏的属性面板，包含多个分组)

### 2. Assignee配置
![Assignee配置](在Assignments分组中有Assignee输入框，输入manager)

### 3. BPMN XML导出
![XML导出](XML文件中包含camunda:assignee="manager")

---

## ✅ 测试完成后

如果所有**必须通过的测试**都通过了，请告诉我：

**方式1: 简单反馈**
```
属性面板测试通过✅，可以配置assignee
```

**方式2: 详细反馈**
```
测试结果：
- 属性面板显示: ✅
- Assignee配置: ✅
- 保存成功: ✅
- XML导出正确: ✅
- 样式美观: ✅
```

**方式3: 发现问题**
```
属性面板显示正常，但是Assignee字段找不到❌
（附上截图会更好）
```

---

## 🚀 测试通过后的下一步

一旦测试通过，我们将继续：

**阶段1.2: 元素连接功能** (2-3小时)
- 启用Context Pad（元素周围的快捷菜单）
- 添加连接工具到Toolbar
- 支持拖拽连接元素

**阶段1.3: 基本编辑功能** (2-3小时)
- 键盘快捷键（Delete、Ctrl+C/V、Ctrl+Z/Y）
- 编辑工具栏（撤销/重做/复制/删除/缩放）

**阶段1.4: UI优化** (2-3小时)
- Mini地图
- 网格背景
- 元素高亮效果

完成这些后，您将拥有一个**完全可用的企业级流程设计器**！🎉

---

**准备好测试了吗？** 🧪

请按照上述步骤测试，有任何问题随时告诉我！

