<template>
  <div class="task-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon @click="goBack" style="cursor: pointer; margin-right: 10px;">
            <ArrowLeft />
          </el-icon>
          <span>任务详情</span>
        </div>
      </template>
      
      <div v-loading="loading">
        <!-- 任务信息 -->
        <div style="margin-bottom: 20px;">
          <div style="display: flex; align-items: center; margin-bottom: 15px;">
            <el-icon style="font-size: 20px; color: #409eff; margin-right: 10px;"><Document /></el-icon>
            <h3 style="margin: 0;">任务信息</h3>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="任务名称" label-class-name="label-bold">
              <el-tag type="primary" effect="plain">{{ taskInfo.name }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="流程定义" label-class-name="label-bold">
              {{ taskInfo.processDefinitionName }}
            </el-descriptions-item>
            <el-descriptions-item label="任务ID" label-class-name="label-bold">
              <el-text size="small" type="info">{{ taskInfo.id }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="流程实例ID" label-class-name="label-bold">
              <el-text size="small" type="info">{{ taskInfo.processInstanceId }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" label-class-name="label-bold">
              <el-icon style="margin-right: 6px; color: #909399;"><Clock /></el-icon>
              {{ formatTime(taskInfo.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="办理人" label-class-name="label-bold">
              <el-tag type="success" size="small">{{ taskInfo.assignee }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 流程变量 -->
        <div style="margin-top: 30px;">
          <div style="display: flex; align-items: center; margin-bottom: 15px;">
            <el-icon style="font-size: 20px; color: #67c23a; margin-right: 10px;"><InfoFilled /></el-icon>
            <h3 style="margin: 0;">流程信息</h3>
          </div>
          <el-descriptions :column="2" border style="margin-top: 15px;">
            <el-descriptions-item label="申请人">{{ variables.applicant || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审批人">{{ variables.manager || '-' }}</el-descriptions-item>
            <el-descriptions-item label="请假类型" v-if="variables.leaveType">
              {{ getLeaveTypeName(variables.leaveType) }}
            </el-descriptions-item>
            <el-descriptions-item label="请假天数" v-if="variables.days">
              {{ variables.days }} 天
            </el-descriptions-item>
            <el-descriptions-item label="开始日期" v-if="variables.startDate">
              {{ variables.startDate }}
            </el-descriptions-item>
            <el-descriptions-item label="结束日期" v-if="variables.endDate">
              {{ variables.endDate }}
            </el-descriptions-item>
            <el-descriptions-item label="请假原因" :span="2" v-if="variables.reason">
              {{ variables.reason }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 审批表单 -->
        <div style="margin-top: 30px;">
          <div style="display: flex; align-items: center; margin-bottom: 15px;">
            <el-icon style="font-size: 20px; color: #e6a23c; margin-right: 10px;"><Edit /></el-icon>
            <h3 style="margin: 0;">审批意见</h3>
          </div>
          <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-width="120px"
            style="margin-top: 15px;"
          >
            <el-form-item label="审批结果" prop="approved">
              <el-radio-group v-model="formData.approved">
                <el-radio :label="true">同意</el-radio>
                <el-radio :label="false">拒绝</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批意见" prop="comment">
              <el-input
                v-model="formData.comment"
                type="textarea"
                :rows="4"
                placeholder="请输入审批意见"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitTask" :loading="submitting">
                提交
              </el-button>
              <el-button @click="goBack">
                返回
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document, InfoFilled, Edit, Clock } from '@element-plus/icons-vue'
import { getTaskDetail, completeTask } from '@/api/workflow'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const taskId = ref(route.params.id)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const taskInfo = ref({})
const variables = ref({})

const formData = reactive({
  approved: true,
  comment: ''
})

// 获取请假类型名称
const getLeaveTypeName = (type) => {
  const typeMap = {
    'annual': '年假',
    'personal': '事假',
    'sick': '病假'
  }
  return typeMap[type] || type
}

const rules = {
  approved: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入审批意见', trigger: 'blur' }
  ]
}

// 加载任务详情
const loadTaskDetail = async () => {
  loading.value = true
  try {
    const res = await getTaskDetail(taskId.value)
    if (res.code === 200 && res.data) {
      taskInfo.value = res.data
      variables.value = res.data.variables || {}
    }
  } catch (error) {
    ElMessage.error('加载任务详情失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 提交任务
const submitTask = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await completeTask(taskId.value, formData)
        if (res.code === 200) {
          ElMessage.success('任务提交成功')
          router.push('/workflow/tasks')
        } else {
          ElMessage.error(res.message || '任务提交失败')
        }
      } catch (error) {
        ElMessage.error('任务提交失败: ' + error.message)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadTaskDetail()
})
</script>

<style scoped>
.task-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 10px;
}

:deep(.label-bold) {
  font-weight: 600;
  background-color: #f5f7fa;
}
</style>

