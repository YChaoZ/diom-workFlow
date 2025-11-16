<template>
  <div class="templates-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程模板管理</span>
          <el-button type="primary" :icon="Plus" @click="handleCreate">
            创建模板
          </el-button>
        </div>
      </template>

      <!-- 标签页切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="公开模板" name="public">
          <el-table
            :data="publicTemplates"
            v-loading="loading"
            stripe
            highlight-current-row
            empty-text="暂无公开模板"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="templateName" label="模板名称" min-width="150" />
            <el-table-column prop="processDefinitionKey" label="流程类型" width="200">
              <template #default="{ row }">
                <el-tag v-if="row.processDefinitionKey === 'leave-approval-process'">
                  请假审批
                </el-tag>
                <el-tag v-else type="info">
                  {{ row.processDefinitionKey }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="useCount" label="使用次数" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="success">{{ row.useCount }}次</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creatorName" label="创建人" width="120" />
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  :icon="View"
                  @click="handleView(row)"
                >
                  查看
                </el-button>
                <el-button
                  v-if="row.creatorId === userStore.userInfo?.id"
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的模板" name="my">
          <el-table
            :data="myTemplates"
            v-loading="loading"
            stripe
            highlight-current-row
            empty-text="您还没有创建模板"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="templateName" label="模板名称" min-width="150" />
            <el-table-column prop="processDefinitionKey" label="流程类型" width="200">
              <template #default="{ row }">
                <el-tag v-if="row.processDefinitionKey === 'leave-approval-process'">
                  请假审批
                </el-tag>
                <el-tag v-else type="info">
                  {{ row.processDefinitionKey }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isPublic" label="可见性" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isPublic === 1" type="success">公开</el-tag>
                <el-tag v-else type="info">私有</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="useCount" label="使用次数" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="success">{{ row.useCount }}次</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  :icon="View"
                  @click="handleView(row)"
                >
                  查看
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  :icon="Edit"
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 模板详情/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="模板名称" prop="templateName">
          <el-input
            v-model="formData.templateName"
            placeholder="请输入模板名称"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>

        <el-form-item label="流程类型" prop="processDefinitionKey">
          <el-select
            v-model="formData.processDefinitionKey"
            placeholder="请选择流程类型"
            :disabled="dialogMode === 'view'"
            style="width: 100%"
          >
            <el-option
              label="请假审批流程"
              value="leave-approval-process"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>

        <el-form-item label="模板数据" prop="templateData">
          <el-input
            v-model="formData.templateData"
            type="textarea"
            :rows="6"
            placeholder='请输入JSON格式数据，例如：{"leaveType":"annual","manager":"manager","days":5}'
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>

        <el-form-item label="可见性" prop="isPublic">
          <el-radio-group v-model="formData.isPublic" :disabled="dialogMode === 'view'">
            <el-radio :label="0">私有（仅自己可见）</el-radio>
            <el-radio :label="1">公开（所有人可见）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="dialogMode === 'view'" label="使用次数">
          <el-tag type="success">{{ formData.useCount }}次</el-tag>
        </el-form-item>

        <el-form-item v-if="dialogMode === 'view'" label="创建人">
          <span>{{ formData.creatorName }}</span>
        </el-form-item>

        <el-form-item v-if="dialogMode === 'view'" label="创建时间">
          <span>{{ formatTime(formData.createTime) }}</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          {{ dialogMode === 'view' ? '关闭' : '取消' }}
        </el-button>
        <el-button
          v-if="dialogMode !== 'view'"
          type="primary"
          @click="handleSubmit"
          :loading="submitting"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Edit, Delete } from '@element-plus/icons-vue'
import {
  getPublicTemplates,
  getMyTemplates,
  createTemplate,
  updateTemplate,
  deleteTemplate
} from '@/api/template'
import { useUserStore } from '@/stores/user'
import { formatTime } from '@/utils/format'

const userStore = useUserStore()

const activeTab = ref('public')
const loading = ref(false)
const publicTemplates = ref([])
const myTemplates = ref([])

const dialogVisible = ref(false)
const dialogMode = ref('create') // 'create' | 'edit' | 'view'
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  templateName: '',
  processDefinitionKey: 'leave-approval-process',
  templateData: '',
  description: '',
  isPublic: 0,
  useCount: 0,
  creatorName: '',
  createTime: null
})

const formRules = {
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  processDefinitionKey: [
    { required: true, message: '请选择流程类型', trigger: 'change' }
  ],
  templateData: [
    { required: true, message: '请输入模板数据', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        try {
          JSON.parse(value)
          callback()
        } catch (e) {
          callback(new Error('模板数据必须是有效的JSON格式'))
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载公开模板
const loadPublicTemplates = async () => {
  loading.value = true
  try {
    const res = await getPublicTemplates()
    if (res.code === 200) {
      publicTemplates.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载公开模板失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载我的模板
const loadMyTemplates = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }
    const res = await getMyTemplates(userId)
    if (res.code === 200) {
      myTemplates.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载我的模板失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = (tab) => {
  if (tab === 'public') {
    loadPublicTemplates()
  } else {
    loadMyTemplates()
  }
}

// 创建模板
const handleCreate = () => {
  dialogMode.value = 'create'
  dialogTitle.value = '创建模板'
  resetForm()
  dialogVisible.value = true
}

// 查看模板
const handleView = (row) => {
  dialogMode.value = 'view'
  dialogTitle.value = '模板详情'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 编辑模板
const handleEdit = (row) => {
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑模板'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除模板
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${row.templateName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteTemplate(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 重新加载列表
      if (activeTab.value === 'public') {
        loadPublicTemplates()
      } else {
        loadMyTemplates()
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const userId = userStore.userInfo?.id
        const username = userStore.userInfo?.username

        if (!userId) {
          ElMessage.warning('请先登录')
          return
        }

        const data = {
          ...formData,
          creatorId: userId,
          creatorName: username
        }

        let res
        if (dialogMode.value === 'create') {
          res = await createTemplate(data)
        } else {
          res = await updateTemplate(formData.id, data)
        }

        if (res.code === 200) {
          ElMessage.success(dialogMode.value === 'create' ? '创建成功' : '更新成功')
          dialogVisible.value = false
          // 重新加载列表
          if (activeTab.value === 'public') {
            loadPublicTemplates()
          } else {
            loadMyTemplates()
          }
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败: ' + error.message)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    templateName: '',
    processDefinitionKey: 'leave-approval-process',
    templateData: '',
    description: '',
    isPublic: 0,
    useCount: 0,
    creatorName: '',
    createTime: null
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

onMounted(() => {
  loadPublicTemplates()
})
</script>

<style scoped>
.templates-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

