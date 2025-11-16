import request from '@/utils/request'

/**
 * 获取权限树
 */
export function getPermissionTree() {
  return request({
    url: '/auth/permission/tree',
    method: 'get'
  })
}

/**
 * 获取权限列表
 * @param {Object} params - {type}
 */
export function getPermissionList(params) {
  return request({
    url: '/auth/permission/list',
    method: 'get',
    params
  })
}

/**
 * 获取用户权限
 * @param {String} username - 用户名
 */
export function getUserPermissions(username) {
  return request({
    url: `/auth/permission/user/${username}`,
    method: 'get'
  })
}

/**
 * 获取用户权限编码列表
 * @param {String} username - 用户名
 */
export function getUserPermissionCodes(username) {
  return request({
    url: '/auth/permission/codes',
    method: 'get',
    params: { username }
  })
}

/**
 * 检查用户权限
 * @param {String} username - 用户名
 * @param {String} permissionCode - 权限编码
 */
export function checkPermission(username, permissionCode) {
  return request({
    url: '/auth/permission/check',
    method: 'get',
    params: { username, permissionCode }
  })
}

/**
 * 创建权限
 * @param {Object} data - 权限数据
 */
export function createPermission(data) {
  return request({
    url: '/auth/permission',
    method: 'post',
    data
  })
}

/**
 * 更新权限
 * @param {Number} permissionId - 权限ID
 * @param {Object} data - 权限数据
 */
export function updatePermission(permissionId, data) {
  return request({
    url: `/auth/permission/${permissionId}`,
    method: 'put',
    data
  })
}

/**
 * 删除权限
 * @param {Number} permissionId - 权限ID
 */
export function deletePermission(permissionId) {
  return request({
    url: `/auth/permission/${permissionId}`,
    method: 'delete'
  })
}

