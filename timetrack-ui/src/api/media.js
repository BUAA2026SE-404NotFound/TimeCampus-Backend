import request from '@/utils/request'

export function listMedia(params) {
  return request({ url: '/admin/media', method: 'get', params })
}

export function deleteMedia(id) {
  return request({ url: `/admin/media/${id}`, method: 'delete' })
}

export function importOfficial(items) {
  return request({
    url: '/admin/contents/batch-import',
    method: 'post',
    data: { items }
  })
}
