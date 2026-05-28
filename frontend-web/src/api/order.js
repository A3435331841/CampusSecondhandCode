import request from './request'

export const getMyBuyOrders = (params) =>
  request.get('/order/my/buy', { params })

export const getMySellOrders = (params) =>
  request.get('/order/my/sell', { params })

export const createOrder = (data) =>
  request.post('/order/create', data)

export const cancelOrder = (orderNo) =>
  request.post(`/order/cancel?orderNo=${encodeURIComponent(orderNo)}`)

export const confirmOrder = (orderNo) =>
  request.post(`/order/confirm?orderNo=${encodeURIComponent(orderNo)}`)
