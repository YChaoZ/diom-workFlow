<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" @click="loadUsers" :icon="Refresh">
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="success">正常</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>

      <div class="pagination" v-if="userList.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getUserList } from '@/api/user'

const loading = ref(false)
const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (res.code === 200) {
      userList.value = res.data || []
      total.value = userList.value.length
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 分页改变
const handlePageChange = (page) => {
  currentPage.value = page
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-list {
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

