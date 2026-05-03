import request from '@/utils/request'

export function listLogs(params) {
  return request({ url: '/admin/logs', method: 'get', params })
}
