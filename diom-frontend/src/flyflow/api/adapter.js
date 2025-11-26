/**
 * FlyFlow API 适配器
 * 将 FlyFlow 的 API 调用适配到 diom-flowable-service
 * 
 * FlyFlow 原始 API 格式：
 * {
 *   code: '00000',
 *   data: { ... },
 *   msg: '操作成功'
 * }
 * 
 * diom-flowable-service API 格式：
 * {
 *   code: 200,
 *   data: { ... },
 *   message: 'Success'
 * }
 */

import request from '@/utils/request'

/**
 * 通用响应转换器
 */
function transformResponse(res) {
  return {
    code: '00000',
    data: res.data || res,
    msg: res.message || '操作成功'
  }
}

/**
 * 流程定义 API 适配
 */
export const flowApi = {
  /**
   * 获取流程列表
   * FlyFlow: GET /api/flow/list
   * diom: GET /workflow/definitions
   */
  getFlowList(params) {
    return request({
      url: '/workflow/definitions',
      method: 'get',
      params: {
        page: params.pageNum || 1,
        size: params.pageSize || 10,
        name: params.name,
        key: params.key
      }
    }).then(res => {
      return {
        code: '00000',
        data: {
          list: Array.isArray(res.data) ? res.data : (res.data?.list || []),
          total: res.total || res.data?.total || 0
        },
        msg: '查询成功'
      }
    }).catch(err => {
      console.error('获取流程列表失败:', err)
      return {
        code: '50000',
        data: { list: [], total: 0 },
        msg: err.message || '查询失败'
      }
    })
  },

  /**
   * 启动流程
   * FlyFlow: POST /api/flow/start
   * diom: POST /workflow/start/{key}
   */
  startFlow(flowKey, data) {
    return request({
      url: `/workflow/start/${flowKey}`,
      method: 'post',
      data: data.variables || data
    }).then(transformResponse).catch(err => {
      console.error('启动流程失败:', err)
      return {
        code: '50000',
        data: null,
        msg: err.message || '流程启动失败'
      }
    })
  },

  /**
   * 获取流程详情
   * FlyFlow: GET /api/flow/detail/{id}
   * diom: GET /workflow/definition/{key}
   */
  getFlowDetail(id) {
    return request({
      url: `/workflow/definition/${id}`,
      method: 'get'
    }).then(transformResponse).catch(err => {
      console.error('获取流程详情失败:', err)
      return {
        code: '50000',
        data: null,
        msg: err.message || '获取流程详情失败'
      }
    })
  },

  /**
   * 部署流程
   * FlyFlow: POST /api/flow/deploy
   * diom: POST /workflow/deploy
   */
  deployFlow(data) {
    return request({
      url: '/workflow/deploy',
      method: 'post',
      data
    }).then(res => {
      return {
        code: '00000',
        data: res.data,
        msg: '流程部署成功'
      }
    }).catch(err => {
      console.error('部署流程失败:', err)
      return {
        code: '50000',
        data: null,
        msg: err.message || '流程部署失败'
      }
    })
  },

  /**
   * 删除流程定义
   * FlyFlow: DELETE /api/flow/{id}
   * diom: DELETE /workflow/definition/{key}
   */
  deleteFlow(id) {
    return request({
      url: `/workflow/definition/${id}`,
      method: 'delete'
    }).then(res => {
      return {
        code: '00000',
        data: res.data,
        msg: '流程删除成功'
      }
    }).catch(err => {
      console.error('删除流程失败:', err)
      return {
        code: '50000',
        data: null,
        msg: err.message || '流程删除失败'
      }
    })
  }
}

/**
 * 任务 API 适配
 */
export const taskApi = {
  /**
   * 获取待办任务
   * FlyFlow: GET /api/task/pending
   * diom: GET /workflow/tasks
   */
  getPendingTasks(params) {
    return request({
      url: '/workflow/tasks',
      method: 'get',
      params: {
        assignee: params.assignee,
        page: params.pageNum || 1,
        size: params.pageSize || 10
      }
    }).then(res => {
      return {
        code: '00000',
        data: {
          list: Array.isArray(res.data) ? res.data : (res.data?.list || []),
          total: res.total || res.data?.total || 0
        },
        msg: '查询成功'
      }
    }).catch(err => {
      console.error('获取待办任务失败:', err)
      return {
        code: '50000',
        data: { list: [], total: 0 },
        msg: err.message || '查询失败'
      }
    })
  },

  /**
   * 完成任务
   * FlyFlow: POST /api/task/complete
   * diom: POST /workflow/task/complete
   */
  completeTask(taskId, data) {
    return request({
      url: '/workflow/task/complete',
      method: 'post',
      data: {
        taskId,
        variables: data.variables,
        comment: data.comment
      }
    }).then(res => {
      return {
        code: '00000',
        data: res.data,
        msg: '任务完成'
      }
    }).catch(err => {
      console.error('完成任务失败:', err)
      return {
        code: '50000',
        data: null,
        msg: err.message || '任务完成失败'
      }
    })
  },

  /**
   * 获取我发起的流程
   * FlyFlow: GET /api/task/started
   * diom: GET /workflow/my-instances
   */
  getStartedTasks(params) {
    return request({
      url: '/workflow/my-instances',
      method: 'get',
      params: {
        page: params.pageNum || 1,
        size: params.pageSize || 10
      }
    }).then(res => {
      return {
        code: '00000',
        data: {
          list: Array.isArray(res.data) ? res.data : (res.data?.list || []),
          total: res.total || res.data?.total || 0
        },
        msg: '查询成功'
      }
    }).catch(err => {
      console.error('获取我发起的流程失败:', err)
      return {
        code: '50000',
        data: { list: [], total: 0 },
        msg: err.message || '查询失败'
      }
    })
  },

  /**
   * 获取已完成任务
   * FlyFlow: GET /api/task/completed
   * diom: GET /workflow/history/tasks
   */
  getCompletedTasks(params) {
    return request({
      url: '/workflow/history/tasks',
      method: 'get',
      params: {
        assignee: params.assignee,
        page: params.pageNum || 1,
        size: params.pageSize || 10
      }
    }).then(res => {
      return {
        code: '00000',
        data: {
          list: Array.isArray(res.data) ? res.data : (res.data?.list || []),
          total: res.total || res.data?.total || 0
        },
        msg: '查询成功'
      }
    }).catch(err => {
      console.error('获取已完成任务失败:', err)
      return {
        code: '50000',
        data: { list: [], total: 0 },
        msg: err.message || '查询失败'
      }
    })
  },

  /**
   * 获取抄送我的任务
   * FlyFlow: GET /api/task/cc
   * diom: GET /workflow/cc/tasks
   */
  getCcTasks(params) {
    return request({
      url: '/workflow/cc/tasks',
      method: 'get',
      params: {
        page: params.pageNum || 1,
        size: params.pageSize || 10
      }
    }).then(res => {
      return {
        code: '00000',
        data: {
          list: Array.isArray(res.data) ? res.data : (res.data?.list || []),
          total: res.total || res.data?.total || 0
        },
        msg: '查询成功'
      }
    }).catch(err => {
      console.error('获取抄送任务失败:', err)
      return {
        code: '50000',
        data: { list: [], total: 0 },
        msg: err.message || '查询失败'
      }
    })
  },

  /**
   * 获取任务详情
   * FlyFlow: GET /api/task/detail/{id}
   * diom: GET /workflow/task/{id}
   */
  getTaskDetail(id) {
    return request({
      url: `/workflow/task/${id}`,
      method: 'get'
    }).then(transformResponse).catch(err => {
      console.error('获取任务详情失败:', err)
      return {
        code: '50000',
        data: null,
        msg: err.message || '获取任务详情失败'
      }
    })
  }
}

/**
 * 流程实例 API 适配
 */
export const processInstanceApi = {
  /**
   * 获取流程实例列表
   * FlyFlow: GET /api/processInstance/list
   * diom: GET /workflow/instances
   */
  getInstanceList(params) {
    return request({
      url: '/workflow/instances',
      method: 'get',
      params: {
        page: params.pageNum || 1,
        size: params.pageSize || 10,
        processDefinitionKey: params.processDefinitionKey
      }
    }).then(res => {
      return {
        code: '00000',
        data: {
          list: Array.isArray(res.data) ? res.data : (res.data?.list || []),
          total: res.total || res.data?.total || 0
        },
        msg: '查询成功'
      }
    }).catch(err => {
      console.error('获取流程实例列表失败:', err)
      return {
        code: '50000',
        data: { list: [], total: 0 },
        msg: err.message || '查询失败'
      }
    })
  },

  /**
   * 获取流程实例详情
   * FlyFlow: GET /api/processInstance/detail/{id}
   * diom: GET /workflow/instance/{id}
   */
  getInstanceDetail(id) {
    return request({
      url: `/workflow/instance/${id}`,
      method: 'get'
    }).then(transformResponse).catch(err => {
      console.error('获取流程实例详情失败:', err)
      return {
        code: '50000',
        data: null,
        msg: err.message || '获取流程实例详情失败'
      }
    })
  }
}

/**
 * 用户 API 适配
 */
export const userApi = {
  /**
   * 获取用户信息
   * FlyFlow: GET /api/user/info
   * diom: GET /auth/user/info
   */
  getUserInfo() {
    return request({
      url: '/auth/user/info',
      method: 'get'
    }).then(transformResponse).catch(err => {
      console.error('获取用户信息失败:', err)
      return {
        code: '50000',
        data: null,
        msg: err.message || '获取用户信息失败'
      }
    })
  }
}

// 默认导出所有 API
export default {
  flowApi,
  taskApi,
  processInstanceApi,
  userApi
}

