<template>
  <div class="flowable-modeler-container">
    <!-- 顶部工具栏（可选）-->
    <div class="modeler-toolbar" v-if="showToolbar">
      <div class="toolbar-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/workflow/design' }">流程管理</el-breadcrumb-item>
          <el-breadcrumb-item>流程设计器</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="toolbar-right">
        <el-button @click="refreshModeler" icon="Refresh">刷新</el-button>
        <el-button @click="openInNewTab" icon="FullScreen">全屏打开</el-button>
        <el-button @click="goBack" icon="Back">返回</el-button>
      </div>
    </div>
    
    <!-- Flowable Modeler iframe -->
    <div class="modeler-iframe-wrapper">
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>正在加载流程设计器...</p>
      </div>
      
      <iframe
        ref="modelerIframe"
        :src="modelerUrl"
        class="modeler-iframe"
        frameborder="0"
        @load="onModelerLoad"
        v-show="!loading"
      ></iframe>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

// 状态
const modelerIframe = ref(null);
const loading = ref(true);
const showToolbar = ref(true);

// 获取 Token（从 localStorage 或 sessionStorage）
const getToken = () => {
  return localStorage.getItem('token') || sessionStorage.getItem('token') || '';
};

// 计算 Modeler URL
const modelerUrl = computed(() => {
  // 获取基础 URL（通过环境变量配置）
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
  
  // 获取流程模型 ID（如果有）
  const modelId = route.query.modelId;
  const processKey = route.query.processKey;
  
  // 构建 URL
  let url = '';
  
  if (modelId) {
    // 编辑模式：打开指定的流程模型
    url = `${baseUrl}/flowable-modeler/index.html#/editor/${modelId}`;
  } else if (processKey) {
    // 从流程定义创建新模型
    url = `${baseUrl}/flowable-modeler/index.html#/processes`;
  } else {
    // 默认：打开流程列表
    url = `${baseUrl}/flowable-modeler/index.html#/processes`;
  }
  
  return url;
});

/**
 * Modeler 加载完成
 */
const onModelerLoad = () => {
  loading.value = false;
  
  // 可以通过 postMessage 与 iframe 通信
  const token = getToken();
  if (modelerIframe.value && modelerIframe.value.contentWindow) {
    try {
      modelerIframe.value.contentWindow.postMessage({
        type: 'AUTH_TOKEN',
        token: token
      }, '*');
      
      console.log('Flowable Modeler loaded successfully');
    } catch (error) {
      console.error('Failed to send token to Modeler:', error);
    }
  }
};

/**
 * 刷新 Modeler
 */
const refreshModeler = () => {
  if (modelerIframe.value && modelerIframe.value.contentWindow) {
    loading.value = true;
    modelerIframe.value.contentWindow.location.reload();
  }
};

/**
 * 在新标签页打开
 */
const openInNewTab = () => {
  window.open(modelerUrl.value, '_blank');
};

/**
 * 返回
 */
const goBack = () => {
  router.back();
};

/**
 * 监听来自 iframe 的消息
 */
const handleMessage = (event) => {
  // 验证消息来源（可选，增强安全性）
  // if (event.origin !== window.location.origin) {
  //   return;
  // }
  
  const data = event.data;
  
  if (data && data.type) {
    switch (data.type) {
      case 'MODELER_READY':
        console.log('Modeler is ready');
        ElMessage.success('流程设计器已就绪');
        break;
        
      case 'MODEL_SAVED':
        console.log('Model saved:', data.modelId);
        ElMessage.success('流程模型已保存');
        break;
        
      case 'MODEL_DEPLOYED':
        console.log('Model deployed:', data.deploymentId);
        ElMessage.success('流程已部署成功');
        break;
        
      case 'ERROR':
        console.error('Modeler error:', data.message);
        ElMessage.error(data.message || '操作失败');
        break;
        
      default:
        console.log('Unknown message from Modeler:', data);
    }
  }
};

// 生命周期钩子
onMounted(() => {
  // 添加消息监听器
  window.addEventListener('message', handleMessage);
  
  // 检查是否隐藏工具栏
  showToolbar.value = route.query.hideToolbar !== 'true';
});

onBeforeUnmount(() => {
  // 移除消息监听器
  window.removeEventListener('message', handleMessage);
});
</script>

<style scoped>
.flowable-modeler-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.modeler-toolbar {
  height: 60px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.toolbar-left {
  flex: 1;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.modeler-iframe-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
  background: #fff;
}

.loading-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 100;
}

.loading-container p {
  margin-top: 20px;
  color: #606266;
  font-size: 14px;
}

.modeler-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

/* Element Plus 图标动画 */
.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modeler-toolbar {
    flex-direction: column;
    height: auto;
    padding: 10px;
  }
  
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: center;
    margin: 5px 0;
  }
}
</style>

