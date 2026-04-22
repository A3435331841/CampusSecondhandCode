import request from './request.js'

export const getProductList = (params) => request.get('/product/list', { params })
export const auditProduct = (id, status) =>
  request.post('/product/audit', null, { params: { id, status } })
