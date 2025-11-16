/**
 * 格式化工具函数
 */

/**
 * 格式化时间为中文格式
 * @param {string|Date} time - 时间字符串或Date对象
 * @param {boolean} withSeconds - 是否显示秒，默认true
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time, withSeconds = true) {
  if (!time) return '-'
  
  try {
    const date = typeof time === 'string' ? new Date(time) : time
    
    // 检查是否是有效日期
    if (isNaN(date.getTime())) {
      return '-'
    }
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    if (withSeconds) {
      return `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`
    } else {
      return `${year}/${month}/${day} ${hours}:${minutes}`
    }
  } catch (error) {
    console.error('时间格式化错误:', error)
    return '-'
  }
}

/**
 * 格式化日期为中文格式（不含时间）
 * @param {string|Date} date - 日期字符串或Date对象
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date) {
  if (!date) return '-'
  
  try {
    const d = typeof date === 'string' ? new Date(date) : date
    
    if (isNaN(d.getTime())) {
      return '-'
    }
    
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    
    return `${year}/${month}/${day}`
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '-'
  }
}

/**
 * 格式化相对时间（如：3分钟前、2小时前）
 * @param {string|Date} time - 时间字符串或Date对象
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(time) {
  if (!time) return '-'
  
  try {
    const date = typeof time === 'string' ? new Date(time) : time
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    
    if (days > 7) {
      return formatTime(time, false)
    } else if (days > 0) {
      return `${days}天前`
    } else if (hours > 0) {
      return `${hours}小时前`
    } else if (minutes > 0) {
      return `${minutes}分钟前`
    } else {
      return '刚刚'
    }
  } catch (error) {
    console.error('相对时间格式化错误:', error)
    return '-'
  }
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的大小
 */
export function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 格式化数字（千分位）
 * @param {number} num - 数字
 * @returns {string} 格式化后的数字
 */
export function formatNumber(num) {
  if (!num && num !== 0) return '-'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

