import request from '@/utils/request'

/**
 * 流程设计API服务
 */

/**
 * 查询流程设计列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getProcessDesignList(params) {
  return request({
    url: '/workflow/api/process-design/list',
    method: 'get',
    params
  })
}

/**
 * 查询流程设计详情
 * @param {Number} id - 流程设计ID
 * @returns {Promise}
 */
export function getProcessDesignById(id) {
  return request({
    url: `/workflow/api/process-design/${id}`,
    method: 'get'
  })
}

/**
 * 保存草稿
 * @param {Object} data - 流程设计数据
 * @returns {Promise}
 */
export function saveProcessDesign(data) {
  return request({
    url: '/workflow/api/process-design/save',
    method: 'post',
    data
  })
}

/**
 * 验证BPMN
 * @param {Object} data - BPMN XML数据
 * @returns {Promise}
 */
export function validateBpmn(data) {
  return request({
    url: '/workflow/api/process-design/validate',
    method: 'post',
    data
  })
}

/**
 * 发布流程
 * @param {Object} data - 发布数据
 * @returns {Promise}
 */
export function publishProcess(data) {
  return request({
    url: '/workflow/api/process-design/publish',
    method: 'post',
    data
  })
}

/**
 * 查询变更历史
 * @param {Number} id - 流程设计ID
 * @returns {Promise}
 */
export function getProcessHistory(id) {
  return request({
    url: `/workflow/api/process-design/${id}/history`,
    method: 'get'
  })
}

/**
 * 创建新版本
 * @param {Number} id - 基础流程设计ID
 * @param {Object} data - 变更说明
 * @returns {Promise}
 */
export function createNewVersion(id, data) {
  return request({
    url: `/workflow/api/process-design/${id}/new-version`,
    method: 'post',
    data
  })
}

/**
 * 删除草稿
 * @param {Number} id - 流程设计ID
 * @returns {Promise}
 */
export function deleteProcessDesign(id) {
  return request({
    url: `/workflow/api/process-design/${id}`,
    method: 'delete'
  })
}

