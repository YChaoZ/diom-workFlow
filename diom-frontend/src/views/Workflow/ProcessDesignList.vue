<template>
  <div class="process-design-list">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索流程名称或Key"
          style="width: 300px; margin-right: 10px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="searchStatus"
          placeholder="状态"
          style="width: 150px; margin-right: 10px"
          clearable
          @change="handleSearch"
        >
          <el-option label="全部" value="ALL" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已废弃" value="DEPRECATED" />
        </el-select>
        
        <el-select
          v-model="searchCategory"
          placeholder="分类"
          style="width: 150px; margin-right: 10px"
          clearable
          @change="handleSearch"
        >
          <el-option label="人事" value="人事" />
          <el-option label="财务" value="财务" />
          <el-option label="行政" value="行政" />
          <el-option label="采购" value="采购" />
          <el-option label="其他" value="其他" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
      </div>
      
      <div class="toolbar-right">
        <el-button type="primary" @click="createProcess">
          <el-icon><Plus /></el-icon> 新建流程
        </el-button>
      </div>
    </div>
    
    <!-- 列表 -->
    <el-table 
      :data="tableData" 
      v-loading="loading"
      border
      style="width: 100%"
      @row-dblclick="handleEdit"
    >
      <el-table-column prop="processName" label="流程名称" min-width="200">
        <template #default="{ row }">
          <div>
            <strong>{{ row.processName }}</strong>
            <el-tag 
              v-if="row.hasNewerVersion" 
              type="warning" 
              size="small" 
              style="margin-left: 10px"
            >
              有新版本
            </el-tag>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="processKey" label="流程Key" width="200" />
      
      <el-table-column prop="version" label="版本" width="80" align="center">
        <template #default="{ row }">
          <el-tag type="info" size="small">v{{ row.version }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="category" label="分类" width="100" align="center" />
      
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      
      <el-table-column prop="creatorName" label="创建人" width="100" />
      
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button 
            link 
            type="primary" 
            size="small" 
            @click="handleView(row)"
          >
            查看
          </el-button>
          
          <el-button 
            link 
            type="primary" 
            size="small" 
            @click="handleEdit(row)"
            v-if="row.status === 'DRAFT'"
          >
            编辑
          </el-button>
          
          <el-button 
            link 
            type="primary" 
            size="small" 
            @click="handleNewVersion(row)"
            v-if="row.status === 'PUBLISHED'"
          >
            新版本
          </el-button>
          
          <el-button 
            link 
            type="danger" 
            size="small" 
            @click="handleDelete(row)"
            v-if="row.status === 'DRAFT'"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { 
  getProcessDesignList, 
  deleteProcessDesign,
  createNewVersion 
} from '@/api/processDesign'

const router = useRouter()

// 数据
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchKeyword = ref('')
const searchStatus = ref('ALL')
const searchCategory = ref('')

// 生命周期
onMounted(() => {
  loadList()
})

// 加载列表
const loadList = async () => {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value,
      status: searchStatus.value,
      category: searchCategory.value
    }
    
    const response = await getProcessDesignList(params)
    
    if (response.code === 200) {
      tableData.value = response.data.list
      total.value = response.data.total
    }
  } catch (err) {
    console.error('加载列表失败', err)
    ElMessage.error('加载列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadList()
}

// 创建流程
const createProcess = () => {
  router.push('/workflow/design/new')
}

// 查看
const handleView = (row) => {
  router.push(`/workflow/design/view/${row.id}`)
}

// 编辑
const handleEdit = (row) => {
  router.push(`/workflow/design/edit/${row.id}`)
}

// 新版本
const handleNewVersion = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入变更说明', '创建新版本', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '变更说明不能为空'
    })
    
    const response = await createNewVersion(row.id, {
      changeDescription: value
    })
    
    if (response.code === 200) {
      ElMessage.success('新版本创建成功')
      // 跳转到编辑页面
      router.push(`/workflow/design/edit/${response.data.id}`)
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('创建新版本失败', err)
      ElMessage.error('创建新版本失败')
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除流程"${row.processName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await deleteProcessDesign(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadList()
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败', err)
      ElMessage.error('删除失败')
    }
  }
}

// 分页
const handleSizeChange = () => {
  loadList()
}

const handleCurrentChange = () => {
  loadList()
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'DRAFT': 'info',
    'PUBLISHED': 'success',
    'DEPRECATED': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status) => {
  const labels = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'DEPRECATED': '已废弃'
  }
  return labels[status] || status
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.process-design-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

