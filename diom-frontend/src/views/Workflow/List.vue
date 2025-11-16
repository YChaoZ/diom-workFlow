<template>
  <div class="workflow-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程定义列表</span>
        </div>
      </template>
      
      <el-table :data="processList" v-loading="loading" style="width: 100%">
        <el-table-column prop="key" label="流程Key" width="200" />
        <el-table-column prop="name" label="流程名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              type="primary"
              text
              @click="startProcess(row.key)"
            >
              发起流程
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProcessDefinitions } from '@/api/workflow'

const router = useRouter()
const loading = ref(false)
const processList = ref([])

// 加载流程定义列表
const loadProcessList = async () => {
  loading.value = true
  try {
    const res = await getProcessDefinitions()
    if (res.code === 200) {
      processList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载流程列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 启动流程
const startProcess = (processKey) => {
  router.push(`/workflow/start/${processKey}`)
}

onMounted(() => {
  loadProcessList()
})
</script>

<style scoped>
.workflow-list {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>

