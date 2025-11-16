# DIOM工作流系统 - 前端项目

基于 Vue 3 + Vite + Element Plus 的现代化前端应用

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - 基于 Vue 3 的组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - 新一代状态管理
- **Axios** - HTTP 客户端
- **js-cookie** - Cookie 管理

## 功能特性

### 1. 用户认证
- ✅ 登录/注册
- ✅ JWT Token 管理
- ✅ 路由守卫
- ✅ 自动登录态维护

### 2. 工作流管理
- ✅ 流程定义列表
- ✅ 启动流程
- ✅ 我的任务
- ✅ 任务处理
- ✅ 流程实例
- ✅ 流程历史查询

### 3. 用户管理
- ✅ 个人信息
- ✅ 用户列表
- ✅ 信息修改

### 4. UI/UX
- ✅ 响应式布局
- ✅ 侧边栏导航
- ✅ 页面切换动画
- ✅ Loading 状态
- ✅ 错误提示

## 项目结构

```
diom-frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API 接口
│   │   ├── auth.js     # 认证相关
│   │   ├── workflow.js # 工作流相关
│   │   └── user.js     # 用户相关
│   ├── assets/         # 静态资源
│   ├── components/     # 公共组件
│   │   ├── Layout/     # 布局组件
│   │   └── Common/     # 通用组件
│   ├── router/         # 路由配置
│   ├── stores/         # Pinia 状态管理
│   ├── utils/          # 工具函数
│   │   ├── request.js  # Axios 封装
│   │   └── auth.js     # Token 管理
│   ├── views/          # 页面组件
│   │   ├── Login/      # 登录页
│   │   ├── Home/       # 首页
│   │   ├── Workflow/   # 工作流模块
│   │   └── User/       # 用户模块
│   ├── App.vue
│   └── main.js
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问：http://localhost:3000

### 生产构建

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## API 配置

后端 API 地址配置在 `src/utils/request.js`:

```javascript
const service = axios.create({
  baseURL: 'http://localhost:8080', // Gateway 地址
  timeout: 10000
})
```

## 路由说明

| 路径 | 说明 | 权限 |
|------|------|------|
| `/login` | 登录/注册页 | 公开 |
| `/` | 主页 | 需登录 |
| `/home` | 首页 | 需登录 |
| `/workflow/list` | 流程定义列表 | 需登录 |
| `/workflow/start/:key` | 发起流程 | 需登录 |
| `/workflow/tasks` | 我的任务 | 需登录 |
| `/workflow/task/:id` | 任务详情 | 需登录 |
| `/workflow/instances` | 流程实例 | 需登录 |
| `/user/profile` | 个人信息 | 需登录 |
| `/user/list` | 用户列表 | 需登录 |

## 默认账号

测试账号：

- 用户名：`admin`
- 密码：`123456`

或

- 用户名：`user_1`
- 密码：`123456`

## 开发说明

### 添加新页面

1. 在 `src/views/` 下创建页面组件
2. 在 `src/router/index.js` 中添加路由
3. 如需要 API，在 `src/api/` 中添加接口

### 状态管理

使用 Pinia 进行状态管理，示例：

```javascript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userInfo = userStore.userInfo
```

### API 调用

```javascript
import { login } from '@/api/auth'

const res = await login({ username, password })
if (res.code === 200) {
  // 成功
}
```

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## License

MIT
