import request from '@/utils/request'

/**
 * 查询角色列表
 * @param {Object} params - {page, size, keyword}
 */
export function getRoleList(params) {
  return request({
    url: '/auth/role/list',
    method: 'get',
    params
  })
}

/**
 * 获取角色详情
 * @param {Number} roleId - 角色ID
 */
export function getRoleById(roleId) {
  return request({
    url: `/auth/role/${roleId}`,
    method: 'get'
  })
}

/**
 * 创建角色
 * @param {Object} data - {roleCode, roleName, description, status, sortOrder, permissionIds}
 */
export function createRole(data) {
  return request({
    url: '/auth/role',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 * @param {Number} roleId - 角色ID
 * @param {Object} data - {roleName, description, status, sortOrder, permissionIds}
 */
export function updateRole(roleId, data) {
  return request({
    url: `/auth/role/${roleId}`,
    method: 'put',
    data
  })
}

/**
 * 删除角色
 * @param {Number} roleId - 角色ID
 */
export function deleteRole(roleId) {
  return request({
    url: `/auth/role/${roleId}`,
    method: 'delete'
  })
}

/**
 * 为用户分配角色
 * @param {Object} data - {userId, username, roleIds}
 */
export function assignRolesToUser(data) {
  return request({
    url: '/auth/role/assign',
    method: 'post',
    data
  })
}

/**
 * 获取用户的角色列表
 * @param {String} username - 用户名
 */
export function getUserRoles(username) {
  return request({
    url: `/auth/role/user/${username}`,
    method: 'get'
  })
}

