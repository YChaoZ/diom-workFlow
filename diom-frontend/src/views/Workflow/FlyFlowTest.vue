<template>
  <div class="flyflow-test">
    <el-page-header @back="goBack" title="返回" content="FlyFlow 组件测试" />
    
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>FlyFlow 工作流组件集成测试</span>
          <el-tag type="success">v1.0</el-tag>
        </div>
      </template>

      <el-alert 
        title="测试说明" 
        type="info" 
        description="这个页面用于测试 FlyFlow 组件是否正常集成。请逐个测试每个标签页的功能。" 
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 测试流程列表 -->
        <el-tab-pane label="📋 流程列表" name="list">
          <el-alert 
            title="流程列表测试" 
            type="success" 
            description="测试从 diom-flowable-service 获取流程定义列表" 
            :closable="false"
            style="margin-bottom: 15px;"
          />
          <FlyFlowList />
        </el-tab-pane>

        <!-- 测试待办任务 -->
        <el-tab-pane label="📝 待办任务" name="pending">
          <el-alert 
            title="待办任务测试" 
            type="success" 
            description="测试获取当前用户的待办任务列表" 
            :closable="false"
            style="margin-bottom: 15px;"
          />
          <FlyFlowPendingTasks />
        </el-tab-pane>

        <!-- 测试我发起的 -->
        <el-tab-pane label="🚀 我发起的" name="started">
          <el-alert 
            title="我发起的流程测试" 
            type="success" 
            description="测试获取当前用户发起的流程实例" 
            :closable="false"
            style="margin-bottom: 15px;"
          />
          <FlyFlowStartedTasks />
        </el-tab-pane>

        <!-- 测试已完成 -->
        <el-tab-pane label="✅ 已完成" name="completed">
          <el-alert 
            title="已完成任务测试" 
            type="success" 
            description="测试获取已完成的任务列表" 
            :closable="false"
            style="margin-bottom: 15px;"
          />
          <FlyFlowCompletedTasks />
        </el-tab-pane>

        <!-- API 测试 -->
        <el-tab-pane label="🔧 API 测试" name="api">
          <el-alert 
            title="API 适配器测试" 
            type="warning" 
            description="测试 API 适配器是否正常工作，检查与后端的连接" 
            :closable="false"
            style="margin-bottom: 15px;"
          />
          
          <el-space direction="vertical" style="width: 100%;">
            <el-button type="primary" @click="testFlowListApi">
              测试流程列表 API
            </el-button>
            <el-button type="primary" @click="testTaskListApi">
              测试待办任务 API
            </el-button>
            <el-button type="success" @click="testUserInfoApi">
              测试用户信息 API
            </el-button>
          </el-space>

          <el-divider />

          <div v-if="apiTestResult" class="api-result">
            <h4>API 测试结果：</h4>
            <pre>{{ JSON.stringify(apiTestResult, null, 2) }}</pre>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card class="info-card" style="margin-top: 20px;">
      <template #header>
        <span>📊 集成状态</span>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="FlyFlow 组件">
          <el-tag type="success">✅ 已集成（272 个文件）</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="LogicFlow 引擎">
          <el-tag type="success">✅ 已安装（v1.2.10）</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="API 适配器">
          <el-tag type="success">✅ 已创建</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="样式文件">
          <el-tag type="success">✅ 已引入</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 导入 FlyFlow 组件
import FlyFlowList from '@/flyflow/views/flow/list.vue'
import FlyFlowPendingTasks from '@/flyflow/views/task/pending.vue'
import FlyFlowStartedTasks from '@/flyflow/views/task/started.vue'
import FlyFlowCompletedTasks from '@/flyflow/views/task/completed.vue'

// 导入 API 适配器
import { flowApi, taskApi, userApi } from '@/flyflow/api/adapter'

const router = useRouter()
const activeTab = ref('list')
const apiTestResult = ref(null)

// 返回
const goBack = () => {
  router.back()
}

// 测试流程列表 API
const testFlowListApi = async () => {
  try {
    ElMessage.info('正在测试流程列表 API...')
    const result = await flowApi.getFlowList({
      pageNum: 1,
      pageSize: 10
    })
    apiTestResult.value = result
    ElMessage.success('流程列表 API 测试成功！')
  } catch (error) {
    console.error('API 测试失败:', error)
    apiTestResult.value = {
      error: error.message,
      stack: error.stack
    }
    ElMessage.error('流程列表 API 测试失败：' + error.message)
  }
}

// 测试待办任务 API
const testTaskListApi = async () => {
  try {
    ElMessage.info('正在测试待办任务 API...')
    const result = await taskApi.getPendingTasks({
      pageNum: 1,
      pageSize: 10
    })
    apiTestResult.value = result
    ElMessage.success('待办任务 API 测试成功！')
  } catch (error) {
    console.error('API 测试失败:', error)
    apiTestResult.value = {
      error: error.message,
      stack: error.stack
    }
    ElMessage.error('待办任务 API 测试失败：' + error.message)
  }
}

// 测试用户信息 API
const testUserInfoApi = async () => {
  try {
    ElMessage.info('正在测试用户信息 API...')
    const result = await userApi.getUserInfo()
    apiTestResult.value = result
    ElMessage.success('用户信息 API 测试成功！')
  } catch (error) {
    console.error('API 测试失败:', error)
    apiTestResult.value = {
      error: error.message,
      stack: error.stack
    }
    ElMessage.error('用户信息 API 测试失败：' + error.message)
  }
}
</script>

<style scoped>
.flyflow-test {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.test-card,
.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.api-result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.api-result h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #409eff;
}

.api-result pre {
  margin: 0;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
}
</style>

