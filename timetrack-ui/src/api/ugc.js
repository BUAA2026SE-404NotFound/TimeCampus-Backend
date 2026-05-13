import request from '@/utils/request'

export function listUgc(status) {
  return request({ url: '/admin/ugc', method: 'get', params: { status } })
}

export function approveUgc(id) {
  return request({ url: `/admin/ugc/${id}/approve`, method: 'post' })
}

export function rejectUgc(id, reason) {
  return request({ url: `/admin/ugc/${id}/reject`, method: 'post', data: { reason } })
}
