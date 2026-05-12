import request from '@/utils/request'

export function getAdminMapOverview(params) {
  return request({ url: '/admin/map/overview', method: 'get', params })
}

export function getAdminMapConfig() {
  return request({ url: '/admin/map/config', method: 'get' })
}
