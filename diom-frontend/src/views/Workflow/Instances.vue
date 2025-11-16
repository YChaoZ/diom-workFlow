<template>
  <div class="workflow-instances">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程实例</span>
          <el-button type="primary" @click="loadInstances" :icon="Refresh">
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table 
        :data="instanceList" 
        v-loading="loading" 
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无流程实例'"
        stripe
        highlight-current-row
      >
        <el-table-column prop="processDefinitionName" label="流程名称" min-width="150">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 8px; color: #409eff;"><Files /></el-icon>
              <span style="font-weight: 500;">{{ row.processDefinitionName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="实例ID" width="280" show-overflow-tooltip />
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 6px; color: #909399;"><Clock /></el-icon>
              <span>{{ formatTime(row.startTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.endTime" type="success" effect="dark">
              <el-icon><CircleCheck /></el-icon>
              <span style="margin-left: 4px;">已结束</span>
            </el-tag>
            <el-tag v-else type="warning" effect="dark">
              <el-icon><Loading /></el-icon>
              <span style="margin-left: 4px;">进行中</span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewDiagram(row)"
            >
              流程图
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="viewHistory(row.id)"
            >
              查看历史
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="instanceList.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- BPMN流程图对话框 -->
    <el-dialog
      v-model="diagramDialogVisible"
      :title="`${currentInstance?.processDefinitionName || '流程图'}`"
      width="90%"
      top="5vh"
    >
      <BpmnViewer 
        v-if="diagramXml" 
        :xml="diagramXml"
        :activeNodes="activeNodes"
      />
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="流程历史"
      width="800px"
    >
      <el-timeline>
        <el-timeline-item
          v-for="item in historyList"
          :key="item.id"
          :timestamp="item.time"
          placement="top"
        >
          <el-card>
            <h4>{{ item.activityName }}</h4>
            <p>{{ item.activityType }}</p>
            <p v-if="item.assignee">办理人: {{ item.assignee }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Files, Clock, CircleCheck, Loading } from '@element-plus/icons-vue'
import { getProcessDefinitions, getHistoricProcessInstances, getHistoricTasks, getProcessDefinitionXml } from '@/api/workflow'
import { useUserStore } from '@/stores/user'
import { formatTime } from '@/utils/format'
import BpmnViewer from '@/components/BpmnViewer.vue'

const userStore = useUserStore()

const loading = ref(false)
const instanceList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const historyDialogVisible = ref(false)
const historyList = ref([])

const diagramDialogVisible = ref(false)
const diagramXml = ref('')
const activeNodes = ref([])
const currentInstance = ref(null)

// 加载流程实例列表（合并所有流程的历史记录）
const loadInstances = async () => {
  loading.value = true
  try {
    // 先获取所有流程定义
    const defsRes = await getProcessDefinitions()
    if (defsRes.code === 200 && defsRes.data) {
      const allInstances = []
      
      // 遍历每个流程定义，获取其历史实例
      for (const def of defsRes.data) {
        try {
          const historyRes = await getHistoricProcessInstances(def.key)
          if (historyRes.code === 200 && historyRes.data) {
              allInstances.push(...historyRes.data.map(item => ({
                ...item,
                processDefinitionName: def.name
              })))
          }
        } catch (err) {
          console.error(`获取流程 ${def.key} 的历史失败:`, err)
        }
      }
      
      // 按开始时间倒序排序
      instanceList.value = allInstances.sort((a, b) => 
        new Date(b.startTime) - new Date(a.startTime)
      )
      total.value = instanceList.value.length
    }
  } catch (error) {
    ElMessage.error('加载流程实例失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 查看流程图
const viewDiagram = async (instance) => {
  currentInstance.value = instance
  try {
    // 获取流程定义Key（从processDefinitionId中提取，格式为：key:version:id）
    const processKey = instance.processDefinitionKey || instance.processDefinitionId?.split(':')[0]
    
    if (!processKey) {
      ElMessage.error('无法获取流程定义Key')
      return
    }
    
    // 获取流程定义XML
    const xmlRes = await getProcessDefinitionXml(processKey)
    if (xmlRes.code === 200 && xmlRes.data) {
      diagramXml.value = xmlRes.data
      
      // TODO: 获取当前活动节点（高亮功能）
      // 如果流程正在进行中，高亮当前节点
      if (!instance.endTime) {
        // 这里可以调用API获取当前活动节点
        // activeNodes.value = await getActiveNodes(instance.id)
        activeNodes.value = []
      } else {
        activeNodes.value = []
      }
      
      diagramDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载流程图失败: ' + error.message)
  }
}

// 查看历史
const viewHistory = async (processInstanceId) => {
  try {
    const res = await getHistoricTasks(processInstanceId)
    if (res.code === 200) {
          historyList.value = (res.data || []).map(task => ({
            id: task.id,
            activityName: task.name,
            activityType: '用户任务',
            assignee: task.assignee,
            time: formatTime(task.startTime),
            endTime: formatTime(task.endTime)
          }))
      historyDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载流程历史失败: ' + error.message)
  }
}

// 分页改变
const handlePageChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  loadInstances()
})
</script>

<style scoped>
.workflow-instances {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

