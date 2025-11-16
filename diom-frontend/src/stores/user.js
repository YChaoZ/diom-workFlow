import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo, logout } from '@/api/auth'
import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref('')
  const roles = ref([])
  const permissions = ref([])

  // 登录
  const loginAction = async (username, password) => {
    try {
      const res = await login({ username, password })
      if (res.code === 200) {
        token.value = res.data.token
        setToken(res.data.token)
        // 登录成功后获取用户信息
        await getUserInfoAction()
        return { success: true }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  // 获取用户信息
  const getUserInfoAction = async () => {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
        // 存储角色和权限
        roles.value = res.data.roles || []
        permissions.value = res.data.permissions || []
        return { success: true }
      } else {
        return { success: false }
      }
    } catch (error) {
      return { success: false }
    }
  }

  // 退出登录
  const logoutAction = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      roles.value = []
      permissions.value = []
      removeToken()
    }
  }

  // 重置状态
  const resetState = () => {
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    removeToken()
  }

  return {
    userInfo,
    token,
    roles,
    permissions,
    loginAction,
    getUserInfoAction,
    logoutAction,
    resetState
  }
})

