<template>
  <div class="role-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建角色
      </el-button>
    </div>
    
    <el-table :data="roleList" style="width: 100%" v-loading="loading">
      <el-table-column prop="roleCode" label="角色编码" width="150" />
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleAssignPermission(row)">分配权限</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadRoles"
      @current-change="loadRoles"
      style="margin-top: 20px; justify-content: flex-end;"
    />
    
    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色编码">
          <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="请输入角色编码，如：DEVELOPER" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 权限分配对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="600px">
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="{ children: 'children', label: 'permissionName' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="selectedPermissions"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRoleList, getRoleById, createRole, updateRole, deleteRole } from '@/api/role'
import { getPermissionTree } from '@/api/permission'

// 列表数据
const roleList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitting = ref(false)

// 表单
const form = ref({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
  status: 1,
  sortOrder: 0
})

// 权限分配
const permissionDialogVisible = ref(false)
const permissionTree = ref([])
const selectedPermissions = ref([])
const currentRoleId = ref(null)
const permissionTreeRef = ref(null)

// 加载角色列表
const loadRoles = async () => {
  loading.value = true
  try {
    const res = await getRoleList({
      page: currentPage.value,
      size: pageSize.value
    })
    if (res.code === 200) {
      roleList.value = res.data || []
      total.value = res.total || 0
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// 创建角色
const handleCreate = () => {
  isEdit.value = false
  dialogTitle.value = '创建角色'
  form.value = {
    id: null,
    roleCode: '',
    roleName: '',
    description: '',
    status: 1,
    sortOrder: 0
  }
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  try {
    const res = await getRoleById(row.id)
    if (res.code === 200) {
      form.value = { ...res.data }
    }
  } catch (error) {
    ElMessage.error('获取角色详情失败')
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!form.value.roleCode || !form.value.roleName) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRole(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createRole(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRoles()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除角色
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      loadRoles()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 分配权限
const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  permissionDialogVisible.value = true
  
  // 加载权限树
  try {
    const res = await getPermissionTree()
    if (res.code === 200) {
      permissionTree.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载权限树失败')
  }
  
  // 获取角色已有权限
  try {
    const res = await getRoleById(row.id)
    if (res.code === 200) {
      selectedPermissions.value = res.data.permissionIds || []
    }
  } catch (error) {
    ElMessage.error('获取角色权限失败')
  }
}

// 保存权限
const handleSavePermissions = async () => {
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
  const allKeys = [...checkedKeys, ...halfCheckedKeys]
  
  submitting.value = true
  try {
    await updateRole(currentRoleId.value, {
      permissionIds: allKeys
    })
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    ElMessage.error('权限分配失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.role-container {
  padding: 20px;
  background: white;
  border-radius: 4px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>

