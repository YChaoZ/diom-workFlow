<template>
  <div class="user-profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      
      <div v-loading="loading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ userInfo.nickname }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userInfo.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="userInfo.status === 0" type="success">正常</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ userInfo.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 30px;">
          <el-button type="primary" @click="editDialogVisible = true">
            修改信息
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 修改信息对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改个人信息"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="editForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUserInfo" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUser } from '@/api/user'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const editDialogVisible = ref(false)
const formRef = ref(null)

const userInfo = computed(() => userStore.userInfo || {})

const editForm = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 打开编辑对话框时，填充当前数据
const openEditDialog = () => {
  editForm.nickname = userInfo.value.nickname || ''
  editForm.email = userInfo.value.email || ''
  editForm.phone = userInfo.value.phone || ''
  editDialogVisible.value = true
}

// 保存用户信息
const saveUserInfo = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const res = await updateUser({
          id: userInfo.value.id,
          ...editForm
        })
        
        if (res.code === 200) {
          ElMessage.success('信息修改成功')
          editDialogVisible.value = false
          // 重新获取用户信息
          await userStore.getUserInfoAction()
        } else {
          ElMessage.error(res.message || '信息修改失败')
        }
      } catch (error) {
        ElMessage.error('信息修改失败: ' + error.message)
      } finally {
        saving.value = false
      }
    }
  })
}

onMounted(() => {
  if (!userInfo.value || !userInfo.value.id) {
    userStore.getUserInfoAction()
  }
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>

