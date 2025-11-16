import request from '@/utils/request'

/**
 * 获取公开模板列表
 */
export function getPublicTemplates(processKey) {
  return request({
    url: '/workflow/template/public',
    method: 'get',
    params: { processKey }
  })
}

/**
 * 获取我的模板列表
 */
export function getMyTemplates(userId) {
  return request({
    url: '/workflow/template/my',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取模板详情
 */
export function getTemplateById(id) {
  return request({
    url: `/workflow/template/${id}`,
    method: 'get'
  })
}

/**
 * 创建模板
 */
export function createTemplate(data) {
  return request({
    url: '/workflow/template',
    method: 'post',
    data
  })
}

/**
 * 更新模板
 */
export function updateTemplate(id, data) {
  return request({
    url: `/workflow/template/${id}`,
    method: 'put',
    data
  })
}

/**
 * 使用模板（增加使用次数）
 */
export function useTemplate(id) {
  return request({
    url: `/workflow/template/${id}/use`,
    method: 'post'
  })
}

/**
 * 删除模板
 */
export function deleteTemplate(id) {
  return request({
    url: `/workflow/template/${id}`,
    method: 'delete'
  })
}

// ==================== 草稿管理 ====================

/**
 * 获取我的草稿列表
 */
export function getMyDrafts(userId) {
  return request({
    url: '/workflow/template/draft/my',
    method: 'get',
    params: { userId }
  })
}

/**
 * 根据流程Key获取草稿
 */
export function getDraftsByProcessKey(processKey, userId) {
  return request({
    url: `/workflow/template/draft/process/${processKey}`,
    method: 'get',
    params: { userId }
  })
}

/**
 * 保存草稿
 */
export function saveDraft(data) {
  return request({
    url: '/workflow/template/draft',
    method: 'post',
    data
  })
}

/**
 * 删除草稿
 */
export function deleteDraft(id) {
  return request({
    url: `/workflow/template/draft/${id}`,
    method: 'delete'
  })
}

