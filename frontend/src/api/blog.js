import request from '@/utils/request'

export function createBlog(data) {
  return request.post('/blog', data)
}

export function getBlogDetail(id) {
  return request.get(`/blog/${id}`)
}

export function getBlogPage(params) {
  return request.get('/blog/page', { params })
}

export function updateBlog(id, data) {
  return request.put(`/blog/${id}`, data)
}

export function deleteBlog(id) {
  return request.delete(`/blog/${id}`)
}

export function likeBlog(id) {
  return request.post(`/blog/${id}/like`)
}

export function getUserBlogs(userId, params) {
  return request.get('/blog/page', { params: { ...params, userId } })
}
