import request from './request'

export const getFavoriteList = () =>
  request.get('/favorite/list')

export const addFavorite = (productId) =>
  request.post(`/favorite/add?productId=${productId}`)

export const removeFavorite = (productId) =>
  request.post(`/favorite/remove?productId=${productId}`)

export const getFavoriteStatus = (productId) =>
  request.get(`/favorite/status?productId=${productId}`)
