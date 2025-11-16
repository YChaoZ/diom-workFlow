import request from '@/utils/request'

/**
 * 获取用户常用参数（基于历史流程统计）
 */
export function getFrequentParams(username, processKey) {
  return request({
    url: '/workflow/history/frequent-params',
    method: 'get',
    params: { username, processKey }
  })
}

/**
 * 获取用户最近一次的参数
 */
export function getLastParams(username, processKey) {
  return request({
    url: '/workflow/history/last-params',
    method: 'get',
    params: { username, processKey }
  })
}

