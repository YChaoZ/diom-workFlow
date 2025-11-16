/**
 * DIOM Workflow 前端运行时配置
 * 
 * 此文件在打包后可直接修改，无需重新编译前端
 * 适用于需要快速切换Gateway地址的场景
 * 
 * 使用方式:
 * 1. 将此文件复制到 diom-frontend/public/config.js
 * 2. 在 index.html 中引入: <script src="/config.js"></script>
 * 3. 在 request.js 中使用: window.APP_CONFIG.GATEWAY_URL
 * 4. 部署后可直接修改此文件，无需重新打包
 */

window.APP_CONFIG = {
  // Gateway地址配置
  // 方案1: 留空，使用相对路径（Nginx反向代理）- 推荐
  GATEWAY_URL: '',
  
  // 方案2: 指定完整地址（需要Gateway配置CORS）
  // GATEWAY_URL: 'http://gateway.company.com:8080',
  
  // 方案3: 使用环境变量（Docker部署）
  // GATEWAY_URL: window.location.origin,
  
  // 应用配置
  APP_TITLE: 'DIOM工作流管理系统',
  APP_VERSION: '1.0.0',
  
  // 功能开关
  ENABLE_DEBUG: false,
  ENABLE_MOCK: false,
  
  // 其他配置
  TIMEOUT: 10000,  // API超时时间（毫秒）
  TOKEN_EXPIRE: 3600,  // Token过期时间（秒）
}

