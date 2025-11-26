import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'
import permission from './directives/permission'
import installIcon from '@/flyflow/utils/icon'

// 引入 FlyFlow 样式（工作流组件样式）
import '@/flyflow/css/workflow.css'
import '@/flyflow/css/dialog.css'
// import '@/flyflow/assets/base.css'  // ✅ 不需要，避免样式冲突
// import '@/flyflow/assets/main.css'  // ✅ 不需要，会限制页面宽度

// 引入 LogicFlow 样式（流程设计器引擎）
import '@logicflow/core/dist/style/index.css'
import '@logicflow/extension/lib/style/index.css'

const app = createApp(App)
const pinia = createPinia()

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})
app.use(installIcon) // 注册全局图标

// 注册权限指令
app.directive('permission', permission)

app.mount('#app')
