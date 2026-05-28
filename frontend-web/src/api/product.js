import request from './request'

export const getProductList = (params) =>
  request.get('/product/list', { params })

export const getProductDetail = (id) =>
  request.get(`/product/detail/${id}`)
