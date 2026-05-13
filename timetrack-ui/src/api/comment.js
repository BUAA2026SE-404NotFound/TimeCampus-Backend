import request from '@/utils/request'

export function listComments(params) {
  return request({ url: '/admin/comments', method: 'get', params })
}

export function approveComment(id) {
  return request({ url: `/admin/comments/${id}/approve`, method: 'post' })
}

export function rejectComment(id, reason) {
  return request({ url: `/admin/comments/${id}/reject`, method: 'post', data: { reason } })
}
