<template>
  <div class="bpmn-toolbar">
    <div class="toolbar-section">
      <div class="section-title">事件</div>
      <div 
        v-for="item in events"
        :key="item.type"
        class="toolbar-item"
        draggable="true"
        @dragstart="handleDragStart($event, item)"
        :title="item.label"
      >
        <div class="item-icon" :style="{ backgroundColor: item.color }">
          {{ item.icon }}
        </div>
        <div class="item-label">{{ item.label }}</div>
      </div>
    </div>

    <div class="toolbar-section">
      <div class="section-title">任务</div>
      <div 
        v-for="item in tasks"
        :key="item.type"
        class="toolbar-item"
        draggable="true"
        @dragstart="handleDragStart($event, item)"
        :title="item.label"
      >
        <div class="item-icon" :style="{ backgroundColor: item.color }">
          {{ item.icon }}
        </div>
        <div class="item-label">{{ item.label }}</div>
      </div>
    </div>

    <div class="toolbar-section">
      <div class="section-title">网关</div>
      <div 
        v-for="item in gateways"
        :key="item.type"
        class="toolbar-item"
        draggable="true"
        @dragstart="handleDragStart($event, item)"
        :title="item.label"
      >
        <div class="item-icon" :style="{ backgroundColor: item.color }">
          {{ item.icon }}
        </div>
        <div class="item-label">{{ item.label }}</div>
      </div>
    </div>

    <div class="toolbar-section">
      <div class="section-title">其他</div>
      <div 
        v-for="item in others"
        :key="item.type"
        class="toolbar-item"
        draggable="true"
        @dragstart="handleDragStart($event, item)"
        :title="item.label"
      >
        <div class="item-icon" :style="{ backgroundColor: item.color }">
          {{ item.icon }}
        </div>
        <div class="item-label">{{ item.label }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface ToolbarItem {
  type: string
  label: string
  icon: string
  color: string
  bpmnType: string
}

// 事件类型
const events = ref<ToolbarItem[]>([
  {
    type: 'start-event',
    label: '开始',
    icon: '●',
    color: '#52c41a',
    bpmnType: 'bpmn:StartEvent'
  },
  {
    type: 'end-event',
    label: '结束',
    icon: '◉',
    color: '#f5222d',
    bpmnType: 'bpmn:EndEvent'
  },
  {
    type: 'intermediate-event',
    label: '中间事件',
    icon: '◎',
    color: '#faad14',
    bpmnType: 'bpmn:IntermediateCatchEvent'
  },
  {
    type: 'boundary-event',
    label: '边界事件',
    icon: '⊙',
    color: '#722ed1',
    bpmnType: 'bpmn:BoundaryEvent'
  }
])

// 任务类型
const tasks = ref<ToolbarItem[]>([
  {
    type: 'user-task',
    label: '用户任务',
    icon: '👤',
    color: '#1890ff',
    bpmnType: 'bpmn:UserTask'
  },
  {
    type: 'service-task',
    label: '服务任务',
    icon: '⚙',
    color: '#13c2c2',
    bpmnType: 'bpmn:ServiceTask'
  },
  {
    type: 'script-task',
    label: '脚本任务',
    icon: '📝',
    color: '#722ed1',
    bpmnType: 'bpmn:ScriptTask'
  },
  {
    type: 'send-task',
    label: '发送任务',
    icon: '📤',
    color: '#fa8c16',
    bpmnType: 'bpmn:SendTask'
  },
  {
    type: 'receive-task',
    label: '接收任务',
    icon: '📥',
    color: '#fa541c',
    bpmnType: 'bpmn:ReceiveTask'
  },
  {
    type: 'manual-task',
    label: '手工任务',
    icon: '✋',
    color: '#52c41a',
    bpmnType: 'bpmn:ManualTask'
  },
  {
    type: 'business-rule-task',
    label: '业务规则',
    icon: '📋',
    color: '#2f54eb',
    bpmnType: 'bpmn:BusinessRuleTask'
  }
])

// 网关类型
const gateways = ref<ToolbarItem[]>([
  {
    type: 'exclusive-gateway',
    label: '排他网关',
    icon: '◇',
    color: '#faad14',
    bpmnType: 'bpmn:ExclusiveGateway'
  },
  {
    type: 'parallel-gateway',
    label: '并行网关',
    icon: '✚',
    color: '#fa8c16',
    bpmnType: 'bpmn:ParallelGateway'
  },
  {
    type: 'inclusive-gateway',
    label: '包容网关',
    icon: '◯',
    color: '#eb2f96',
    bpmnType: 'bpmn:InclusiveGateway'
  },
  {
    type: 'event-based-gateway',
    label: '事件网关',
    icon: '⬡',
    color: '#722ed1',
    bpmnType: 'bpmn:EventBasedGateway'
  }
])

// 其他元素
const others = ref<ToolbarItem[]>([
  {
    type: 'subprocess',
    label: '子流程',
    icon: '▭',
    color: '#597ef7',
    bpmnType: 'bpmn:SubProcess'
  },
  {
    type: 'call-activity',
    label: '调用活动',
    icon: '📞',
    color: '#9254de',
    bpmnType: 'bpmn:CallActivity'
  },
  {
    type: 'data-object',
    label: '数据对象',
    icon: '📄',
    color: '#13c2c2',
    bpmnType: 'bpmn:DataObjectReference'
  },
  {
    type: 'data-store',
    label: '数据存储',
    icon: '🗄',
    color: '#1890ff',
    bpmnType: 'bpmn:DataStoreReference'
  },
  {
    type: 'pool',
    label: '池',
    icon: '▬',
    color: '#52c41a',
    bpmnType: 'bpmn:Participant'
  }
])

// Emits
const emit = defineEmits<{
  dragStart: [item: ToolbarItem, event: DragEvent]
}>()

// 拖拽开始
const handleDragStart = (event: DragEvent, item: ToolbarItem) => {
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy'
    event.dataTransfer.setData('bpmn-type', item.bpmnType)
    event.dataTransfer.setData('item', JSON.stringify(item))
  }
  emit('dragStart', item, event)
}
</script>

<style scoped>
.bpmn-toolbar {
  width: 220px;
  height: 100%;
  background: #fafafa;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  padding: 16px 12px;
}

.toolbar-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-left: 4px;
  border-left: 3px solid #1890ff;
}

.toolbar-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  margin-bottom: 8px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.3s ease;
}

.toolbar-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
  transform: translateX(4px);
}

.toolbar-item:active {
  cursor: grabbing;
  transform: scale(0.98);
}

.item-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 16px;
  margin-right: 10px;
  color: #fff;
  flex-shrink: 0;
}

.item-label {
  font-size: 13px;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bpmn-toolbar::-webkit-scrollbar {
  width: 6px;
}

.bpmn-toolbar::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.bpmn-toolbar::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}

.bpmn-toolbar::-webkit-scrollbar-track {
  background: #f0f0f0;
}
</style>

