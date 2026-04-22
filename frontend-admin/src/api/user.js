import request from './request.js'

export const getUserList = (params) => request.get('/user/list', { params })
export const updateUserStatus = (id, status) =>
  request.post('/user/updateStatus', null, { params: { id, status } })
