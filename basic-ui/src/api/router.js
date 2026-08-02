import request from '@/utils/request'

export function getRouterList(data) {
  return request({
    url: '/menu/navigate',
    method: 'post',
    data,
  })
}

export function getAuthRoutes() {
  return request({
    baseURL: '',
    url: '/auth/routes',
    method: 'get',
  })
}
