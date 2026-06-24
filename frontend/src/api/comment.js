import request from '@/utils/request'

export function createComment(data) {
  return request.post('/comment', data)
}

export function getComments(blogId) {
  return request.get(`/comment/blog/${blogId}`)
}
