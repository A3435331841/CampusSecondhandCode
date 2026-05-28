import request from './request'

export const getCommentList = (productId) =>
  request.get('/comment/list', { params: { productId } })

export const addComment = (productId, content) =>
  request.post('/comment/add', { productId, content })
