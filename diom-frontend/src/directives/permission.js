import { useUserStore } from '@/stores/user'

/**
 * 权限指令
 * 用法: v-permission="['WORKFLOW_START']"
 * 如果用户没有指定权限，元素将被移除
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions || []
    
    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some(perm => value.includes(perm))
      
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error('需要指定权限码数组！如 v-permission="[\'WORKFLOW_START\']"')
    }
  }
}

