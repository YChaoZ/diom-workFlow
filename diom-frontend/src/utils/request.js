import axios from 'axios'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'

// 创建axios实例
const service = axios.create({
  baseURL: 'http://localhost:8080', // Gateway地址
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从Cookie获取token
    const token = Cookies.get('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是200，说明接口有异常
    if (res.code !== 200) {
      // ⚠️ 特殊处理：BPMN验证接口即使失败也不显示全局错误提示
      // 因为验证失败是正常业务场景，需要在业务代码中处理详细错误
      if (response.config.url && response.config.url.includes('/process-design/validate')) {
        // 不显示全局错误提示，直接reject并保留完整数据
        const error = new Error(res.message || 'Error')
        error.response = { data: res }  // 保留原始响应数据
        return Promise.reject(error)
      }
      
      // 其他接口的错误处理
      ElMessage.error(res.message || '请求失败')
      
      // 401: Token过期或未登录
      if (res.code === 401) {
        ElMessage.warning('登录已过期，请重新登录')
        // 清除token
        Cookies.remove('token')
        // 跳转到登录页
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
      }
      
      const error = new Error(res.message || 'Error')
      error.response = { data: res }  // 保留原始响应数据
      return Promise.reject(error)
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '网络错误'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          Cookies.remove('token')
          setTimeout(() => {
            window.location.href = '/login'
          }, 1000)
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `连接错误 ${error.response.status}`
      }
    } else if (error.message) {
      message = error.message
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service

