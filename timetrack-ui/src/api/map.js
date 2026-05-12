import request from '@/utils/request'

export function reverseGeocode(params) {
  return request({ url: '/map/reverse-geocode', method: 'get', params })
}

export function poiSearch(params) {
  return request({ url: '/map/poi-search', method: 'get', params })
}
