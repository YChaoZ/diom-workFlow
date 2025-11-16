<template>
  <div class="bpmn-viewer">
    <div class="bpmn-toolbar">
      <el-button-group>
        <el-button size="small" @click="zoomIn" :icon="ZoomIn">放大</el-button>
        <el-button size="small" @click="zoomOut" :icon="ZoomOut">缩小</el-button>
        <el-button size="small" @click="zoomReset" :icon="FullScreen">适应画布</el-button>
      </el-button-group>
    </div>
    <div ref="bpmnContainer" class="bpmn-container" v-loading="loading"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { ZoomIn, ZoomOut, FullScreen } from '@element-plus/icons-vue'
import BpmnViewer from 'bpmn-js/lib/NavigatedViewer'

const props = defineProps({
  xml: {
    type: String,
    required: true
  },
  activeNodes: {
    type: Array,
    default: () => []
  }
})

const bpmnContainer = ref(null)
const loading = ref(false)
let viewer = null

// 初始化BPMN查看器
const initViewer = () => {
  if (bpmnContainer.value && !viewer) {
    viewer = new BpmnViewer({
      container: bpmnContainer.value,
      height: 600
    })
  }
}

// 加载BPMN XML
const loadDiagram = async () => {
  if (!viewer || !props.xml) return
  
  loading.value = true
  try {
    await viewer.importXML(props.xml)
    
    // 适应画布
    const canvas = viewer.get('canvas')
    canvas.zoom('fit-viewport', 'auto')
    
    // 高亮活动节点
    highlightActiveNodes()
  } catch (err) {
    console.error('Failed to render BPMN diagram:', err)
  } finally {
    loading.value = false
  }
}

// 高亮活动节点
const highlightActiveNodes = () => {
  if (!viewer || !props.activeNodes || props.activeNodes.length === 0) return
  
  const canvas = viewer.get('canvas')
  const overlays = viewer.get('overlays')
  
  // 清除之前的高亮
  overlays.clear()
  
  props.activeNodes.forEach(nodeId => {
    try {
      // 添加高亮样式
      canvas.addMarker(nodeId, 'highlight-active')
      
      // 添加overlay标记
      overlays.add(nodeId, {
        position: {
          top: -20,
          left: 0
        },
        html: '<div class="active-badge">进行中</div>'
      })
    } catch (err) {
      console.warn(`Failed to highlight node ${nodeId}:`, err)
    }
  })
}

// 缩放功能
const zoomIn = () => {
  if (!viewer) return
  const zoomScroll = viewer.get('zoomScroll')
  zoomScroll.stepZoom(0.1)
}

const zoomOut = () => {
  if (!viewer) return
  const zoomScroll = viewer.get('zoomScroll')
  zoomScroll.stepZoom(-0.1)
}

const zoomReset = () => {
  if (!viewer) return
  const canvas = viewer.get('canvas')
  canvas.zoom('fit-viewport', 'auto')
}

// 监听XML变化
watch(() => props.xml, () => {
  if (props.xml) {
    loadDiagram()
  }
})

// 监听活动节点变化
watch(() => props.activeNodes, () => {
  highlightActiveNodes()
}, { deep: true })

onMounted(() => {
  initViewer()
  if (props.xml) {
    loadDiagram()
  }
})

onBeforeUnmount(() => {
  if (viewer) {
    viewer.destroy()
    viewer = null
  }
})
</script>

<style scoped>
.bpmn-viewer {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.bpmn-toolbar {
  padding: 10px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.bpmn-container {
  width: 100%;
  height: 600px;
  background-color: #ffffff;
}

/* BPMN高亮样式 */
:deep(.highlight-active .djs-visual > *) {
  stroke: #67c23a !important;
  stroke-width: 3px !important;
  fill: #f0f9ff !important;
}

:deep(.active-badge) {
  background-color: #67c23a;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>

<style>
/* BPMN.js默认样式（不能使用scoped） */
@import 'bpmn-js/dist/assets/diagram-js.css';
@import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
@import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css';
@import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
</style>

