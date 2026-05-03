import request from '@/utils/request'

export function listPois(params) {
  return request({ url: '/admin/pois', method: 'get', params })
}

export function createPoi(data) {
  return request({ url: '/admin/pois', method: 'post', data })
}

export function updatePoi(id, data) {
  return request({ url: `/admin/pois/${id}`, method: 'put', data })
}

export function deletePoi(id) {
  return request({ url: `/admin/pois/${id}`, method: 'delete' })
}
