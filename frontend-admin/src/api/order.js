import request from './request.js'

export const getOrderStats = () => request.get('/order/stats')
