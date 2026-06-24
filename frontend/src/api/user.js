import request from '@/utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserInfo(id) {
  return request.get(`/user/${id}`)
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}
