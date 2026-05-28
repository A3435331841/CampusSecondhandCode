import request from './request.js'

export const getAdminCommentList = (params) => request.get('/comment/admin/list', { params })
export const updateCommentStatus = (id, status) =>
  request.post('/comment/updateStatus', null, { params: { id, status } })
