<template>
  <div class="process-designer-container">
    <!-- 顶部工具栏 -->
    <div class="designer-toolbar">
      <div class="toolbar-left">
        <el-input 
          v-model="processName" 
          placeholder="流程名称" 
          style="width: 300px; margin-right: 10px"
          :disabled="viewMode"
        />
        <el-input 
          v-model="processKey" 
          placeholder="流程Key（唯一标识）" 
          style="width: 200px; margin-right: 10px"
          :disabled="isEdit"
        />
        <el-select 
          v-model="category" 
          placeholder="流程分类" 
          style="width: 150px"
          :disabled="viewMode"
        >
          <el-option label="人事" value="人事" />
          <el-option label="财务" value="财务" />
          <el-option label="行政" value="行政" />
          <el-option label="采购" value="采购" />
          <el-option label="其他" value="其他" />
        </el-select>
      </div>
      
      <div class="toolbar-right">
        <el-button @click="saveDraft" :loading="saving" v-if="!viewMode">
          <el-icon><DocumentAdd /></el-icon> 保存草稿
        </el-button>
        <el-button @click="validate" :loading="validating">
          <el-icon><CircleCheck /></el-icon> 验证
        </el-button>
        <el-button type="primary" @click="publish" :loading="publishing" v-if="!viewMode">
          <el-icon><Upload /></el-icon> 发布
        </el-button>
        <el-button @click="viewHistory" v-if="designId">
          <el-icon><Clock /></el-icon> 历史版本
        </el-button>
        <el-button @click="exportXML">
          <el-icon><Download /></el-icon> 导出XML
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon> 返回
        </el-button>
      </div>
    </div>
    
    <!-- 主内容区 -->
    <div class="designer-content">
      <!-- 左侧：自定义工具栏 -->
      <Toolbar @drag-start="handleToolbarDragStart" />
      
      <!-- 中间：BPMN画布 -->
      <div 
        class="designer-canvas"
        @drop="handleDrop"
        @dragover="handleDragOver"
      >
        <div ref="bpmnCanvas" class="bpmn-container"></div>
      </div>
      
      <!-- 右侧：属性面板 -->
      <div class="designer-properties">
        <div ref="propertiesPanel" class="properties-container"></div>
      </div>
    </div>
    
    <!-- 历史版本对话框 -->
    <el-dialog 
      v-model="historyDialogVisible" 
      title="变更历史" 
      width="800px"
    >
      <el-timeline>
        <el-timeline-item
          v-for="item in historyList"
          :key="item.id"
          :timestamp="item.createTime"
          placement="top"
        >
          <el-card>
            <template #header>
              <div class="history-header">
                <el-tag :type="getActionType(item.action)">{{ getActionLabel(item.action) }}</el-tag>
                <span style="margin-left: 10px">版本 v{{ item.version }}</span>
              </div>
            </template>
            <div>
              <p><strong>操作人：</strong>{{ item.operatorName }}</p>
              <p><strong>变更说明：</strong>{{ item.changeDescription || '无' }}</p>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  DocumentAdd, 
  CircleCheck, 
  Upload, 
  Clock, 
  Download, 
  Back 
} from '@element-plus/icons-vue'

// 引入bpmn-js
import BpmnModeler from 'bpmn-js/lib/Modeler'
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule
} from 'bpmn-js-properties-panel'
import CamundaBpmnModdle from 'camunda-bpmn-moddle/resources/camunda.json'

// 引入Camunda行为模块
import CamundaBehaviorsModule from 'camunda-bpmn-js-behaviors/lib/camunda-platform'

// 引入自定义Toolbar
import Toolbar from './Toolbar.vue'

// 引入样式
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import 'bpmn-js-properties-panel/dist/assets/properties-panel.css'

// 引入API
import {
  getProcessDesignById,
  saveProcessDesign,
  validateBpmn,
  publishProcess,
  getProcessHistory
} from '@/api/processDesign'

const route = useRoute()
const router = useRouter()

// 数据
const bpmnCanvas = ref(null)
const propertiesPanel = ref(null)
let modeler = null

const designId = ref(null)
const processName = ref('')
const processKey = ref('')
const category = ref('')
const description = ref('')
const viewMode = ref(false)
const isEdit = ref(false)

const saving = ref(false)
const validating = ref(false)
const publishing = ref(false)

const historyDialogVisible = ref(false)
const historyList = ref([])

// 空白BPMN模板
const emptyBpmn = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" 
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn" 
                  id="Definitions_1" 
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" name="新建流程" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="159" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="186" y="202" width="22" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

// 生命周期
onMounted(() => {
  initBpmnModeler()
  
  // 判断是新建还是编辑
  if (route.params.id) {
    designId.value = parseInt(route.params.id)
    isEdit.value = true
    loadExistingProcess(designId.value)
  } else {
    // 新建流程
    createNewProcess()
  }
})

onBeforeUnmount(() => {
  if (modeler) {
    modeler.destroy()
  }
})

// 初始化BPMN建模器
const initBpmnModeler = () => {
  modeler = new BpmnModeler({
    container: bpmnCanvas.value,
    propertiesPanel: {
      parent: propertiesPanel.value
    },
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule,
      CamundaPlatformPropertiesProviderModule,  // ⭐ Camunda属性提供器
      // CamundaBehaviorsModule  // ⭐ 暂时注释以测试Context Pad
    ],
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
  
  // ⭐⭐ Phase 1.2: 调试Context Pad
  // 检查Context Pad服务是否正确加载
  try {
    const contextPad = modeler.get('contextPad')
    const contextPadProvider = modeler.get('contextPadProvider')
    console.log('✅ Context Pad loaded:', contextPad)
    console.log('✅ Context Pad Provider loaded:', contextPadProvider)
    
    // 获取所有已加载的模块（用于调试）
    const injector = modeler.get('injector')
    console.log('📦 Injector loaded:', injector)
  } catch (e) {
    console.error('❌ Context Pad loading error:', e)
  }
  
  // 隐藏默认Palette
  const canvas = modeler.get('canvas')
  const paletteContainer = canvas._container.parentNode.querySelector('.djs-palette')
  if (paletteContainer) {
    paletteContainer.style.display = 'none'
  }
}

// Toolbar拖拽开始
const handleToolbarDragStart = (item, event) => {
  console.log('Drag start:', item)
}

// 处理拖拽悬停
const handleDragOver = (event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'copy'
}

// 处理放置
const handleDrop = (event) => {
  event.preventDefault()
  
  try {
    const bpmnType = event.dataTransfer.getData('bpmn-type')
    const itemData = event.dataTransfer.getData('item')
    
    if (!bpmnType || !modeler) {
      return
    }
    
    const item = JSON.parse(itemData)
    console.log('Drop item:', item)
    
    // 获取画布坐标
    const canvas = modeler.get('canvas')
    const elementFactory = modeler.get('elementFactory')
    const create = modeler.get('create')
    const canvasRect = bpmnCanvas.value.getBoundingClientRect()
    
    // 计算相对于画布的坐标
    const x = event.clientX - canvasRect.left
    const y = event.clientY - canvasRect.top
    
    // 转换为画布坐标
    const viewbox = canvas.viewbox()
    const canvasX = (x - viewbox.x) / viewbox.scale
    const canvasY = (y - viewbox.y) / viewbox.scale
    
    // 创建元素
    const shape = elementFactory.createShape({
      type: bpmnType
    })
    
    // 在指定位置创建元素
    create.start(event, shape, {
      x: canvasX,
      y: canvasY
    })
    
    ElMessage.success(`已添加${item.label}`)
  } catch (error) {
    console.error('Drop error:', error)
    ElMessage.error('添加元素失败')
  }
}

// 创建新流程
const createNewProcess = async () => {
  try {
    await modeler.importXML(emptyBpmn)
    
    // 尝试适应视口缩放，如果失败则使用默认缩放
    try {
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
    } catch (zoomErr) {
      console.warn('Zoom fit-viewport failed, using default zoom', zoomErr)
      // 使用默认缩放1.0作为后备方案
      const canvas = modeler.get('canvas')
      canvas.zoom(1.0)
    }
  } catch (err) {
    console.error('创建新流程失败', err)
    ElMessage.error('创建新流程失败')
  }
}

// 加载已有流程
const loadExistingProcess = async (id) => {
  try {
    const response = await getProcessDesignById(id)
    if (response.code === 200) {
      const design = response.data
      
      processName.value = design.processName
      processKey.value = design.processKey
      category.value = design.category
      description.value = design.description || ''
      viewMode.value = design.status === 'PUBLISHED'
      
      // 导入BPMN
      await modeler.importXML(design.bpmnXml)
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
      
      ElMessage.success('流程加载成功')
    }
  } catch (err) {
    console.error('加载流程失败', err)
    ElMessage.error('加载流程失败')
  }
}

// 保存草稿
const saveDraft = async () => {
  if (!processName.value) {
    ElMessage.warning('请输入流程名称')
    return
  }
  if (!processKey.value) {
    ElMessage.warning('请输入流程Key')
    return
  }
  
  try {
    saving.value = true
    
    // 获取BPMN XML
    const { xml } = await modeler.saveXML({ format: true })
    
    // 更新BPMN中的流程ID和名称
    const updatedXml = xml
      .replace(/id="Process_\d+"/, `id="${processKey.value}"`)
      .replace(/name="[^"]*"/, `name="${processName.value}"`)
      .replace(/bpmnElement="Process_\d+"/, `bpmnElement="${processKey.value}"`)
    
    // 调用API保存
    const response = await saveProcessDesign({
      id: designId.value,
      processKey: processKey.value,
      processName: processName.value,
      bpmnXml: updatedXml,
      description: description.value,
      category: category.value
    })
    
    if (response.code === 200) {
      designId.value = response.data.id
      isEdit.value = true
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (err) {
    console.error('保存草稿失败', err)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 验证BPMN
const validate = async () => {
  try {
    validating.value = true
    
    const { xml } = await modeler.saveXML({ format: true })
    
    const response = await validateBpmn({ bpmnXml: xml })
    
    if (response.code === 200 && response.data.valid) {
      ElMessage.success('验证通过！流程设计正确')
    } else {
      // 显示错误列表
      const errors = response.data.errors || []
      const errorMsg = errors.map(e => e.message).join('\n')
      ElMessageBox.alert(errorMsg, '验证失败', {
        confirmButtonText: '确定',
        type: 'error'
      })
    }
  } catch (err) {
    console.error('验证失败', err)
    
    // 检查是否有详细的验证错误信息（从axios拦截器reject的错误中提取）
    if (err.response && err.response.data) {
      const data = err.response.data
      if (data.data && data.data.errors && data.data.errors.length > 0) {
        // 显示详细的验证错误
        const errorMsg = data.data.errors.map(e => e.message).join('\n')
        ElMessageBox.alert(errorMsg, 'BPMN验证失败', {
          confirmButtonText: '确定',
          type: 'error'
        })
        return
      } else if (data.message) {
        // 显示后端返回的错误消息
        ElMessage.error(data.message)
        return
      }
    }
    
    // 如果没有详细错误信息，显示通用错误
    ElMessage.error('验证失败：' + (err.message || '未知错误'))
  } finally {
    validating.value = false
  }
}

// 发布流程
const publish = async () => {
  if (!designId.value) {
    ElMessage.warning('请先保存草稿')
    return
  }
  
  try {
    const { value } = await ElMessageBox.prompt('请输入变更说明', '发布流程', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '变更说明不能为空'
    })
    
    publishing.value = true
    
    const response = await publishProcess({
      id: designId.value,
      changeDescription: value
    })
    
    if (response.code === 200) {
      ElMessage.success('发布成功')
      router.push('/workflow/design/list')
    } else {
      ElMessage.error(response.message || '发布失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('发布失败', err)
      ElMessage.error('发布失败')
    }
  } finally {
    publishing.value = false
  }
}

// 查看历史
const viewHistory = async () => {
  try {
    const response = await getProcessHistory(designId.value)
    if (response.code === 200) {
      historyList.value = response.data
      historyDialogVisible.value = true
    }
  } catch (err) {
    console.error('查询历史失败', err)
    ElMessage.error('查询历史失败')
  }
}

// 导出XML
const exportXML = async () => {
  try {
    const { xml } = await modeler.saveXML({ format: true })
    
    const blob = new Blob([xml], { type: 'application/xml' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${processKey.value || 'process'}.bpmn`
    a.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (err) {
    console.error('导出失败', err)
    ElMessage.error('导出失败')
  }
}

// 返回
const goBack = () => {
  router.push('/workflow/design/list')
}

// 获取操作类型标签
const getActionType = (action) => {
  const types = {
    'CREATE': 'info',
    'UPDATE': 'warning',
    'PUBLISH': 'success',
    'DEPRECATE': 'danger'
  }
  return types[action] || 'info'
}

// 获取操作标签
const getActionLabel = (action) => {
  const labels = {
    'CREATE': '创建',
    'UPDATE': '更新',
    'PUBLISH': '发布',
    'DEPRECATE': '废弃'
  }
  return labels[action] || action
}
</script>

<style scoped>
.process-designer-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.designer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.designer-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.designer-canvas {
  flex: 1;
  background: #fff;
  border-right: 1px solid #e4e7ed;
}

.bpmn-container {
  width: 100%;
  height: 100%;
}

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

.history-header {
  display: flex;
  align-items: center;
}

/* bpmn-js样式覆盖 */
:deep(.bjs-powered-by) {
  display: none;
}

:deep(.djs-palette) {
  left: 20px;
  top: 20px;
}

:deep(.djs-container) {
  background-color: #fafafa;
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
</style>

