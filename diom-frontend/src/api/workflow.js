import request from '@/utils/request'

/**
 * 获取流程定义列表
 */
export function getProcessDefinitions() {
  return request({
    url: '/workflow/definitions',
    method: 'get'
  })
}

/**
 * 根据key获取流程定义
 */
export function getProcessDefinitionByKey(key) {
  return request({
    url: `/workflow/definition/${key}`,
    method: 'get'
  })
}

/**
 * 获取流程定义的BPMN XML
 */
export function getProcessDefinitionXml(key) {
  return request({
    url: `/workflow/definition/${key}/xml`,
    method: 'get'
  })
}

/**
 * 启动流程实例
 */
export function startProcess(processKey, data) {
  return request({
    url: `/workflow/start/${processKey}`,
    method: 'post',
    data
  })
}

/**
 * 获取我的任务列表
 */
export function getMyTasks(params) {
  return request({
    url: '/workflow/tasks',
    method: 'get',
    params
  })
}

/**
 * 获取任务详情
 */
export function getTaskDetail(taskId) {
  return request({
    url: `/workflow/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 完成任务
 */
export function completeTask(taskId, data) {
  return request({
    url: `/workflow/task/${taskId}/complete`,
    method: 'post',
    data
  })
}


/**
 * 查询流程实例（根据流程实例ID）
 */
export function getProcessInstance(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}`,
    method: 'get'
  })
}

/**
 * 查询历史流程实例
 */
export function getHistoricProcessInstances(processDefinitionKey) {
  return request({
    url: '/workflow/history/instances',
    method: 'get',
    params: { processDefinitionKey }
  })
}

/**
 * 查询历史任务
 */
export function getHistoricTasks(processInstanceId) {
  return request({
    url: '/workflow/history/tasks',
    method: 'get',
    params: { processInstanceId }
  })
}

