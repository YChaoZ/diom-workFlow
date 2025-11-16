<template>
  <div class="workflow-start">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon @click="goBack" style="cursor: pointer; margin-right: 10px;">
            <ArrowLeft />
          </el-icon>
          <span>发起流程</span>
        </div>
      </template>
      
      <!-- 模板、草稿和历史参数选择 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-select
            v-model="selectedTemplate"
            placeholder="📋 选择模板快速填充"
            clearable
            @change="loadTemplate"
            style="width: 100%"
          >
            <el-option
              v-for="template in templates"
              :key="template.id"
              :label="template.templateName"
              :value="template.id"
            >
              <span>{{ template.templateName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px;">
                使用{{ template.useCount }}次
              </span>
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="selectedDraft"
            placeholder="💾 继续编辑草稿"
            clearable
            @change="loadDraft"
            style="width: 100%"
          >
            <el-option
              v-for="draft in drafts"
              :key="draft.id"
              :label="draft.draftName || '未命名草稿'"
              :value="draft.id"
            >
              <span>{{ draft.draftName || '未命名草稿' }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px;">
                {{ formatTime(draft.updateTime) }}
              </span>
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button-group style="width: 100%">
            <el-button
              style="width: 50%"
              @click="loadFrequentParams"
              :loading="loadingHistory"
            >
              🔥 常用参数
            </el-button>
            <el-button
              style="width: 50%"
              @click="loadLastParams"
              :loading="loadingHistory"
            >
              ⏮️ 最近参数
            </el-button>
          </el-button-group>
        </el-col>
      </el-row>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        v-loading="loading"
      >
        <!-- 请假流程表单 -->
        <template v-if="processKey === 'leave-approval-process'">
          <el-form-item label="审批人" prop="manager">
            <el-input
              v-model="formData.manager"
              placeholder="请输入审批人用户名（如：manager）"
            />
          </el-form-item>
          <el-form-item label="请假类型" prop="leaveType">
            <el-select v-model="formData.leaveType" placeholder="请选择请假类型">
              <el-option label="年假" value="annual" />
              <el-option label="事假" value="personal" />
              <el-option label="病假" value="sick" />
            </el-select>
          </el-form-item>
          <el-form-item label="请假天数" prop="days">
            <el-input-number
              v-model="formData.days"
              :min="1"
              :max="30"
              placeholder="请输入请假天数"
            />
          </el-form-item>
          <el-form-item label="开始日期" prop="startDate">
            <el-date-picker
              v-model="formData.startDate"
              type="date"
              placeholder="选择开始日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="结束日期" prop="endDate">
            <el-date-picker
              v-model="formData.endDate"
              type="date"
              placeholder="选择结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="请假原因" prop="reason">
            <el-input
              v-model="formData.reason"
              type="textarea"
              :rows="4"
              placeholder="请输入请假原因"
            />
          </el-form-item>
        </template>

        <!-- 默认表单 -->
        <template v-else>
          <el-alert
            title="该流程暂无特定表单"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
          />
        </template>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            提交
          </el-button>
          <el-button @click="saveAsDraft" :loading="savingDraft">
            保存草稿
          </el-button>
          <el-button @click="saveAsTemplate">
            另存为模板
          </el-button>
          <el-button @click="goBack">
            返回
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { startProcess } from '@/api/workflow'
import { getPublicTemplates, useTemplate as useTemplateAPI, createTemplate } from '@/api/template'
import { getDraftsByProcessKey, saveDraft } from '@/api/template'
import { getFrequentParams, getLastParams } from '@/api/history'
import { useUserStore } from '@/stores/user'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const processKey = ref(route.params.key)
const loading = ref(false)
const submitting = ref(false)
const savingDraft = ref(false)
const loadingHistory = ref(false)
const formRef = ref(null)

// 模板和草稿
const templates = ref([])
const drafts = ref([])
const selectedTemplate = ref(null)
const selectedDraft = ref(null)
const currentDraftId = ref(null)

const formData = reactive({
  manager: 'manager',
  leaveType: 'annual',
  days: 1,
  startDate: '',
  endDate: '',
  reason: ''
})

// 表单验证规则
const rules = {
  manager: [
    { required: true, message: '请输入审批人', trigger: 'blur' }
  ],
  leaveType: [
    { required: true, message: '请选择请假类型', trigger: 'change' }
  ],
  days: [
    { required: true, message: '请输入请假天数', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入请假原因', trigger: 'blur' },
    { min: 5, message: '请假原因至少5个字符', trigger: 'blur' }
  ]
}

// 加载模板和草稿
const loadTemplatesAndDrafts = async () => {
  try {
    // 加载公开模板
    const templateRes = await getPublicTemplates(processKey.value)
    if (templateRes.code === 200) {
      templates.value = templateRes.data || []
    }
    
    // 加载我的草稿
    const userId = userStore.userInfo?.id
    if (userId) {
      const draftRes = await getDraftsByProcessKey(processKey.value, userId)
      if (draftRes.code === 200) {
        drafts.value = draftRes.data || []
      }
    }
  } catch (error) {
    console.error('加载模板和草稿失败:', error)
  }
}

// 从模板加载数据
const loadTemplate = async (templateId) => {
  if (!templateId) return
  
  const template = templates.value.find(t => t.id === templateId)
  if (template && template.templateData) {
    try {
      const data = JSON.parse(template.templateData)
      Object.assign(formData, data)
      ElMessage.success('模板已加载')
      
      // 增加使用次数
      await useTemplateAPI(templateId)
    } catch (error) {
      ElMessage.error('加载模板失败: ' + error.message)
    }
  }
}

// 从草稿加载数据
const loadDraft = (draftId) => {
  if (!draftId) return
  
  const draft = drafts.value.find(d => d.id === draftId)
  if (draft && draft.draftData) {
    try {
      const data = JSON.parse(draft.draftData)
      Object.assign(formData, data)
      currentDraftId.value = draftId
      ElMessage.success('草稿已加载')
    } catch (error) {
      ElMessage.error('加载草稿失败: ' + error.message)
    }
  }
}

// 保存草稿
const saveAsDraft = async () => {
  savingDraft.value = true
  try {
    const userId = userStore.userInfo?.id
    const username = userStore.userInfo?.username
    
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }
    
    const draftData = {
      id: currentDraftId.value,
      draftName: `${formData.leaveType === 'annual' ? '年假' : formData.leaveType === 'sick' ? '病假' : '事假'}草稿`,
      processDefinitionKey: processKey.value,
      draftData: JSON.stringify(formData),
      creatorId: userId,
      creatorName: username
    }
    
    const res = await saveDraft(draftData)
    if (res.code === 200) {
      currentDraftId.value = res.data.id
      ElMessage.success('草稿保存成功')
      // 重新加载草稿列表
      await loadTemplatesAndDrafts()
    }
  } catch (error) {
    ElMessage.error('保存草稿失败: ' + error.message)
  } finally {
    savingDraft.value = false
  }
}

// 另存为模板
const saveAsTemplate = async () => {
  try {
    const { value: templateName } = await ElMessageBox.prompt('请输入模板名称', '另存为模板', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '模板名称不能为空'
    })
    
    if (!templateName) return
    
    const userId = userStore.userInfo?.id
    const username = userStore.userInfo?.username
    
    const templateData = {
      templateName,
      processDefinitionKey: processKey.value,
      templateData: JSON.stringify(formData),
      description: `${username}创建的模板`,
      isPublic: 0, // 默认私有
      creatorId: userId,
      creatorName: username
    }
    
    const res = await createTemplate(templateData)
    if (res.code === 200) {
      ElMessage.success('模板创建成功')
      // 重新加载模板列表
      await loadTemplatesAndDrafts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('创建模板失败: ' + error.message)
    }
  }
}

// 加载常用参数（基于历史统计）
const loadFrequentParams = async () => {
  loadingHistory.value = true
  try {
    const username = userStore.userInfo?.username
    if (!username) {
      ElMessage.warning('请先登录')
      return
    }
    
    const res = await getFrequentParams(username, processKey.value)
    if (res.code === 200 && res.data) {
      const params = res.data
      if (Object.keys(params).length === 0) {
        ElMessage.info('您还没有历史流程记录')
        return
      }
      
      // 回填到表单
      Object.assign(formData, params)
      ElMessage.success(`已加载您最常用的参数（基于${Object.keys(params).length}个字段的历史统计）`)
    }
  } catch (error) {
    ElMessage.error('加载常用参数失败: ' + error.message)
  } finally {
    loadingHistory.value = false
  }
}

// 加载最近参数
const loadLastParams = async () => {
  loadingHistory.value = true
  try {
    const username = userStore.userInfo?.username
    if (!username) {
      ElMessage.warning('请先登录')
      return
    }
    
    const res = await getLastParams(username, processKey.value)
    if (res.code === 200 && res.data) {
      const params = res.data
      if (Object.keys(params).length === 0) {
        ElMessage.info('您还没有历史流程记录')
        return
      }
      
      // 回填到表单
      Object.assign(formData, params)
      ElMessage.success('已加载您最近一次的参数')
    }
  } catch (error) {
    ElMessage.error('加载最近参数失败: ' + error.message)
  } finally {
    loadingHistory.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const variables = {
          applicant: userStore.userInfo?.username || 'unknown',
          ...formData
        }
        
        const res = await startProcess(processKey.value, variables)
        if (res.code === 200) {
          ElMessage.success('流程启动成功')
          
          // 清除草稿（如果是从草稿发起的）
          if (currentDraftId.value) {
            // 可以选择删除草稿或保留
          }
          
          router.push('/workflow/instances')
        } else {
          ElMessage.error(res.message || '流程启动失败')
        }
      } catch (error) {
        ElMessage.error('流程启动失败: ' + error.message)
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
  // 加载模板和草稿
  loadTemplatesAndDrafts()
})
</script>

<style scoped>
.workflow-start {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>

