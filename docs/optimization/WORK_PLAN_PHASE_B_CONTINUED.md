# 流程设计器完整版优化 - 工作计划（续）

## 🚀 阶段2（续）: 增强功能

### 任务2.3: 流程模板库 ⭐⭐⭐
**优先级**: P1  
**工时**: 8-10小时  
**负责人**: 前端+后端  
**依赖**: 阶段1完成

#### 子任务

##### 2.3.1 设计模板数据结构（1小时）

**数据库表设计**:

```sql
-- 流程模板表
CREATE TABLE `process_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_key` varchar(100) NOT NULL COMMENT '模板Key',
  `category` varchar(50) COMMENT '分类',
  `description` varchar(500) COMMENT '描述',
  `bpmn_xml` longtext NOT NULL COMMENT 'BPMN XML',
  `thumbnail` varchar(500) COMMENT '缩略图URL',
  `usage_count` int(11) DEFAULT 0 COMMENT '使用次数',
  `is_system` tinyint(1) DEFAULT 0 COMMENT '是否系统模板:0-否,1-是',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态:ACTIVE-启用,INACTIVE-停用',
  `create_user` varchar(50) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_key` (`template_key`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程模板表';
```

**验收标准**:
- [ ] 表结构设计合理
- [ ] 索引优化完成

---

##### 2.3.2 创建内置模板（3小时）

**准备5个常用模板**:

1. **请假审批流程**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions ...>
  <bpmn:process id="leave-approval-template" name="请假审批流程模板">
    <bpmn:startEvent id="StartEvent_1" name="员工提交请假申请" />
    <bpmn:userTask id="Task_Manager" name="部门经理审批" camunda:assignee="manager" />
    <bpmn:userTask id="Task_HR" name="HR审批" camunda:assignee="hr" />
    <bpmn:endEvent id="EndEvent_1" name="流程结束" />
    <!-- ... 完整流程定义 ... -->
  </bpmn:process>
</bpmn:definitions>
```

2. **报销审批流程**
3. **采购审批流程**
4. **合同审批流程**
5. **简单串行流程**

**初始化SQL**:

```sql
-- 插入系统模板
INSERT INTO `process_template` (`template_name`, `template_key`, `category`, `description`, `bpmn_xml`, `is_system`) VALUES
('请假审批流程', 'leave-approval-template', '人事', '标准请假审批流程，包含部门经理和HR两级审批', '<bpmn xml...>', 1),
('报销审批流程', 'reimbursement-template', '财务', '费用报销审批流程，包含部门经理和财务审批', '<bpmn xml...>', 1),
('采购审批流程', 'procurement-template', '采购', '采购申请审批流程，包含多级审批和财务确认', '<bpmn xml...>', 1),
('合同审批流程', 'contract-template', '法务', '合同签署审批流程，包含法务审核和领导审批', '<bpmn xml...>', 1),
('简单串行流程', 'simple-serial-template', '其他', '最基础的串行审批流程，适合简单场景', '<bpmn xml...>', 1);
```

**验收标准**:
- [ ] 5个模板BPMN定义完整
- [ ] 模板数据导入成功
- [ ] 每个模板包含完整的Camunda属性

---

##### 2.3.3 后端API开发（2小时）

**创建`TemplateController.java`**:

```java
@RestController
@RequestMapping("/workflow/api/template")
public class TemplateController {
    
    @Autowired
    private TemplateService templateService;
    
    /**
     * 查询模板列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Map<String, Object> params) {
        Page<TemplateVO> page = templateService.list(params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", page.getTotal());
        data.put("list", page.getRecords());
        result.put("data", data);
        
        return result;
    }
    
    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        TemplateVO template = templateService.getById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", template);
        return result;
    }
    
    /**
     * 应用模板（创建新流程）
     */
    @PostMapping("/apply")
    public Map<String, Object> applyTemplate(@RequestBody ApplyTemplateDTO dto) {
        // 基于模板创建新的流程设计
        ProcessDesignVO design = templateService.applyTemplate(dto);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "模板应用成功");
        result.put("data", design);
        return result;
    }
    
    /**
     * 保存为模板
     */
    @PostMapping("/save")
    public Map<String, Object> saveAsTemplate(@RequestBody SaveTemplateDTO dto) {
        TemplateVO template = templateService.saveAsTemplate(dto);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "保存模板成功");
        result.put("data", template);
        return result;
    }
    
    /**
     * 删除模板（仅非系统模板）
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        templateService.delete(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }
}
```

**验收标准**:
- [ ] 所有API接口实现
- [ ] 权限验证正确
- [ ] 异常处理完善

---

##### 2.3.4 前端模板库组件（2小时）

**创建`TemplateLibrary.vue`**:

```vue
<template>
  <el-dialog 
    v-model="visible" 
    title="流程模板库" 
    width="80%"
    :close-on-click-modal="false"
  >
    <div class="template-library">
      <!-- 分类筛选 -->
      <div class="category-filter">
        <el-radio-group v-model="selectedCategory" @change="loadTemplates">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="人事">人事</el-radio-button>
          <el-radio-button label="财务">财务</el-radio-button>
          <el-radio-button label="采购">采购</el-radio-button>
          <el-radio-button label="法务">法务</el-radio-button>
          <el-radio-button label="其他">其他</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 模板卡片列表 -->
      <div class="template-grid">
        <div 
          v-for="template in templates"
          :key="template.id"
          class="template-card"
          @click="selectTemplate(template)"
        >
          <!-- 缩略图 -->
          <div class="template-thumbnail">
            <img v-if="template.thumbnail" :src="template.thumbnail" />
            <div v-else class="placeholder">
              <el-icon><Document /></el-icon>
            </div>
            
            <!-- 系统模板标识 -->
            <el-tag v-if="template.isSystem" class="system-tag" size="small">
              系统模板
            </el-tag>
          </div>
          
          <!-- 模板信息 -->
          <div class="template-info">
            <div class="template-name">{{ template.templateName }}</div>
            <div class="template-description">{{ template.description }}</div>
            <div class="template-meta">
              <el-tag size="small">{{ template.category }}</el-tag>
              <span class="usage-count">
                <el-icon><User /></el-icon>
                {{ template.usageCount }}次使用
              </span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="template-actions">
            <el-button type="primary" size="small" @click.stop="applyTemplate(template)">
              使用模板
            </el-button>
            <el-button size="small" @click.stop="previewTemplate(template)">
              预览
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, User } from '@element-plus/icons-vue'
import { getTemplateList, applyTemplateApi } from '@/api/template'

const visible = ref(false)
const templates = ref([])
const selectedCategory = ref('')

const show = () => {
  visible.value = true
  loadTemplates()
}

const loadTemplates = async () => {
  try {
    const res = await getTemplateList({ category: selectedCategory.value })
    if (res.code === 200) {
      templates.value = res.data.list
    }
  } catch (error) {
    ElMessage.error('加载模板失败')
  }
}

const applyTemplate = async (template) => {
  try {
    const res = await applyTemplateApi({
      templateId: template.id,
      newProcessName: `${template.templateName}-副本`,
      newProcessKey: `${template.templateKey}-${Date.now()}`
    })
    
    if (res.code === 200) {
      ElMessage.success('模板应用成功')
      emit('applied', res.data)
      visible.value = false
    }
  } catch (error) {
    ElMessage.error('应用模板失败')
  }
}

const previewTemplate = (template) => {
  emit('preview', template)
}

defineExpose({ show })
</script>

<style scoped>
.template-library {
  min-height: 400px;
}

.category-filter {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.template-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.template-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.template-thumbnail {
  position: relative;
  height: 160px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder {
  font-size: 48px;
  color: #c0c4cc;
}

.system-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.template-info {
  padding: 15px;
}

.template-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-description {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 12px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.template-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.usage-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.template-actions {
  padding: 10px 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 10px;
}
</style>
```

**集成到流程设计器列表页**:

```vue
<template>
  <!-- 在列表页顶部添加"从模板创建"按钮 -->
  <el-button type="success" @click="openTemplateLibrary">
    <el-icon><Files /></el-icon> 从模板创建
  </el-button>
  
  <!-- 模板库组件 -->
  <TemplateLibrary 
    ref="templateLibrary"
    @applied="handleTemplateApplied"
    @preview="handleTemplatePreview"
  />
</template>
```

**验收标准**:
- [ ] 模板库显示所有模板
- [ ] 分类筛选正常工作
- [ ] 点击"使用模板"创建新流程
- [ ] 模板预览功能正常
- [ ] 使用次数正确统计

---

## 📊 阶段2总结

**预计完成时间**: 28-36小时（3.5-4.5天）

**交付物清单**:
- [x] FormDesigner.vue（表单设计器）
- [x] bpmnValidator.js（验证引擎）
- [x] ValidationPanel（验证面板）
- [x] TemplateLibrary.vue（模板库）
- [x] 后端API（表单+模板）
- [x] 数据库表（form_definition + process_template）
- [x] 内置模板（5个）

**验收标准**:
- [ ] 可以设计表单并关联到用户任务
- [ ] 实时验证功能正常，错误高亮清晰
- [ ] 模板库功能完整，可以应用模板创建流程
- [ ] 用户体验显著提升

---

## 🎯 阶段3: 高级功能（P2）

### 任务3.1: 流程模拟运行 ⭐⭐⭐
**优先级**: P2  
**工时**: 10-14小时  
**负责人**: 前端+后端  
**依赖**: 阶段1、2完成

#### 子任务

##### 3.1.1 设计模拟引擎（3小时）

**创建`processSimulator.js`**:

```javascript
export class ProcessSimulator {
  constructor(modeler) {
    this.modeler = modeler
    this.elementRegistry = modeler.get('elementRegistry')
    this.canvas = modeler.get('canvas')
    this.currentTokens = []
    this.history = []
    this.variables = {}
  }
  
  /**
   * 开始模拟
   */
  start() {
    // 清除之前的模拟状态
    this.reset()
    
    // 找到所有开始事件
    const startEvents = this.elementRegistry.filter(
      el => el.type === 'bpmn:StartEvent'
    )
    
    if (startEvents.length === 0) {
      throw new Error('没有找到开始事件')
    }
    
    // 为每个开始事件创建令牌
    startEvents.forEach(start => {
      this.createToken(start)
      this.highlightElement(start.id, 'token')
      this.history.push({
        timestamp: Date.now(),
        action: 'START',
        element: start,
        message: `流程从"${start.businessObject.name || start.id}"开始`
      })
    })
    
    return {
      success: true,
      tokens: this.currentTokens.length,
      message: '模拟开始'
    }
  }
  
  /**
   * 移动到下一步
   */
  next() {
    if (this.currentTokens.length === 0) {
      return {
        success: false,
        message: '流程已结束'
      }
    }
    
    // 获取当前令牌
    const token = this.currentTokens[0]
    const currentElement = token.element
    
    // 清除当前高亮
    this.clearHighlight(currentElement.id)
    
    // 获取出口连接
    const outgoing = currentElement.outgoing || []
    
    if (outgoing.length === 0) {
      // 结束事件
      this.removeToken(token)
      this.history.push({
        timestamp: Date.now(),
        action: 'END',
        element: currentElement,
        message: `流程在"${currentElement.businessObject.name || currentElement.id}"结束`
      })
      
      return {
        success: true,
        finished: this.currentTokens.length === 0,
        message: '到达结束事件'
      }
    }
    
    // 处理网关
    if (this.isGateway(currentElement)) {
      return this.handleGateway(token, currentElement, outgoing)
    }
    
    // 普通任务，移动到下一个元素
    const nextFlow = outgoing[0]
    const nextElement = nextFlow.target
    
    token.element = nextElement
    this.highlightElement(nextElement.id, 'token')
    
    this.history.push({
      timestamp: Date.now(),
      action: 'MOVE',
      element: nextElement,
      message: `移动到"${nextElement.businessObject.name || nextElement.id}"`
    })
    
    return {
      success: true,
      currentElement: nextElement,
      message: `当前节点: ${nextElement.businessObject.name || nextElement.id}`
    }
  }
  
  /**
   * 处理网关
   */
  handleGateway(token, gateway, outgoing) {
    if (gateway.type === 'bpmn:ParallelGateway') {
      // 并行网关：为每个出口创建新令牌
      this.removeToken(token)
      
      outgoing.forEach(flow => {
        const newToken = this.createToken(flow.target)
        this.highlightElement(flow.target.id, 'token')
      })
      
      return {
        success: true,
        message: `并行网关：创建了${outgoing.length}个分支`
      }
    } else if (gateway.type === 'bpmn:ExclusiveGateway') {
      // 排他网关：需要选择一个出口
      return {
        success: false,
        needChoice: true,
        choices: outgoing.map(flow => ({
          id: flow.id,
          name: flow.businessObject.name || flow.id,
          target: flow.target
        })),
        message: '请选择一个分支'
      }
    }
    
    // 其他网关类型...
  }
  
  /**
   * 选择网关出口
   */
  chooseGatewayOutgoing(flowId) {
    const token = this.currentTokens[0]
    const flow = this.elementRegistry.get(flowId)
    
    if (!flow) {
      return { success: false, message: '无效的分支' }
    }
    
    const nextElement = flow.target
    token.element = nextElement
    this.highlightElement(nextElement.id, 'token')
    
    return {
      success: true,
      currentElement: nextElement
    }
  }
  
  /**
   * 设置变量
   */
  setVariable(key, value) {
    this.variables[key] = value
  }
  
  /**
   * 获取变量
   */
  getVariable(key) {
    return this.variables[key]
  }
  
  /**
   * 重置模拟
   */
  reset() {
    this.currentTokens = []
    this.history = []
    this.variables = {}
    this.elementRegistry.forEach(element => {
      this.clearHighlight(element.id)
    })
  }
  
  // 辅助方法
  createToken(element) {
    const token = {
      id: `token-${Date.now()}-${Math.random()}`,
      element: element
    }
    this.currentTokens.push(token)
    return token
  }
  
  removeToken(token) {
    const index = this.currentTokens.indexOf(token)
    if (index > -1) {
      this.currentTokens.splice(index, 1)
    }
  }
  
  highlightElement(elementId, markerClass) {
    this.canvas.addMarker(elementId, markerClass)
  }
  
  clearHighlight(elementId) {
    this.canvas.removeMarker(elementId, 'token')
    this.canvas.removeMarker(elementId, 'active')
  }
  
  isGateway(element) {
    return element.type && element.type.includes('Gateway')
  }
}
```

**验收标准**:
- [ ] 模拟引擎核心逻辑实现
- [ ] 支持开始、结束、任务、网关
- [ ] 令牌移动逻辑正确

---

##### 3.1.2 创建模拟控制面板（4小时）

**创建`SimulationPanel.vue`**:

```vue
<template>
  <div v-if="visible" class="simulation-panel">
    <div class="panel-header">
      <span>流程模拟</span>
      <el-button text @click="stopSimulation">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
    
    <div class="panel-content">
      <!-- 控制按钮 -->
      <div class="control-buttons">
        <el-button 
          type="primary" 
          @click="handleNext"
          :disabled="isFinished"
        >
          <el-icon><DArrowRight /></el-icon>
          下一步
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
      </div>
      
      <!-- 当前状态 -->
      <div class="current-state">
        <div class="state-label">当前节点</div>
        <div class="state-value">
          {{ currentElementName || '(未开始)' }}
        </div>
      </div>
      
      <!-- 变量面板 -->
      <div class="variables-section">
        <div class="section-title">流程变量</div>
        <div class="variable-list">
          <div 
            v-for="(value, key) in variables"
            :key="key"
            class="variable-item"
          >
            <span class="variable-name">{{ key }}:</span>
            <el-input 
              v-model="variables[key]"
              size="small"
              @change="updateVariable(key, $event)"
            />
          </div>
        </div>
        <el-button size="small" text @click="showAddVariable = true">
          + 添加变量
        </el-button>
      </div>
      
      <!-- 网关选择对话框 -->
      <el-dialog
        v-model="showGatewayChoice"
        title="选择分支"
        width="400px"
      >
        <div class="gateway-choices">
          <el-radio-group v-model="selectedFlow">
            <el-radio 
              v-for="choice in gatewayChoices"
              :key="choice.id"
              :label="choice.id"
              class="choice-item"
            >
              {{ choice.name }}
            </el-radio>
          </el-radio-group>
        </div>
        <template #footer>
          <el-button @click="showGatewayChoice = false">取消</el-button>
          <el-button type="primary" @click="confirmGatewayChoice">确定</el-button>
        </template>
      </el-dialog>
      
      <!-- 历史记录 -->
      <div class="history-section">
        <div class="section-title">执行历史</div>
        <div class="history-timeline">
          <div 
            v-for="(record, index) in history"
            :key="index"
            class="history-item"
          >
            <div class="history-time">
              {{ formatTime(record.timestamp) }}
            </div>
            <div class="history-message">
              {{ record.message }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { DArrowRight, RefreshLeft, Close } from '@element-plus/icons-vue'
import { ProcessSimulator } from './utils/processSimulator'

const visible = ref(false)
const simulator = ref(null)
const currentElementName = ref('')
const variables = ref({})
const history = ref([])
const isFinished = ref(false)

const showGatewayChoice = ref(false)
const gatewayChoices = ref([])
const selectedFlow = ref(null)

const start = (modeler) => {
  visible.value = true
  simulator.value = new ProcessSimulator(modeler)
  
  try {
    const result = simulator.value.start()
    history.value = simulator.value.history
    isFinished.value = false
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleNext = () => {
  if (!simulator.value) return
  
  const result = simulator.value.next()
  
  if (result.needChoice) {
    // 需要选择网关出口
    showGatewayChoice.value = true
    gatewayChoices.value = result.choices
  } else if (result.finished) {
    // 流程结束
    isFinished.value = true
    ElMessage.success('流程模拟完成')
  }
  
  if (result.currentElement) {
    currentElementName.value = result.currentElement.businessObject.name || result.currentElement.id
  }
  
  history.value = simulator.value.history
}

const confirmGatewayChoice = () => {
  if (!selectedFlow.value) {
    ElMessage.warning('请选择一个分支')
    return
  }
  
  const result = simulator.value.chooseGatewayOutgoing(selectedFlow.value)
  if (result.success) {
    showGatewayChoice.value = false
    selectedFlow.value = null
    if (result.currentElement) {
      currentElementName.value = result.currentElement.businessObject.name || result.currentElement.id
    }
  }
}

const handleReset = () => {
  if (simulator.value) {
    simulator.value.reset()
    currentElementName.value = ''
    history.value = []
    isFinished.value = false
  }
}

const stopSimulation = () => {
  handleReset()
  visible.value = false
}

const updateVariable = (key, value) => {
  if (simulator.value) {
    simulator.value.setVariable(key, value)
  }
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString()
}

defineExpose({ start })
</script>

<style scoped>
.simulation-panel {
  position: fixed;
  top: 100px;
  right: 340px;
  width: 320px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 100;
  max-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  background: #ecf5ff;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.control-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.current-state {
  padding: 12px;
  background: #f0f9ff;
  border-radius: 6px;
  margin-bottom: 15px;
}

.state-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.state-value {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

.variables-section,
.history-section {
  margin-bottom: 15px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #606266;
}

.variable-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.variable-name {
  font-size: 12px;
  color: #606266;
  min-width: 80px;
}

.history-timeline {
  max-height: 200px;
  overflow-y: auto;
}

.history-item {
  padding: 8px 10px;
  border-left: 2px solid #409eff;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.history-time {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.history-message {
  font-size: 12px;
  color: #606266;
}

.gateway-choices {
  padding: 10px;
}

.choice-item {
  display: block;
  padding: 10px;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

/* 令牌高亮样式 */
:deep(.token) {
  stroke: #67c23a !important;
  stroke-width: 4px !important;
  fill: rgba(103, 194, 58, 0.2) !important;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    stroke-width: 4px;
  }
  50% {
    stroke-width: 6px;
  }
}
</style>
```

**集成到流程设计器**:

```vue
<template>
  <!-- 在顶部工具栏添加模拟按钮 -->
  <el-button @click="startSimulation">
    <el-icon><VideoPlay /></el-icon> 模拟运行
  </el-button>
  
  <!-- 模拟面板 -->
  <SimulationPanel ref="simulationPanel" />
</template>

<script setup>
const simulationPanel = ref(null)

const startSimulation = () => {
  if (!modeler) {
    ElMessage.warning('请先创建流程')
    return
  }
  
  simulationPanel.value.start(modeler)
}
</script>
```

**验收标准**:
- [ ] 模拟面板显示正常
- [ ] 可以逐步执行流程
- [ ] 令牌高亮动画流畅
- [ ] 网关分支选择功能正常
- [ ] 变量管理功能正常
- [ ] 历史记录完整

---

##### 3.1.3 添加断点功能（2小时）

**在模拟器中添加断点支持**:

```javascript
// 在ProcessSimulator类中添加
class ProcessSimulator {
  constructor(modeler) {
    // ...
    this.breakpoints = new Set()
  }
  
  /**
   * 添加断点
   */
  addBreakpoint(elementId) {
    this.breakpoints.add(elementId)
    this.highlightElement(elementId, 'breakpoint')
  }
  
  /**
   * 移除断点
   */
  removeBreakpoint(elementId) {
    this.breakpoints.delete(elementId)
    this.clearHighlight(elementId)
  }
  
  /**
   * 检查是否到达断点
   */
  isBreakpoint(elementId) {
    return this.breakpoints.has(elementId)
  }
  
  // 在next()方法中添加断点检查
  next() {
    // ... 原有逻辑
    
    if (this.isBreakpoint(nextElement.id)) {
      return {
        success: true,
        paused: true,
        message: '已到达断点'
      }
    }
    
    // ...
  }
}
```

**添加断点UI**:

```vue
<!-- 在元素右键菜单中添加"切换断点"选项 -->
<template>
  <div class="context-menu-item" @click="toggleBreakpoint">
    <el-icon><Position /></el-icon>
    {{ hasBreakpoint ? '移除断点' : '添加断点' }}
  </div>
</template>
```

**验收标准**:
- [ ] 可以在元素上设置断点
- [ ] 断点元素有视觉标识
- [ ] 模拟运行时在断点处暂停
- [ ] 可以继续执行或移除断点

---

### 任务3.2: 协作功能 ⭐⭐
**优先级**: P3  
**工时**: 12-16小时  
**负责人**: 前端+后端  
**依赖**: 阶段1、2完成

#### 子任务

##### 3.2.1 流程评论系统（4小时）

**数据库设计**:

```sql
CREATE TABLE `process_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `design_id` bigint(20) NOT NULL COMMENT '流程设计ID',
  `element_id` varchar(100) COMMENT '元素ID（可选，针对特定元素的评论）',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `user_id` varchar(50) NOT NULL COMMENT '评论人ID',
  `user_name` varchar(100) COMMENT '评论人姓名',
  `parent_id` bigint(20) COMMENT '父评论ID（回复）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_design_id` (`design_id`),
  KEY `idx_element_id` (`element_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程评论表';
```

**前端组件**:

```vue
<template>
  <div class="comment-panel">
    <div class="comment-list">
      <div 
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <div class="comment-header">
          <span class="user-name">{{ comment.userName }}</span>
          <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div class="comment-actions">
          <el-button text size="small" @click="replyComment(comment)">
            回复
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="comment-input">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="添加评论..."
      />
      <el-button type="primary" @click="submitComment">
        发表评论
      </el-button>
    </div>
  </div>
</template>
```

---

##### 3.2.2 变更历史对比（4小时）

**创建Diff视图组件**:

```vue
<template>
  <el-dialog 
    v-model="visible"
    title="版本对比"
    width="90%"
  >
    <div class="version-compare">
      <div class="version-selector">
        <el-select v-model="version1" placeholder="选择版本1">
          <el-option
            v-for="v in versions"
            :key="v.version"
            :label="`v${v.version}`"
            :value="v.id"
          />
        </el-select>
        
        <el-icon><DArrowRight /></el-icon>
        
        <el-select v-model="version2" placeholder="选择版本2">
          <el-option
            v-for="v in versions"
            :key="v.version"
            :label="`v${v.version}`"
            :value="v.id"
          />
        </el-select>
        
        <el-button type="primary" @click="compareVersions">
          对比
        </el-button>
      </div>
      
      <div class="compare-result">
        <div class="version-panel">
          <div class="panel-title">版本 {{ version1 }}</div>
          <div ref="viewer1" class="bpmn-viewer"></div>
        </div>
        
        <div class="version-panel">
          <div class="panel-title">版本 {{ version2 }}</div>
          <div ref="viewer2" class="bpmn-viewer"></div>
        </div>
      </div>
      
      <div class="diff-summary">
        <div class="diff-stats">
          <el-tag type="success">新增: {{ diffStats.added }}</el-tag>
          <el-tag type="warning">修改: {{ diffStats.modified }}</el-tag>
          <el-tag type="danger">删除: {{ diffStats.deleted }}</el-tag>
        </div>
        
        <div class="diff-list">
          <div 
            v-for="diff in diffList"
            :key="diff.id"
            class="diff-item"
          >
            <el-tag :type="getDiffType(diff.type)">
              {{ diff.type }}
            </el-tag>
            <span>{{ diff.element }}: {{ diff.description }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>
```

---

##### 3.2.3 审批发布流程（4小时）

**工作流审批**:

```sql
CREATE TABLE `process_publish_approval` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `design_id` bigint(20) NOT NULL,
  `applicant` varchar(50) NOT NULL COMMENT '申请人',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING,APPROVED,REJECTED',
  `approver` varchar(50) COMMENT '审批人',
  `approval_comment` varchar(500) COMMENT '审批意见',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `approval_time` datetime COMMENT '审批时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程发布审批表';
```

---

## 📊 阶段3总结

**预计完成时间**: 22-30小时（2.75-3.75天）

**交付物清单**:
- [x] ProcessSimulator.js（模拟引擎）
- [x] SimulationPanel.vue（模拟面板）
- [x] CommentPanel.vue（评论系统）
- [x] VersionCompare.vue（版本对比）
- [x] ApprovalWorkflow（审批流程）

**验收标准**:
- [ ] 流程可以模拟运行，逐步执行
- [ ] 支持断点调试
- [ ] 可以添加评论和回复
- [ ] 版本对比功能完整
- [ ] 发布审批流程正常

---

## 📈 完整项目总结

### 总工时统计

| 阶段 | 内容 | 工时 | 工期 |
|-----|------|------|------|
| 阶段1 | 核心功能 | 20-26小时 | 2.5-3.5天 |
| 阶段2 | 增强功能 | 28-36小时 | 3.5-4.5天 |
| 阶段3 | 高级功能 | 22-30小时 | 2.75-3.75天 |
| **总计** | **完整版** | **70-92小时** | **8.75-11.75天** |

### 关键里程碑

**Week 1 (5天)**:
- Day 1-3: 阶段1核心功能
- Day 4-5: 阶段2表单设计器

**Week 2 (5天)**:
- Day 1-2: 阶段2验证+模板
- Day 3-5: 阶段3模拟+协作

### 交付成果

**前端组件** (12个):
1. ProcessDesigner.vue (升级)
2. Toolbar.vue (升级)
3. FormDesigner.vue (新增)
4. ValidationPanel.vue (新增)
5. TemplateLibrary.vue (新增)
6. SimulationPanel.vue (新增)
7. CommentPanel.vue (新增)
8. VersionCompare.vue (新增)
... 等

**后端API** (5个模块):
1. ProcessDesignController (升级)
2. FormController (新增)
3. TemplateController (新增)
4. CommentController (新增)
5. ApprovalController (新增)

**数据库表** (4张):
1. form_definition (表单定义)
2. process_template (流程模板)
3. process_comment (流程评论)
4. process_publish_approval (发布审批)

**工具类** (3个):
1. bpmnValidator.js (验证引擎)
2. processSimulator.js (模拟引擎)
3. bpmnDiffer.js (对比工具)

---

## 🎯 最终效果

### 功能对比

| 功能 | 当前 | 方案B完成后 |
|------|------|-----------|
| 属性配置 | ❌ 必须改SQL | ✅ 完全UI配置 |
| 元素连接 | ❌ 不支持 | ✅ 拖拽连接 |
| 表单设计 | ❌ 不支持 | ✅ 可视化设计 |
| 实时验证 | ❌ 不支持 | ✅ 实时高亮 |
| 流程模板 | ❌ 不支持 | ✅ 5个内置模板 |
| 流程模拟 | ❌ 不支持 | ✅ 逐步执行 |
| 团队协作 | ❌ 不支持 | ✅ 评论+审批 |
| **可用性** | 🔴 不可用 | ✅ 企业级 |

---

## 📞 讨论要点

建议在讨论时重点关注：

### 1. 工时与工期确认
- **问题**: 70-92小时的工作量是否合理？
- **建议**: 可以分阶段实施，先完成阶段1（核心），再根据反馈决定是否继续

### 2. 优先级调整
- **问题**: 是否有些功能可以延后或省略？
- **建议**: 
  - 必须做：阶段1（核心功能）
  - 重要做：阶段2.1表单 + 2.2验证
  - 可选做：阶段2.3模板 + 阶段3全部

### 3. 资源分配
- **问题**: 需要多少开发人员？
- **建议**: 
  - 前端开发 x 1 (70%工作量)
  - 后端开发 x 1 (30%工作量)
  - 可以并行开发

### 4. 技术风险
- **风险点**: 
  - bpmn-js版本兼容性
  - 表单设计器集成复杂度
  - 模拟引擎性能
- **缓解措施**: 
  - 前期做POC验证
  - 分阶段实施降低风险

### 5. 验收标准
- **问题**: 如何定义"完成"？
- **建议**: 每个阶段独立验收，有明确的验收清单

---

**准备好了吗？** 🚀

请告诉我：
1. 是否接受这个工作计划？
2. 是否需要调整优先级？
3. 何时可以开始实施？
4. 需要我协助实施哪个部分？

