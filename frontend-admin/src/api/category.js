import request from './request.js'

export const getCategoryList = () => request.get('/category/list')
export const getAdminCategoryList = () => request.get('/category/admin/list')
export const saveCategory = (data) => request.post('/category/save', data)
export const updateCategoryStatus = (id, status) =>
  request.post('/category/updateStatus', null, { params: { id, status } })
