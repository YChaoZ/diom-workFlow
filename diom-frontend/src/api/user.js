import request from '@/utils/request'

/**
 * 获取用户列表
 */
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取用户
 */
export function getUserById(id) {
  return request({
    url: `/user/id/${id}`,
    method: 'get'
  })
}

/**
 * 根据用户名获取用户
 */
export function getUserByUsername(username) {
  return request({
    url: `/user/username/${username}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUser(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

